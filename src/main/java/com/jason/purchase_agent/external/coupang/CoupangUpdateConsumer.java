package com.jason.purchase_agent.external.coupang;

import com.fasterxml.jackson.databind.JsonNode;
import com.jason.purchase_agent.dto.products.PriceUpdateChannelMessage;
import com.jason.purchase_agent.dto.products.PriceUpdateMessage;
import com.jason.purchase_agent.dto.products.StockUpdateChannelMessage;
import com.jason.purchase_agent.dto.products.StockUpdateMessage;
import com.jason.purchase_agent.service.process_status.ProcessStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoupangUpdateConsumer {

    private final CoupangApiService coupangApiService;
    private final ProcessStatusService pss;

    @RabbitListener(queues = "price-update-coupang", concurrency = "1")
    public void handlePriceUpdate(PriceUpdateChannelMessage msg) {
        try {
            Integer salePrice = msg.getSalePrice();
            Double marginRate = msg.getMarginRate();
            salePrice = (int) (Math.ceil((salePrice * (100 - 18.5 - marginRate) / (100 - 12 - marginRate))/100.0) * 100);
            String responseJson = coupangApiService.updatePrice(msg.getChannelId1(), salePrice);
            JsonNode root = objectMapper.readTree(responseJson);

            String code = root.path("code").asText("");
            boolean findSuccess = "SUCCESS".equalsIgnoreCase(code);

            // 채널 결과 map 준비
            Map<String, Object> channelResult = new HashMap<>();
            if (findSuccess) {
                String message = String.format("가격: %,d원, ID: %s", salePrice, msg.getChannelId1());
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", message);
                log.info("[{}][Coupang][Price] 성공 (vendorItemId={}, salePrice={})",
                        msg.getProductCode(), msg.getChannelId1(), salePrice);
            } else {
                String message = root.get("message").asText();
                // String message = String.format("코드: {}", code);
                channelResult.put("status", "FAIL");
                channelResult.put("message", message);
                // {"code":"ERROR","message":"가격변경에 실패했습니다. [옵션ID[87744527109] : 판매가 변경이 불가능합니다. 변경전 판매가의 최대 50% 인하/최대 100%인상까지 변경가능합니다.]"}
                log.error("[{}][Coupang][Price] 실패 (responseJson={})",
                        msg.getProductCode(), responseJson);
            }

            pss.mergeChannelResult(msg.getBatchId(), msg.getProductCode(), "coupang", channelResult);

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (Exception e) {

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            pss.mergeChannelResult(msg.getBatchId(), msg.getProductCode(), "coupang", channelResult);

            log.error("[{}][Coupang][Price] 실패 (e.getMessage()={})", msg.getProductCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

    @RabbitListener(queues = "stock-update-coupang", concurrency = "1")
    public void handleStockUpdate(StockUpdateChannelMessage msg) {
        try {
            String responseJson = coupangApiService.updateStock(msg.getChannelId1(), msg.getStock());
            JsonNode root = objectMapper.readTree(responseJson);

            String code = root.path("code").asText(""); // "SUCCESS" or error code
            boolean findSuccess = "SUCCESS".equalsIgnoreCase(code);

            // 채널 결과 map 준비
            Map<String, Object> channelResult = new HashMap<>();
            if (findSuccess) {
                String message = String.format("재고: %d개, ID: %s", msg.getStock(), msg.getChannelId1());
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", message);
                log.info("[{}][Coupang][Stock] 성공 (vendorItemId={}, stock={})",
                        msg.getProductCode(), msg.getChannelId1(), msg.getStock());
            } else {
                String message = String.format("코드: {}", code);
                channelResult.put("status", "FAIL");
                channelResult.put("message", message);
                log.error("[{}][Coupang][Stock] 실패 (responseJson={})",
                        msg.getProductCode(), responseJson);
            }

            pss.mergeChannelResult(msg.getBatchId(), msg.getProductCode(), "coupang", channelResult);

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (Exception e) {

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            pss.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "coupang", channelResult
            );

            log.error("[{}][Coupang][Stock] 실패 (e.getMessage()={})", msg.getProductCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }
}
