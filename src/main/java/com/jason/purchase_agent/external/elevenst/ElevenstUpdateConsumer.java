package com.jason.purchase_agent.external.elevenst;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ElevenstUpdateConsumer {
    private final ElevenstApiService elevenstApiService;
    private final ProcessStatusService processStatusService;

    @RabbitListener(queues = "price-update-elevenst", concurrency = "1")
    public void handlePriceUpdate(PriceUpdateChannelMessage msg) {

        try {
            Integer salePrice = msg.getSalePrice();
            Double marginRate = msg.getMarginRate();
            salePrice = (int) (Math.ceil((salePrice * (100 - 18.5 - marginRate) / (100 - 17.5 - marginRate))/100.0) * 100);

            String responseXml = elevenstApiService.updatePrice(msg.getChannelId1(), salePrice);

            XmlMapper xmlMapper = new XmlMapper();
            Map<String, Object> xmlResult = xmlMapper.readValue(responseXml, Map.class);

            String code = String.valueOf(xmlResult.getOrDefault("resultCode", ""));
            String returnedMessage = String.valueOf(xmlResult.getOrDefault("message", "상세 메시지 없음"));
            String normalizedMessage = returnedMessage.trim();

            // 성공 조건: resultCode == 200 || (가격 동일시 보통 "동일한 가격"류 메시지가 포함)
            boolean findSuccess = "200".equals(code)
                    || normalizedMessage.contains("변경될 판매가격이 같습니다."); // 11번가의 특유 메시지

            Map<String, Object> channelResult = new HashMap<>();
            if (findSuccess) {
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", String.format("가격: %,d원, ID: %s", salePrice, msg.getChannelId1()));
                log.info("[{}][Elevenst][Price] 성공 (elevenstId={}, salePrice={})",
                        msg.getProductCode(), msg.getChannelId1(), salePrice);
            } else {
                channelResult.put("status", "FAIL");
                channelResult.put("message", code + " : " + returnedMessage);
                log.error("[{}][Elevenst][Price] 실패 (responseXml={})",
                        msg.getProductCode(), responseXml);
            }

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "elevenst", channelResult
            );

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (Exception e) {

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "elevenst", channelResult
            );
            log.error("[{}][Elevenst][Price] 실패 (e.getMessage()={})", msg.getProductCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

    @RabbitListener(queues = "stock-update-elevenst", concurrency = "1")
    public void handleStockUpdate(StockUpdateChannelMessage msg) {
        try {
            String responseXml = elevenstApiService.updateStock(msg.getChannelId1(), msg.getStock());
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
                channelResult.put("message", String.format("재고: %,d개, ID: %s", msg.getStock(), msg.getChannelId1()));
                log.info("[{}][Elevenst][Stock] 성공 (elevenstId={}, stock={})",
                        msg.getProductCode(), msg.getChannelId1(), msg.getStock());
            } else {
                channelResult.put("status", "FAIL");
                channelResult.put("message", code + " : " + returnedMessage);
                log.error("[{}][Elevenst][Stock] 실패 (responseXml={})",
                        msg.getProductCode(), responseXml);
            }

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "elevenst", channelResult
            );

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (Exception e) {

            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            processStatusService.mergeChannelResult(
                    msg.getBatchId(), msg.getProductCode(), "elevenst", channelResult
            );

            log.error("[{}][Elevenst][Stock] 실패 (e.getMessage()={})", msg.getProductCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }


}
