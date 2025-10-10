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
            String responseJson = coupangApiService.updatePrice(msg.getChannelId1(), msg.getSalePrice());
            JsonNode root = objectMapper.readTree(responseJson);

            String code = root.path("code").asText("");
            boolean findSuccess = "SUCCESS".equalsIgnoreCase(code);

            // 채널 결과 map 준비
            Map<String, Object> channelResult = new HashMap<>();
            if (findSuccess) {
                String message = String.format("가격: %,d원, ID: %s", msg.getSalePrice(), msg.getChannelId1());
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", message);
                log.info("[{}][Coupang][Price] 성공", msg.getProductCode());
            } else {
                String message = String.format("코드: {}", code);
                channelResult.put("status", "FAIL");
                channelResult.put("message", message);
                log.error("[{}][Coupang][Price] 실패", msg.getProductCode());
            }

            pss.mergeChannelResult(msg.getBatchId(), msg.getProductCode(), "coupang", channelResult);

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (Exception e) {

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            pss.mergeChannelResult(msg.getBatchId(), msg.getProductCode(), "coupang", channelResult);

            log.error("[{}][Coupang][Price] 실패({})", msg.getProductCode(), e.getMessage());
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
                log.info("[{}][Coupang][Stock] 성공", msg.getProductCode());
            } else {
                String message = String.format("코드: {}", code);
                channelResult.put("status", "FAIL");
                channelResult.put("message", message);
                log.error("[{}][Coupang][Stock] 실패", msg.getProductCode());
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

            log.error("[{}][Coupang][Stock] 실패({})", msg.getProductCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }
}
