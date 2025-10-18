package com.jason.purchase_agent.external.smartstore;

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
public class SmartstoreUpdateConsumer {
    private final SmartstoreApiService smartstoreApiService;
    private final ProcessStatusService processStatusService;

    @RabbitListener(queues = "price-update-smartstore", concurrency = "1")
    public void handlePriceUpdate(PriceUpdateChannelMessage msg) {
        try {
            Integer salePrice = msg.getSalePrice();
            Double marginRate = msg.getMarginRate();
            salePrice = (int) (Math.ceil((salePrice * (100 - 18.5 - marginRate) / (100 - 17 - marginRate))/100.0) * 100);
            String responseJson = smartstoreApiService.updatePrice(msg.getChannelId1(), salePrice);
            JsonNode root = objectMapper.readTree(responseJson);

            String code = root.has("code") ? root.path("code").asText() : null;
            String returnedMessage;
            String status;

            if (code != null && !code.isEmpty()) {
                // 실패 케이스 (code 존재)
                status = "FAIL";
                returnedMessage = code + " : " + root.path("message").asText("상세 메시지 없음");
                log.error("[{}][Smartstore][Price] 실패 (responseJson={})", msg.getProductCode(), responseJson);
            } else {
                // 성공 케이스 (code 없음)
                status = "SUCCESS";
                returnedMessage = String.format("가격: %,d원, ID: %s", salePrice, msg.getChannelId1());
                log.info("[{}][Smartstore][Price] 성공 (originProductNo={}, salePrice={})"
                        , msg.getProductCode(), msg.getChannelId1(), salePrice);
            }

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", status);
            channelResult.put("message", returnedMessage);

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "smartstore", channelResult
            );

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (Exception e) {

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "smartstore", channelResult
            );

            log.error("[{}][Smartstore][Price] 실패 (e.getMessage()={})", msg.getProductCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

    @RabbitListener(queues = "stock-update-smartstore", concurrency = "1")
    public void handleStockUpdate(StockUpdateChannelMessage msg) {
        try {
            String responseJson = smartstoreApiService.updateStock(msg.getChannelId1(), msg.getStock());
            JsonNode root = objectMapper.readTree(responseJson);

            String code = root.has("code") ? root.path("code").asText() : null;
            String returnedMessage;
            String status;

            if (code != null && !code.isEmpty()) {
                // 실패 케이스
                status = "FAIL";
                returnedMessage = code + " : " + root.path("message").asText("상세 메시지 없음");
                log.error("[{}][Smartstore][Stock] 실패 (responseJson={})", msg.getProductCode(), responseJson);
            } else {
                // 성공 케이스
                status = "SUCCESS";
                returnedMessage = String.format("재고: %d개, ID: %s", msg.getStock(), msg.getChannelId1());;
                log.info("[{}][Smartstore][Stock] 성공 (originProductNo={}, stock={})"
                        , msg.getProductCode(), msg.getChannelId1(), msg.getStock());
            }

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", status);
            channelResult.put("message", returnedMessage);

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "smartstore", channelResult
            );

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (Exception e) {

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "smartstore", channelResult
            );

            log.error("[{}][Smartstore][Stock] 실패 (e.getMessage()={})", msg.getProductCode(), e.getMessage());            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

}
