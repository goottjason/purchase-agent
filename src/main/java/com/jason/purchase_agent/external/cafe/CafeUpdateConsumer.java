package com.jason.purchase_agent.external.cafe;

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
public class CafeUpdateConsumer {
    private final CafeApiService cafeApiService;
    private final ProcessStatusService processStatusService;

    @RabbitListener(queues = "price-update-cafe", concurrency = "1")
    public void handlePriceUpdate(PriceUpdateChannelMessage msg) {
        try {
            String responseJson = cafeApiService.updatePrice(msg.getChannelId1(), msg.getProductCode(), msg.getSalePrice());
            JsonNode root = objectMapper.readTree(responseJson);

            String status;
            String returnedMessage;
            if (root.has("product")) {
                // 성공 케이스
                status = "SUCCESS";
                returnedMessage = String.format("가격: %,d원, ID: %s", msg.getSalePrice(), msg.getChannelId1());
                log.info("[{}][Cafe][Price] 성공 (cafeNo={}, salePrice={})",
                        msg.getProductCode(), msg.getChannelId1(), msg.getSalePrice());
            } else {
                // 실패 케이스
                JsonNode errorNode = root.path("error");
                String code = errorNode.path("code").asText();
                String message = errorNode.path("message").asText("상세 메시지 없음");
                status = "FAIL";
                returnedMessage = code + " : " + message;
                log.error("[{}][Cafe][Price] 실패 (responseJson={})",
                        msg.getProductCode(), responseJson);
            }

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", status);
            channelResult.put("message", returnedMessage);

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "cafe", channelResult
            );

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (Exception e) {

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "cafe", channelResult
            );
            log.error("[{}][Cafe][Price] 실패 (e.getMessage()={})", msg.getProductCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }
    @RabbitListener(queues = "stock-update-cafe", concurrency = "1")
    public void handleStockUpdate(StockUpdateChannelMessage msg) {

        try {
            String responseJson = cafeApiService.updateStock(
                    msg.getChannelId1(), msg.getChannelId2(), msg.getStock()
            );
            JsonNode root = objectMapper.readTree(responseJson);

            String status;
            String returnedMessage;

            if (root.has("inventory")) {
                status = "SUCCESS";
                returnedMessage = String.format("재고: %,d개, ID: %s / %s",
                        msg.getStock(), msg.getChannelId1(), msg.getChannelId2());
                log.info("[{}][Cafe][Stock] 성공 (cafeNo={}, cafeOptCode={} stock={})",
                        msg.getProductCode(), msg.getChannelId1(), msg.getChannelId2(), msg.getStock());
            } else {
                // 실패 케이스
                JsonNode errorNode = root.path("error");
                String code = errorNode.path("code").asText();
                String message = errorNode.path("message").asText("상세 메시지 없음");
                status = "FAIL";
                returnedMessage = code + " : " + message;
                log.error("[{}][Cafe][Stock] 실패 (responseJson={})",
                        msg.getProductCode(), responseJson);
            }

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", status);
            channelResult.put("message", returnedMessage);

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "cafe", channelResult
            );

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (Exception e) {

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "cafe", channelResult
            );

            log.error("[{}][Cafe][Stock] 실패 (e.getMessage()={})", msg.getProductCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }
}
