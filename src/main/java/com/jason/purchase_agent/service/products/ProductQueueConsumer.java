package com.jason.purchase_agent.service.products;

import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.repository.jpa.ProductRepository;

import com.jason.purchase_agent.external.iherb.IherbProductCrawler;
import com.jason.purchase_agent.external.iherb.dto.IherbProductDto;
import com.jason.purchase_agent.external.coupang.CoupangApiService;
import com.jason.purchase_agent.external.smartstore.SmartstoreApiService;
import com.jason.purchase_agent.external.elevenst.ElevenstApiService;

import com.jason.purchase_agent.dto.products.BatchAutoPriceStockUpdateMessage;
import com.jason.purchase_agent.dto.products.ProductDto;

import static com.jason.purchase_agent.common.calculator.Calculator.calculateSalePrice;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ProductQueueConsumer {
    private final ProductRepository productRepo;
    private final CoupangApiService coupangApiService;
    private final SmartstoreApiService smartstoreApiService;
    private final ElevenstApiService elevenstApiService;

    // 각 상품별 처리 (일단, 로그는 나중에 남기자... 너무 복잡해!)
    @RabbitListener(queues = "products.batch_auto_price_stock_update_queue")
    public void receive(BatchAutoPriceStockUpdateMessage message) throws Exception {
        // 1. 메시지에서 상품 꺼내옴
        ProductDto productDto = message.getProductDto();
        System.out.println("productDto = " + productDto);
        // 2. 상품의 링크에서 상품ID 추출
        String productId = productRepo.findIherbProductIdFromLinkByCode(productDto.getCode());
        // 3. 상품ID를 인자로 넘겨 크롤링하여 상품정보를 json 반환 받음
        String productJson = IherbProductCrawler.crawlProductAsJson(productId);
        IherbProductDto iherbProductDto = IherbProductDto.fromJsonWithLinks(productJson);
        // 4. salePrice 계산
        Integer salePrice = calculateSalePrice(
                message.getMarginRate(),
                message.getCouponRate(),
                message.getMinMarginPrice(),
                message.getProductDto().getPackQty(),
                iherbProductDto);
        // 5. stock 계산
        Integer stock = iherbProductDto.getIsAvailableToPurchase() ? 498 : 0;
        // 6. Product 테이블 업데이트
        Product productEntity = productRepo.findById(productDto.getCode())
                .orElseThrow(() -> new Exception("Not Found : " + productDto.getCode()));
        productEntity.setSalePrice(salePrice);
        productEntity.setStock(stock);
        productRepo.save(productEntity);
        // 7. 쿠팡 업데이트
        coupangApiService.updatePriceStock(productDto.getVendorItemId(), salePrice, stock);
        // 8. 스마트스토어 업데이트
        smartstoreApiService.updatePriceStock(productDto.getOriginProductNo(), salePrice, stock);
        // 9. 11번가 업데이트
        elevenstApiService.updatePriceStock(productDto.getElevenstId(), salePrice, stock);
    }

}
