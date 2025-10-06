package com.jason.purchase_agent.external.smartstore;

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
public class SmartstoreUpdateConsumer {
    private final SmartstoreApiService smartstoreApiService;
    private final ProcessStatusService processStatusService;

    @RabbitListener(queues = "price-update-smartstore")
    public void handlePriceUpdate(PriceUpdateMessage msg) {
        log.info("[MQ][Smartstore][PriceUpdate] 메시지 수신 - {}", msg);

        try {
            log.info("[SmartstoreAPI][Price] 가격 변경 API 호출 시작 - channelId={}, salePrice={}", msg.getChannelId(), msg.getSalePrice());

            String responseJson = smartstoreApiService.updatePrice(msg.getChannelId(), msg.getSalePrice());
            JsonNode root = objectMapper.readTree(responseJson);

            String code = root.has("code") ? root.path("code").asText() : null;
            String returnedMessage;
            String status;

            if (code != null && !code.isEmpty()) {
                // 실패 케이스 (code 존재)
                status = "FAIL";
                returnedMessage = code + " : " + root.path("message").asText("상세 메시지 없음");
            } else {
                // 성공 케이스 (code 없음)
                status = "SUCCESS";
                returnedMessage = "가격 변경을 완료했습니다.";
            }

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", status);
            channelResult.put("message", returnedMessage);

            log.info("[Smartstore][PriceUpdate] 상태 병합 시작 - batchId={}, productCode={}, status={}", msg.getBatchId(), msg.getProductCode(), status);

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "smartstore", channelResult
            );
            log.debug("[Smartstore][Price] 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][Smartstore][PriceUpdate] 처리 후 Thread.sleep(100ms)");

        } catch (Exception e) {
            log.error("[MQ][Smartstore][PriceUpdate] 처리 중 예외! - batchId={}, productCode={}, 원인={}", msg.getBatchId(), msg.getProductCode(), e.getMessage(), e);

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "smartstore", channelResult
            );

            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

    @RabbitListener(queues = "stock-update-smartstore")
    public void handleStockUpdate(StockUpdateMessage msg) {
        log.info("[MQ][Smartstore][StockUpdate] 메시지 수신 - {}", msg);

        try {
            log.info("[SmartstoreAPI][Stock] 재고 변경 API 호출 시작 - channelId={}, stock={}", msg.getChannelId(), msg.getStock());

            String responseJson = smartstoreApiService.updateStock(msg.getChannelId(), msg.getStock());
            JsonNode root = objectMapper.readTree(responseJson);

            String code = root.has("code") ? root.path("code").asText() : null;
            String returnedMessage;
            String status;

            if (code != null && !code.isEmpty()) {
                // 실패 케이스
                status = "FAIL";
                returnedMessage = code + " : " + root.path("message").asText("상세 메시지 없음");
            } else {
                // 성공 케이스
                status = "SUCCESS";
                returnedMessage = "재고 변경을 완료했습니다.";
            }

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", status);
            channelResult.put("message", returnedMessage);

            log.info("[Smartstore][StockUpdate] 상태 병합 시작 - batchId={}, productCode={}, status={}", msg.getBatchId(), msg.getProductCode(), status);

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "smartstore", channelResult
            );
            log.debug("[Smartstore][Stock] 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][Smartstore][StockUpdate] 처리 후 Thread.sleep(100ms)");

        } catch (Exception e) {
            log.error("[MQ][Smartstore][StockUpdate] 처리 중 예외! - batchId={}, productCode={}, 원인={}", msg.getBatchId(), msg.getProductCode(), e.getMessage(), e);

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "smartstore", channelResult
            );

            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

}
