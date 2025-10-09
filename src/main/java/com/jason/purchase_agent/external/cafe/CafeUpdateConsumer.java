package com.jason.purchase_agent.external.cafe;

import com.fasterxml.jackson.databind.JsonNode;
import com.jason.purchase_agent.dto.products.PriceUpdateMessage;
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

    @RabbitListener(queues = "price-update-cafe")
    public void handlePriceUpdate(PriceUpdateMessage msg) {
        log.info("[MQ][Cafe][PriceUpdate] 메시지 수신 - {}", msg);

        try {
            log.info("[Cafe][Price] 가격 변경 API 호출 시작 - channelId={}, salePrice={}",
                    msg.getChannelId(), msg.getSalePrice());
            String responseJson = cafeApiService.updatePrice(msg.getChannelId(), msg.getProductCode(), msg.getSalePrice());
            JsonNode root = objectMapper.readTree(responseJson);

            String status;
            String returnedMessage;
            if (root.has("product")) {
                // 성공 케이스
                status = "SUCCESS";
                returnedMessage = String.format("가격: %,d원, ID: %s", msg.getSalePrice(), msg.getChannelId());
            } else if (root.has("error")) {
                // 실패 케이스
                JsonNode errorNode = root.path("error");
                String code = errorNode.path("code").asText();
                String message = errorNode.path("message").asText("상세 메시지 없음");
                status = "FAIL";
                returnedMessage = code + " : " + message;
            } else {
                // 예외적 파싱 실패 처리
                status = "FAIL";
                returnedMessage = "알 수 없는 응답: " + responseJson;
            }

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", status);
            channelResult.put("message", returnedMessage);

            log.info("[Cafe][PriceUpdate] 상태 병합 시작 - batchId={}, productCode={}, status={}",
                    msg.getBatchId(), msg.getProductCode(), status);

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "cafe", channelResult
            );
            log.debug("[Cafe][Price] 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][Cafe][PriceUpdate] 처리 후 Thread.sleep(100ms)");

        } catch (Exception e) {
            log.error("[MQ][Cafe][PriceUpdate] 처리 중 예외! - batchId={}, productCode={}, 원인={}",
                    msg.getBatchId(), msg.getProductCode(), e.getMessage(), e);

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "cafe", channelResult
            );

            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }
    @RabbitListener(queues = "stock-update-cafe")
    public void handleStockUpdate(StockUpdateMessage msg) {
        log.info("[MQ][Cafe][StockUpdate] 메시지 수신 - {}", msg);

        try {
            log.info("[Cafe][Stock] 재고 변경 API 호출 시작 - channelId={}, channelId2={}, stock={}",
                    msg.getChannelId(), msg.getChannelId2(), msg.getStock());
            String responseJson = cafeApiService.updateStock(
                    msg.getChannelId(), msg.getChannelId2(), msg.getStock()
            );
            JsonNode root = objectMapper.readTree(responseJson);

            String status;
            String returnedMessage;

            if (root.has("inventory")) {
                // 성공 케이스
                status = "SUCCESS";
                returnedMessage = String.format("재고: %,d개, ID: %s / %s",
                        msg.getStock(), msg.getChannelId(), msg.getChannelId2());
            } else if (root.has("error")) {
                // 실패 케이스
                JsonNode errorNode = root.path("error");
                String code = errorNode.path("code").asText();
                String message = errorNode.path("message").asText("상세 메시지 없음");
                status = "FAIL";
                returnedMessage = code + " : " + message;
            } else {
                // 예외적(파싱 실패 등)
                status = "FAIL";
                returnedMessage = "알 수 없는 응답: " + responseJson;
            }

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", status);
            channelResult.put("message", returnedMessage);

            log.info("[Cafe][StockUpdate] 상태 병합 시작 - batchId={}, productCode={}, status={}",
                    msg.getBatchId(), msg.getProductCode(), status);

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "cafe", channelResult
            );
            log.debug("[Cafe][Stock] 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][Cafe][StockUpdate] 처리 후 Thread.sleep(100ms)");

        } catch (Exception e) {
            log.error("[MQ][Cafe][StockUpdate] 처리 중 예외! - batchId={}, productCode={}, 원인={}",
                    msg.getBatchId(), msg.getProductCode(), e.getMessage(), e);

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "cafe", channelResult
            );

            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }
}
