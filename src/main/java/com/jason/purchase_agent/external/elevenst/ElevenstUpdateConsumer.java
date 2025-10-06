package com.jason.purchase_agent.external.elevenst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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

            XmlMapper xmlMapper = new XmlMapper();
            Map<String, Object> xmlResult = xmlMapper.readValue(responseXml, Map.class);

            String code = String.valueOf(xmlResult.getOrDefault("resultCode", ""));
            String returnedMessage = String.valueOf(xmlResult.getOrDefault("message", "상세 메시지 없음"));
            String normalizedMessage = returnedMessage.trim();

            // 성공 조건: resultCode == 200 || (가격 동일시 보통 "동일한 가격"류 메시지가 포함)
            boolean findSuccess = "200".equals(code)
                    || normalizedMessage.contains("동일") // ex: "요청하신 가격으로 이미 설정되어 있습니다."
                    || normalizedMessage.contains("변경된 값이 없습니다.") // 11번가의 특유 메시지
                    || normalizedMessage.contains("같은 가격"); // 기타 실질 성공 메시지 패턴 추가 가능

            Map<String, Object> channelResult = new HashMap<>();
            if (findSuccess) {
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", String.format("가격변경완료(가격: %,d원, ID: %s)", msg.getSalePrice(), msg.getChannelId()));
            } else {
                channelResult.put("status", "FAIL");
                channelResult.put("message", code + " : " + returnedMessage);
            }

            log.info("[Elevenst][PriceUpdate] 상태 병합 시작 - batchId={}, productCode={}, status={}",
                    msg.getBatchId(), msg.getProductCode(), channelResult.get("status"));

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

            String responseXml = elevenstApiService.updateStock(msg.getChannelId(), msg.getStock());
            XmlMapper xmlMapper = new XmlMapper();
            Map<String, Object> xmlResult = xmlMapper.readValue(responseXml, Map.class);

            String code = String.valueOf(xmlResult.getOrDefault("resultCode", ""));
            String returnedMessage = String.valueOf(xmlResult.getOrDefault("message", "상세 메시지 없음"));
            String normalizedMessage = returnedMessage.trim();

            // 성공: resultCode == 200 또는, "판매중지", "이미 판매중", "해제" 등 11번가의 실질적 성공 메시지도 포함
            boolean findSuccess = "200".equals(code)
                    || normalizedMessage.contains("판매중지")      // 예시: "판매중지 상품만 판매중지해제 가능"
                    || normalizedMessage.contains("이미 판매중")  // 예시: "이미 판매중인 상품입니다"
                    || normalizedMessage.contains("이미 상태")    // 예시: "이미 판매중인 상태입니다"
                    || normalizedMessage.contains("해제");        // 예시: "판매중지 해제" 등

            Map<String, Object> channelResult = new HashMap<>();
            if (findSuccess) {
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", String.format("재고변경완료(재고: %,d개, ID: %s)", msg.getStock(), msg.getChannelId()));
            } else {
                channelResult.put("status", "FAIL");
                channelResult.put("message", code + " : " + returnedMessage);
            }

            log.info("[Elevenst][StockUpdate] 상태 병합 시작 - batchId={}, productCode={}, status={}",
                    msg.getBatchId(), msg.getProductCode(), channelResult.get("status"));

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "elevenst", channelResult
            );
            log.debug("[Elevenst][Stock] 병합 완료");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            log.debug("[MQ][Elevenst][StockUpdate] 처리 후 Thread.sleep(100ms)");

        } catch (Exception e) {
            log.error("[MQ][Elevenst][StockUpdate] 처리 중 예외! - batchId={}, productCode={}, 원인={}",
                    msg.getBatchId(), msg.getProductCode(), e.getMessage(), e);

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
