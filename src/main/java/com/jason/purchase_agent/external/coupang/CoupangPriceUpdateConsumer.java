package com.jason.purchase_agent.external.coupang;

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
public class CoupangPriceUpdateConsumer {

    private final CoupangApiService coupangApiService;
    private final ProcessStatusService processStatusService;

    @RabbitListener(queues = "price-update-coupang")
    public void handlePriceUpdate(PriceUpdateMessage msg) {
        log.info("[MQ][PriceUpdate] 메시지 수신 - {}", msg);
        try {
            log.info("[MQ][PriceUpdate] API 호출 시작 - channelId={}, salePrice={}", msg.getChannelId(), msg.getSalePrice());
            Map<String, Object> result = coupangApiService.updatePrice(msg.getChannelId(), msg.getSalePrice());
            log.info("[MQ][PriceUpdate] API응답 - result={}", result);

            log.info("[MQ][PriceUpdate] 결과 병합 시작 - batchId={}, productCode={}", msg.getBatchId(), msg.getProductCode());

            // 병합 전략 적용 (트랜잭션, select-for-update 등, 서비스에서 처리!)
            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "coupang", result
            );
            log.debug("[MQ][PriceUpdate] 결과 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][PriceUpdate] 처리 후 대기/sleep 완료");

        } catch (Exception e) {
            log.error("[MQ][PriceUpdate] 메시지 폐기 - {} | 원인: {}", msg, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }


    }

    @RabbitListener(queues = "stock-update-coupang")
    public void handleStockUpdate(StockUpdateMessage msg) {
        log.info("[MQ][StockUpdate] 메시지 수신 - {}", msg);

        try {
            log.info("[MQ][StockUpdate] API 호출 시작 - channelId={}, stock={}", msg.getChannelId(), msg.getStock());

            Map<String, Object> result = coupangApiService.updateStock(msg.getChannelId(), msg.getStock());
            log.info("[MQ][StockUpdate] API응답 - result={}", result);

            // 병합 전략 적용 (트랜잭션, select-for-update 등, 서비스에서 처리!)
            log.info("[MQ][StockUpdate] 결과 병합 시작 - batchId={}, productCode={}", msg.getBatchId(), msg.getProductCode());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "coupang", result
            );
            log.debug("[MQ][StockUpdate] 결과 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][StockUpdate] 처리 후 대기/sleep 완료");

        } catch (Exception e) {
            log.error("[MQ][StockUpdate] 메시지 폐기 - {} | 원인: {}", msg, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }
}
