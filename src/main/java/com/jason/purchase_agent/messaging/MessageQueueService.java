package com.jason.purchase_agent.messaging;

import com.jason.purchase_agent.dto.autoupdate.AutoUpdateMessage;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationMessage;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRetryMessage;
import com.jason.purchase_agent.dto.products.PriceUpdateMessage;
import com.jason.purchase_agent.dto.products.StockUpdateMessage;
import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.entity.ProductChannelMapping;
import com.jason.purchase_agent.dto.products.BatchAutoPriceStockUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageQueueService {
    private final AmqpTemplate amqpTemplate;
    private final RabbitTemplate rabbitTemplate;

    private static final String PRODUCT_REGISTRATION_QUEUE = "product_registration_queue";
    private static final String PRODUCT_REGISTRATION_RETRY_QUEUE = "product_registration_retry_queue";
    private static final String PRODUCTS_BATCH_AUTO_PRICE_STOCK_UPDATE_QUEUE =
            "products.batch_auto_price_stock_update_queue";
    private static final String PRODUCTS_BATCH_MANUAL_PRICE_STOCK_UPDATE_QUEUE =
            "products.batch_manual_price_stock_update_queue";

    // 가격 변경 메시지 발행
    public void publishPriceUpdate(
            String channel, Product product, ProductChannelMapping mapping, String batchId
    ) {
        // 채널 매핑 정보에 해당 채널의 상품 ID가 없으면 리턴
        String channelId = getChannelIdForChannel(channel, mapping);
        if (channelId == null) {
            log.warn("[MQ][PRICE] 발행 실패 - 채널ID 없음, channel={}, product={}, batchId={}", channel, product.getCode(), batchId);

            return;
        }
        // 메시지 생성
        PriceUpdateMessage msg = PriceUpdateMessage.builder()
                .channel(channel)
                .channelId(channelId)
                .batchId(batchId)
                .productCode(product.getCode())
                .salePrice(product.getSalePrice())
                .build();
        log.info("[MQ][PRICE] 메시지 발행 - queue=price-update-{}, message={}, batchId={}", channel, msg, batchId);

        // 메시지 발행
        rabbitTemplate.convertAndSend("price-update-" + channel, msg);
    }

    // 재고 변경 메시지 발행
    public void publishStockUpdate(
            String channel, Product product, ProductChannelMapping mapping, String batchId
    ) {
        // 채널 매핑 정보에 해당 채널의 상품 ID가 없으면 리턴
        String channelId = getChannelIdForChannel(channel, mapping);
        if (channelId == null) {
            log.warn("[MQ][STOCK] 발행 실패 - 채널ID 없음, channel={}, product={}, batchId={}", channel, product.getCode(), batchId);

            return;
        }
        // 메시지 생성 및 발행
        StockUpdateMessage msg = StockUpdateMessage.builder()
                .channel(channel)
                .channelId(channelId)
                .batchId(batchId)
                .productCode(product.getCode())
                .stock(product.getStock())
                .build();
        log.info("[MQ][STOCK] 메시지 발행 - queue=stock-update-{}, message={}, batchId={}", channel, msg, batchId);

        rabbitTemplate.convertAndSend("stock-update-" + channel, msg);
    }
    // 채널명에 따른 채널 상품 ID 조회 헬퍼 메서드
    private String getChannelIdForChannel(String channel, ProductChannelMapping mapping) {
        String channelId;
        switch (channel) {
            case "coupang":
                channelId = mapping.getSellerProductId();
                break;
            case "smartstore":
                channelId = mapping.getOriginProductNo();
                break;
            case "elevenst":
                channelId = mapping.getElevenstId();
                break;
            default:
                channelId = null;
        }
        log.info("[MQ] 채널ID 매핑 결과 - channel={}, channelId={}", channel, channelId);
        return channelId;
    }














    public void publishProductRegistration(ProductRegistrationMessage message) {
        amqpTemplate.convertAndSend(PRODUCT_REGISTRATION_QUEUE, message);
    }
    public void publishProductRegistrationRetry(ProductRegistrationRetryMessage message) {
        amqpTemplate.convertAndSend(PRODUCT_REGISTRATION_RETRY_QUEUE, message);
    }

    public void publishProductsBatchAutoPriceStockUpdate(
            BatchAutoPriceStockUpdateMessage message
    ) {
        amqpTemplate.convertAndSend(PRODUCTS_BATCH_AUTO_PRICE_STOCK_UPDATE_QUEUE, message);
    }

    public void publishProductsBatchManualPriceStockUpdate(AutoUpdateMessage message) {
        // AmqpTemplate을 통해 지정한 큐명에 메시지를 발행(serialize하여 MQ로 쏨)
        amqpTemplate.convertAndSend(PRODUCTS_BATCH_MANUAL_PRICE_STOCK_UPDATE_QUEUE, message);
        // (Spring은 직렬화 자동 처리, 메시지 = 자바 객체 → MQ에 들어감)
        // 큐에 쌓이고 나면 오토MQ Consumer에서 알아서 병렬로 처리 시작됨
    }

    /*public void publishPriceUpdate(String channel, String productCode, String channelProductId, Integer newPrice) {
        ChannelUpdateMessage message = ChannelUpdateMessage.builder()
                .type("PRICE_UPDATE")
                .channel(channel)
                .productCode(productCode)
                .channelProductId(channelProductId)
                .newPrice(newPrice)
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend("product.updates", channel + ".price", message);
    }

    public void publishStockUpdate(String channel, String productCode, String channelProductId, Integer newStock) {
        ChannelUpdateMessage message = ChannelUpdateMessage.builder()
                .type("STOCK_UPDATE")
                .channel(channel)
                .productCode(productCode)
                .channelProductId(channelProductId)
                .newStock(newStock)
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend("product.updates", channel + ".stock", message);
    }*/

    /**
     * 상품의 가격/재고 변경시, 연동되어 있는 판매 채널에 가격/재고 변경 메시지를 전파하는 메서드
     *
     * <주요 동작 흐름>
     * 1. 상품과 연결된 채널 매핑 정보(ProductChannelMapping)가 없으면 중단/리턴
     * 2. 가격이 변경된 경우: 각 채널별로 가격 변경 메시지를 전송
     *    - 쿠팡, 스마트스토어, 11번가 채널 중 vendorId/상품ID가 등록된 채널에만 전파
     * 3. 재고가 변경된 경우: 각 채널별로 재고 변경 메시지를 전송
     *    - 위와 마찬가지로 채널별로 대상 ID가 등록된 경우에만 전파
     *
     * @param product       변경 대상 상품 엔티티 (반영된 현재값)
     * @param oldSalePrice  변경 전 판매 가격
     * @param oldStock      변경 전 재고 수량
     */
    /*public void publishChannelUpdateMessages(
            Product product, Integer oldSalePrice, Integer oldStock
    ) {
        // 1. 연결된 채널 매핑 정보 조회. 없으면 아무것도 하지 않고 return
        ProductChannelMapping mapping = product.getProductChannelMapping();
        if (mapping == null) return;

        // 가격 변경 메시지 (가격이 변경된 경우에만)
        if (priceChanged) {
            if (mapping.getVendorItemId() != null) {
                publishPriceUpdate("coupang", product.getCode(), mapping.getVendorItemId(), product.getSalePrice());
            }
            if (mapping.getSmartstoreId() != null) {
                publishPriceUpdate("smartstore", product.getCode(), mapping.getSmartstoreId(), product.getSalePrice());
            }
            if (mapping.getElevenstId() != null) {
                publishPriceUpdate("elevenst", product.getCode(), mapping.getElevenstId(), product.getSalePrice());
            }
        }

        // 재고 변경 메시지 (재고가 변경된 경우에만)
        if (stockChanged) {
            if (mapping.getVendorItemId() != null) {
                publishStockUpdate("coupang", product.getCode(), mapping.getVendorItemId(), product.getStock());
            }
            if (mapping.getSmartstoreId() != null) {
                publishStockUpdate("smartstore", product.getCode(), mapping.getSmartstoreId(), product.getStock());
            }
            if (mapping.getElevenstId() != null) {
                publishStockUpdate("elevenst", product.getCode(), mapping.getElevenstId(), product.getStock());
            }
        }
    }*/




}
