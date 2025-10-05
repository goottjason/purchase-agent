package com.jason.purchase_agent.external.coupang;

import com.fasterxml.jackson.databind.JsonNode;
import com.jason.purchase_agent.dto.products.VendorItemIdSyncMessage;
import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.entity.ProductChannelMapping;
import com.jason.purchase_agent.messaging.MessageQueueService;
import com.jason.purchase_agent.repository.jpa.ProductChannelMappingRepository;
import com.jason.purchase_agent.repository.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoupangInfoConsumer {
    private final CoupangApiService coupangApiService;
    private final ProductChannelMappingRepository mappingRepository;
    private final MessageQueueService messageQueueService;
    private final ProductRepository productRepository;

    @RabbitListener(queues = "vendoritemid-sync-coupang")
    public void handleVendorItemIdSync(VendorItemIdSyncMessage msg) {
        log.info("[MQ][VENDOR_ID_SYNC] 컨슈머 메시지 수신 - batchId={}, productCode={}", msg.getBatchId(), msg.getProductCode());
        try {
            // 1. sellerProductId로 vendorItemId 조회
            String responseJson = coupangApiService.findProductInfo(msg.getSellerProductId());
            String vendorItemId = null;
            JsonNode root = objectMapper.readTree(responseJson);
            // 예시: data.items[0].vendorItemId 기준 (옵션이 복수면 반복/선택 가능)
            JsonNode itemsNode = root.path("data").path("items");
            if (itemsNode.isArray() && itemsNode.size() > 0) {
                JsonNode item = itemsNode.get(0); // 필요시 더 풍부한 로직!
                vendorItemId = item.path("vendorItemId").asText();
            }
            if (vendorItemId == null || vendorItemId.isBlank()) {
                log.error("[MQ][VENDOR_ID_SYNC] vendorItemId 조회 실패 - productCode={}, sellerProductId={}",
                        msg.getProductCode(), msg.getSellerProductId());
                // 재시도/실패 큐/알람 등 처리
                return;
            }

            // 2. DB에 저장 (매핑)
            ProductChannelMapping mapping = mappingRepository.findById(msg.getProductCode())
                    .orElseThrow(() -> new IllegalArgumentException("매핑정보를 찾을 수 없음: "+msg.getProductCode()));
            mapping.setVendorItemId(vendorItemId);
            mappingRepository.save(mapping);

            Product product = productRepository.findById(msg.getProductCode())
                    .orElseThrow(() -> new IllegalArgumentException(msg.getProductCode() + " 상품을 찾을 수 없습니다."));

            log.info("[MQ][VENDOR_ID_SYNC] vendorItemId 저장 성공 - productCode={}, vendorItemId={}",
                    msg.getProductCode(), vendorItemId);

            // 3. 후속 Price/Stock 메시지 발행
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
    }
}
