package com.jason.purchase_agent.external.smartstore;

import com.jason.purchase_agent.dto.products.PriceUpdateMessage;
import com.jason.purchase_agent.dto.products.StockUpdateMessage;
import com.jason.purchase_agent.service.process_status.ProcessStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartstorePriceUpdateConsumer {
    private final SmartstoreApiService smartstoreApiService;
    private final ProcessStatusService processStatusService;

    @RabbitListener(queues = "price-update-smartstore")
    public void handlePriceUpdate(PriceUpdateMessage msg) {
        log.info("[MQ][Smartstore][PriceUpdate] 메시지 수신 - {}", msg);

        try {
            log.info("[SmartstoreAPI][Price] 가격 변경 API 호출 시작 - channelId={}, salePrice={}", msg.getChannelId(), msg.getSalePrice());

            Map<String, Object> result = smartstoreApiService.updatePrice(msg.getChannelId(), msg.getSalePrice());
            log.info("[SmartstoreAPI][Price] API 응답 결과 - {}", result);
            log.info("[Smartstore][Price] 상태 병합 시작 - batchId={}, productCode={}", msg.getBatchId(), msg.getProductCode());

            // 병합 전략 적용 (트랜잭션, select-for-update 등, 서비스에서 처리!)
            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "smartstore", result
            );
            log.debug("[Smartstore][Price] 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][Smartstore][PriceUpdate] 처리 후 Thread.sleep(100ms)");

        } catch (Exception e) {
            log.error("[MQ][Smartstore][PriceUpdate] 메시지 폐기: {} | 원인: {}", msg, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

    @RabbitListener(queues = "stock-update-smartstore")
    public void handleStockUpdate(StockUpdateMessage msg) {
        log.info("[MQ][Smartstore][StockUpdate] 메시지 수신 - {}", msg);

        try {
            log.info("[SmartstoreAPI][Stock] 재고 변경 API 호출 시작 - channelId={}, stock={}", msg.getChannelId(), msg.getStock());

            Map<String, Object> result = smartstoreApiService.updateStock(msg.getChannelId(), msg.getStock());
            log.info("[SmartstoreAPI][Stock] API 응답 결과 - {}", result);
            log.info("[Smartstore][Stock] 상태 병합 시작 - batchId={}, productCode={}", msg.getBatchId(), msg.getProductCode());

            // 병합 전략 적용 (트랜잭션, select-for-update 등, 서비스에서 처리!)
            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "smartstore", result
            );
            log.debug("[Smartstore][Stock] 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][Smartstore][StockUpdate] 처리 후 Thread.sleep(100ms)");

        } catch (Exception e) {
            log.error("[MQ][Smartstore][StockUpdate] 메시지 폐기: {} | 원인: {}", msg, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }
}
