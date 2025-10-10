package com.jason.purchase_agent.external.coupang;

import com.fasterxml.jackson.databind.JsonNode;
import com.jason.purchase_agent.dto.products.VendorItemIdSyncMessage;
import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.entity.ProductChannelMapping;
import com.jason.purchase_agent.messaging.MessageQueueService;
import com.jason.purchase_agent.repository.jpa.ProductChannelMappingRepository;
import com.jason.purchase_agent.repository.jpa.ProductRepository;
import com.jason.purchase_agent.service.process_status.ProcessStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoupangInfoConsumer {
    private final CoupangApiService coupangApiService;
    private final ProductChannelMappingRepository mappingRepository;
    private final MessageQueueService messageQueueService;
    private final ProductRepository productRepository;
    private final ProcessStatusService processStatusService;

    /*@RabbitListener(queues = "vendoritemid-sync-coupang")
    public void handleVendorItemIdSync(VendorItemIdSyncMessage msg) {
        log.info("[MQ][VENDOR_ID_SYNC] 컨슈머 메시지 수신 - batchId={}, productCode={}",
                msg.getBatchId(), msg.getProductCode());
        try {
            // 1. sellerProductId로 vendorItemId 조회하여 responseJson 반환
            String responseJson = coupangApiService.findProductInfo(msg.getSellerProductId());
            JsonNode root = objectMapper.readTree(responseJson);

            // 기본 성공 플래그
            boolean findSuccess = false;
            String vendorItemId = null;

            // 1. 에러 응답 분기
            JsonNode dataNode = root.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                String errorMsg = root.path("message").asText("상세 메시지 없음");
                // 실패/에러 응답을 channelResult map으로 준비
                Map<String, Object> channelResult = new HashMap<>();
                channelResult.put("status", "FAIL");
                channelResult.put("message", errorMsg);

                log.error("[MQ][VENDOR_ID_SYNC] Coupang 상품조회 실패(에러 메시지) - productCode={}, msg={}", msg.getProductCode(), errorMsg);

                // mergeChannelResult로 통합관리 (실패, 에러도 동일 구조로)
                processStatusService.mergeChannelResult(
                        msg.getBatchId(), msg.getProductCode(), "coupang", channelResult
                );
                // 필요에 따라 DLQ 전송/재시도 로직 추가
                return;
            }

            // 2. 정상 조회 케이스만 처리
            JsonNode itemsNode = dataNode.path("items");
            if (itemsNode.isArray() && itemsNode.size() > 0) {
                JsonNode item = itemsNode.get(0);
                vendorItemId = item.path("vendorItemId").asText();
                if (vendorItemId != null && !vendorItemId.isBlank()) {
                    findSuccess = true;
                }
            }

            if (!findSuccess) {
                log.error("[MQ][VENDOR_ID_SYNC] vendorItemId 조회 실패 (정상 data.items 없음) - productCode={}, sellerProductId={}",
                        msg.getProductCode(), msg.getSellerProductId());

                processStatusService.upsertProcessStatus(
                        msg.getBatchId(), msg.getProductCode(), null,
                        "VENDOR_ID_SYNC", "FAILED", "[쿠팡상품조회] vendorItemId 조회 실패(data.items 없음/형식오류)"
                );
                return;
            }

            // 3. DB 저장 및 후속 메시지 (정상시)
            ProductChannelMapping mapping = mappingRepository.findById(msg.getProductCode())
                    .orElseThrow(() -> new IllegalArgumentException("매핑정보를 찾을 수 없음: "+msg.getProductCode()));
            mapping.setVendorItemId(vendorItemId);
            mappingRepository.save(mapping);

            Product product = productRepository.findById(msg.getProductCode())
                    .orElseThrow(() -> new IllegalArgumentException(msg.getProductCode() + " 상품을 찾을 수 없습니다."));

            log.info("[MQ][VENDOR_ID_SYNC] vendorItemId 저장 성공 - productCode={}, vendorItemId={}",
                    msg.getProductCode(), vendorItemId);

            // 후속 Price/Stock 메시지 발행
            if (msg.isPriceChanged()) {
                messageQueueService.publishPriceUpdate("coupang", product, mapping, msg.getBatchId());
            }
            if (msg.isStockChanged()) {
                messageQueueService.publishStockUpdate("coupang", product, mapping, msg.getBatchId());
            }

            log.info("[MQ][VENDOR_ID_SYNC] 가격/재고 후속 메시지 발행 완료 - productCode={}, batchId={}", msg.getProductCode(), msg.getBatchId());

        } catch (Exception e) {
            log.error("[MQ][VENDOR_ID_SYNC] 처리 중 예외! - batchId={}, productCode={}, 원인={}", msg.getBatchId(), msg.getProductCode(), e.getMessage(), e);
            // 필요하면 재시도/정책 처리
        }
    }*/
}
