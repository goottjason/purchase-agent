package com.jason.purchase_agent.external.coupang;

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
public class CoupangUpdateConsumer {

    private final CoupangApiService coupangApiService;
    private final ProcessStatusService processStatusService;

    @RabbitListener(queues = "price-update-coupang")
    public void handlePriceUpdate(PriceUpdateMessage msg) {
        log.info("[MQ][PriceUpdate] 메시지 수신 - {}", msg);
        try {
            log.info("[MQ][PriceUpdate] API 호출 시작 - channelId={}, salePrice={}", msg.getChannelId(), msg.getSalePrice());
            String responseJson = coupangApiService.updatePrice(msg.getChannelId(), msg.getSalePrice());
            JsonNode root = objectMapper.readTree(responseJson);

            String code = root.path("code").asText(""); // string "SUCCESS" or error code
            String returnedMessage = root.path("message").asText("상세 메시지 없음");
            boolean findSuccess = "SUCCESS".equalsIgnoreCase(code);

            // 채널 결과 map 준비
            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "SUCCESS");
            channelResult.put("message", returnedMessage);

            log.info("[MQ][PriceUpdate] 결과 mergeChannelResult 호출 - batchId={}, productCode={}, success={}",
                    msg.getBatchId(), msg.getProductCode(), findSuccess);

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "coupang", channelResult
            );

        } catch (Exception e) {

            log.error("[MQ][PriceUpdate] 처리 중 예외! - batchId={}, productCode={}, 원인={}",
                    msg.getBatchId(), msg.getProductCode(), e.getMessage(), e);

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "coupang", channelResult
            );

            log.error("[MQ][PriceUpdate] 메시지 폐기 - {} | 원인: {}", msg, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

    @RabbitListener(queues = "stock-update-coupang")
    public void handleStockUpdate(StockUpdateMessage msg) {

        log.info("[MQ][StockUpdate] 메시지 수신 - {}", msg);

        try {
            log.info("[MQ][StockUpdate] API 호출 시작 - channelId={}, stock={}", msg.getChannelId(), msg.getStock());
            String responseJson = coupangApiService.updateStock(msg.getChannelId(), msg.getStock());
            JsonNode root = objectMapper.readTree(responseJson);

            String code = root.path("code").asText(""); // "SUCCESS" or error code
            String returnedMessage = root.path("message").asText("상세 메시지 없음");
            boolean findSuccess = "SUCCESS".equalsIgnoreCase(code);

            // 채널 결과 map 준비
            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "SUCCESS");
            channelResult.put("message", returnedMessage);

            log.info("[MQ][StockUpdate] 결과 mergeChannelResult 호출 - batchId={}, productCode={}, success={}",
                    msg.getBatchId(), msg.getProductCode(), findSuccess);

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "coupang", channelResult
            );

        } catch (Exception e) {
            log.error("[MQ][StockUpdate] 처리 중 예외! - batchId={}, productCode={}, 원인={}",
                    msg.getBatchId(), msg.getProductCode(), e.getMessage(), e);

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "coupang", channelResult
            );

            log.error("[MQ][StockUpdate] 메시지 폐기 - {} | 원인: {}", msg, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }
}
