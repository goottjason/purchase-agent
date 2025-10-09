package com.jason.purchase_agent.service.autoupdate;

import com.jason.purchase_agent.dto.autoupdate.AutoUpdateMessage;
import com.jason.purchase_agent.dto.products.CrawlAndUpdateProductMessage;
import com.jason.purchase_agent.dto.products.ProductDto;
import com.jason.purchase_agent.external.iherb.dto.IherbProductDto;
import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.repository.jpa.ProcessStatusRepository;
import com.jason.purchase_agent.repository.jpa.ProductRepository;
import com.jason.purchase_agent.external.coupang.CoupangApiService;
import com.jason.purchase_agent.external.elevenst.ElevenstApiService;
import com.jason.purchase_agent.external.smartstore.SmartstoreApiService;
import com.jason.purchase_agent.external.iherb.IherbProductCrawler;
import com.jason.purchase_agent.service.products.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.jason.purchase_agent.common.calculator.Calculator.calculateSalePrice;

/**
 * MQ Consumer 역할 (자동 가격/재고 업데이트 메시지 처리)
 */
@Component
@RequiredArgsConstructor
public class AutoUpdateQueueConsumer {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProcessStatusRepository psr;
    private final CoupangApiService coupangApiService;
    private final SmartstoreApiService smartstoreApiService;
    private final ElevenstApiService elevenstApiService;
    private final IherbProductCrawler iherbProductCrawler;


    @RabbitListener(queues = "crawl-and-update-product")
    public void handleCrawlAndUpdateProduct(CrawlAndUpdateProductMessage msg) {

        // (1) 크롤링하여 request 내부의 productDto에 가격/재고 세팅

        ProductDto productDto = msg.getRequest().getProductDto();
        Integer marginRate = msg.getMarginRate();
        Integer couponRate = msg.getCouponRate();
        Integer minMarginPrice = msg.getMinMarginPrice();
        String batchId = msg.getBatchId();

        String prodId = productRepository.findIherbProductIdFromLinkByCode(productDto.getLink());
        String productJson = iherbProductCrawler.crawlProductAsJson(prodId);
        IherbProductDto iherbProductDto = IherbProductDto.fromJsonWithLinks(productJson);
        Integer salePrice = calculateSalePrice(
                marginRate, couponRate, minMarginPrice, productDto, iherbProductDto);
        Integer stock = Boolean.TRUE.equals(iherbProductDto.getIsAvailableToPurchase()) ? 500 : 0;

        productDto.setSalePrice(salePrice);
        productDto.setStock(stock);

        // (2)
        productService.updateSingleProductInBatch(
                batchId, productDto.getCode(), productDto, true, true, false);
    }







    /**
     * MQ에서 product.auto_update로 메시지가 도착하면 자동으로 실행됨!
     * @param msg 배치ID, 공급업체코드, 상품코드, 마진율 등 포함된 메시지 DTO
     */
    @RabbitListener(queues = "product.auto_update")
    public void receive(AutoUpdateMessage msg) {

        ProductDto prod = msg.getProductDto();

        // 1. 작업 시작 로그 남기기
        psr.insert(msg.getBatchId(), prod.getCode(), prod.getTitle(),
                "PROCESSING", "크롤링 준비", "[" + prod.getCode() + "] 크롤링 준비");

        try {
            // 2. (1) 링크에서 상품ID 추출하여 크롤링하여 json 반환
            String prodId = productRepository.findIherbProductIdFromLinkByCode(prod.getLink());
            String prodJson = IherbProductCrawler.crawlProductAsJson(prodId);

            // String 형태의 json -> IherbProductDto로 변환
            IherbProductDto iherbDto = IherbProductDto.fromJsonWithLinks(prodJson);

            Integer salePrice = calculateSalePrice(
                    msg.getMarginRate(),
                    msg.getCouponRate(),
                    msg.getMinMarginPrice(),
                    msg.getProductDto(),
                    iherbDto);
            Boolean isStock = iherbDto.getIsAvailableToPurchase();
            Integer stock = isStock ? 498 : 0;

            // 로그 업데이트

            // 2. (2) Product 테이블 반영
            Product productEntity = productRepository.findById(prod.getCode())
                    .orElseThrow(() -> new IllegalArgumentException("Not Found : " + prod.getCode()));

            if (productEntity != null) {
                productEntity.setSalePrice(salePrice);
                productEntity.setStock(stock);
                productRepository.save(productEntity);

                // 로그 업데이트
                psr.insert(msg.getBatchId(), prod.getCode(), prod.getTitle(),
                        "PROCESSING", "DB 상품정보 수정", "가격:" + salePrice + ", 재고:" + stock + " 반영");
            } else {
                psr.insert(msg.getBatchId(), prod.getCode(), prod.getTitle(),
                        "FAILED", "DB 상품 없음", "해당 상품코드로 Product 엔티티를 찾지 못함");
            }
            // 2. (3) 쿠팡에 재고/가격 업데이트
            coupangApiService.updatePriceStock(prod.getVendorItemId(), salePrice, stock);
            // 로그 업데이트
            psr.insert(msg.getBatchId(), prod.getCode(), prod.getTitle(),
                    "PROCESSING", "쿠팡 API 반영", "쿠팡 가격&재고 반영 완료");

            // 2. (4) 스마트스토어에 재고/가격 업데이트
            smartstoreApiService.updatePriceStock(prod.getOriginProductNo(), salePrice, stock);
            // 로그 업데이트
            psr.insert(msg.getBatchId(), prod.getCode(), prod.getTitle(),
                    "PROCESSING", "스마트스토어 API 반영", "스마트스토어 가격&재고 반영 완료");

            // 2. (5) 11번가에 재고/가격 업데이트
            elevenstApiService.updatePriceStock(prod.getElevenstId(), salePrice, stock);
            // 로그 업데이트(완료)
            psr.insert(msg.getBatchId(), prod.getCode(), prod.getTitle(),
                    "PROCESSING", "11번가 API 반영", "11번가 가격&재고 반영 완료");

            // 2. (6) 최종 완료 로그
            psr.insert(msg.getBatchId(), prod.getCode(), prod.getTitle(),
                    "DONE", "상품 업데이트 완료", "전체 채널 가격&재고 동기화 완료");

        } catch(Exception e) {
            // 예외 발생시 실패 로그
            psr.insert(msg.getBatchId(), prod.getCode(), prod.getTitle(),
                    "FAILED", "오류발생", "에러: " + e.getMessage());
        }
    }



}
