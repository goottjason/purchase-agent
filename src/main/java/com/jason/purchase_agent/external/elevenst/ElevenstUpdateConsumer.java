package com.jason.purchase_agent.external.elevenst;

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
public class ElevenstUpdateConsumer {
    private final ElevenstApiService elevenstApiService;
    private final ProcessStatusService processStatusService;

    @RabbitListener(queues = "price-update-elevenst")
    public void handlePriceUpdate(PriceUpdateMessage msg) {
        log.info("[MQ][Elevenst][PriceUpdate] 메시지 수신 - {}", msg);

        try {
            log.info("[ElevenstAPI][Price] 가격 변경 API 호출 시작 - channelId={}, salePrice={}", msg.getChannelId(), msg.getSalePrice());

            String responseXml = elevenstApiService.updatePrice(msg.getChannelId(), msg.getSalePrice());
            JsonNode root = objectMapper.readTree(responseXml);

            String code = root.path("code").asText(""); // "SUCCESS" or error code or empty
            String returnedMessage = root.path("message").asText("상세 메시지 없음");
            boolean findSuccess = "SUCCESS".equalsIgnoreCase(code) || code.isEmpty();

            Map<String, Object> channelResult = new HashMap<>();
            // code가 없거나 SUCCESS면 성공!
            if (findSuccess) {
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", "가격 변경을 완료했습니다.");
            } else {
                channelResult.put("status", "FAIL");
                channelResult.put("message", code + " : " + returnedMessage);
            }

            log.info("[Elevenst][PriceUpdate] 상태 병합 시작 - batchId={}, productCode={}, status={}", msg.getBatchId(), msg.getProductCode(), channelResult.get("status"));

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "elevenst", channelResult
            );
            log.debug("[Elevenst][Price] 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][Elevenst][PriceUpdate] 처리 후 Thread.sleep(100ms)");

        } catch (Exception e) {
            log.error("[MQ][Elevenst][PriceUpdate] 처리 중 예외! - batchId={}, productCode={}, 원인={}", msg.getBatchId(), msg.getProductCode(), e.getMessage(), e);

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "elevenst", channelResult
            );

            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

    @RabbitListener(queues = "stock-update-elevenst")
    public void handleStockUpdate(StockUpdateMessage msg) {
        log.info("[MQ][Elevenst][StockUpdate] 메시지 수신 - {}", msg);

        try {
            log.info("[ElevenstAPI][Stock] 재고 변경 API 호출 시작 - channelId={}, stock={}", msg.getChannelId(), msg.getStock());

            String responseJson = elevenstApiService.updateStock(msg.getChannelId(), msg.getStock());
            JsonNode root = objectMapper.readTree(responseJson);

            String code = root.path("code").asText(""); // "SUCCESS" or error code or empty
            String returnedMessage = root.path("message").asText("상세 메시지 없음");
            boolean findSuccess = "SUCCESS".equalsIgnoreCase(code) || code.isEmpty();

            Map<String, Object> channelResult = new HashMap<>();
            if (findSuccess) {
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", "재고 변경을 완료했습니다.");
            } else {
                channelResult.put("status", "FAIL");
                channelResult.put("message", code + " : " + returnedMessage);
            }

            log.info("[Elevenst][StockUpdate] 상태 병합 시작 - batchId={}, productCode={}, status={}", msg.getBatchId(), msg.getProductCode(), channelResult.get("status"));

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "elevenst", channelResult
            );
            log.debug("[Elevenst][Stock] 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][Elevenst][StockUpdate] 처리 후 Thread.sleep(100ms)");

        } catch (Exception e) {
            log.error("[MQ][Elevenst][StockUpdate] 처리 중 예외! - batchId={}, productCode={}, 원인={}", msg.getBatchId(), msg.getProductCode(), e.getMessage(), e);

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "elevenst", channelResult
            );

            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

}
