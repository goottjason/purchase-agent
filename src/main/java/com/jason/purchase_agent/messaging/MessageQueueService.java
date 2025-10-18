package com.jason.purchase_agent.messaging;

import com.jason.purchase_agent.dto.product_registration.ProductRegistrationMessage;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import com.jason.purchase_agent.dto.products.*;
import com.jason.purchase_agent.dto.products.update.CrawlAndUpdatePriceStockMessage;
import com.jason.purchase_agent.dto.products.update.ManualUpdateAllFieldsMessage;
import com.jason.purchase_agent.dto.products.update.ManualUpdatePriceStockMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageQueueService {
    private final RabbitTemplate rabbitTemplate;

    // ===== 1. 크롤링 + 가격/재고 업데이트 메시지 =====
    public void publishCrawlAndUpdatePriceStock(
            Integer marginRate, Integer couponRate, Integer minMarginPrice,
            ProductUpdateRequest request, String batchId
    ) {
        CrawlAndUpdatePriceStockMessage msg = CrawlAndUpdatePriceStockMessage.builder()
                .batchId(batchId)
                .productDto(request.getProductDto())
                .marginRate(marginRate)
                .couponRate(couponRate)
                .minMarginPrice(minMarginPrice)
                .build();

        rabbitTemplate.convertAndSend("crawl-and-update-price-stock", msg);
    }
    // ===== 2. 수동 가격/재고만 업데이트 메시지 =====
    public void publishManualUpdatePriceStock(
            ProductUpdateRequest request, String batchId
    ) {
        ManualUpdatePriceStockMessage msg = ManualUpdatePriceStockMessage.builder()
                .batchId(batchId)
                .productDto(request.getProductDto())
                .build();

        rabbitTemplate.convertAndSend("manual-update-price-stock", msg);
    }

    // ===== 3. 수동 전체 필드 업데이트 메시지 =====
    public void publishManualUpdateAllFields(
            ProductUpdateRequest request, String batchId
    ) {
        ManualUpdateAllFieldsMessage msg = ManualUpdateAllFieldsMessage.builder()
                .batchId(batchId)
                .productDto(request.getProductDto())
                .build();

        rabbitTemplate.convertAndSend("manual-update-all-fields", msg);
    }






























    public void publishRegisterProductToCoupang(
            String batchId, ProductRegistrationRequest request, Integer totalProductCount) {
        ProductRegistrationMessage msg = ProductRegistrationMessage.builder()
                .batchId(batchId).request(request).totalProductCount(totalProductCount).build();
        rabbitTemplate.convertAndSend("register-product-to-coupang", msg);
    }

    public void publishRegisterProductToSmartstore(
            String batchId, ProductRegistrationRequest request, Integer totalProductCount) {
        ProductRegistrationMessage msg = ProductRegistrationMessage.builder()
                .batchId(batchId).request(request).totalProductCount(totalProductCount).build();
        rabbitTemplate.convertAndSend("register-product-to-smartstore", msg);
    }

    public void publishRegisterProductToElevenst(
            String batchId, ProductRegistrationRequest request, Integer totalProductCount) {
        ProductRegistrationMessage msg = ProductRegistrationMessage.builder()
                .batchId(batchId).request(request).totalProductCount(totalProductCount).build();
        rabbitTemplate.convertAndSend("register-product-to-elevenst", msg);
    }
    public void publishRegisterProductToCafe(
            String batchId, ProductRegistrationRequest request, Integer totalProductCount) {
        ProductRegistrationMessage msg = ProductRegistrationMessage.builder()
                .batchId(batchId).request(request).totalProductCount(totalProductCount).build();
        rabbitTemplate.convertAndSend("register-product-to-cafe", msg);
    }



    // 가격 변경 메시지 발행
    public void publishPriceUpdateChannel(
            String batchId, String channel, ProductDto productDto
    ) {
        String channelId = getChannelIdForChannel(channel, productDto);
        PriceUpdateChannelMessage msg = PriceUpdateChannelMessage.builder()
                .batchId(batchId)
                .channel(channel)
                .channelId1(channelId)
                .channelId2(null)
                .productCode(productDto.getCode())
                .salePrice(productDto.getSalePrice())
                .marginRate(productDto.getMarginRate())
                .build();

        rabbitTemplate.convertAndSend("price-update-" + channel, msg);
    }

    // 재고 변경 메시지 발행
    public void publishStockUpdateChannel(
            String batchId, String channel, ProductDto productDto
    ) {
        String channelId = getChannelIdForChannel(channel, productDto);
        StockUpdateChannelMessage msg = StockUpdateChannelMessage.builder()
                .channel(channel)
                .channelId1(channelId)
                .channelId2("cafe".equals(channel) ? productDto.getCafeOptCode() : null)
                .batchId(batchId)
                .productCode(productDto.getCode())
                .stock(productDto.getStock())
                .build();

        rabbitTemplate.convertAndSend("stock-update-" + channel, msg);
    }

    // 채널명에 따른 채널 상품 ID 조회 헬퍼 메서드
    private String getChannelIdForChannel(String channel, ProductDto productDto) {
        String channelId;
        switch (channel) {
            case "coupang":
                channelId = productDto.getVendorItemId();
                break;
            case "smartstore":
                channelId = productDto.getOriginProductNo();
                break;
            case "elevenst":
                channelId = productDto.getElevenstId();
                break;
            case "cafe":
                channelId = productDto.getCafeNo();
                break;
            default:
                channelId = null;
        }
        return channelId;
    }
}
