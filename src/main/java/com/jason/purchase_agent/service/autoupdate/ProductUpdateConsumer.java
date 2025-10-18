package com.jason.purchase_agent.service.autoupdate;

import com.jason.purchase_agent.dto.products.update.CrawlAndUpdatePriceStockMessage;
import com.jason.purchase_agent.dto.products.ModifyAndUpdatePriceStockMore;
import com.jason.purchase_agent.dto.products.ProductDto;
import com.jason.purchase_agent.dto.products.update.ManualUpdateAllFieldsMessage;
import com.jason.purchase_agent.dto.products.update.ManualUpdatePriceStockMessage;
import com.jason.purchase_agent.dto.products.update.PriceStockChangeInfo;
import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.enums.JobType;
import com.jason.purchase_agent.service.process_status.ProcessStatusService;
import com.jason.purchase_agent.service.products.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * MQ Consumer 역할 (자동 가격/재고 업데이트 메시지 처리)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductUpdateConsumer {

    private final ProductService productService;
    private final ProcessStatusService pss;


    @RabbitListener(queues = "crawl-and-update-price-stock", concurrency = "1")
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleCrawlAndUpdatePriceStock(
            CrawlAndUpdatePriceStockMessage msg
    ) {
        try {
            // 메세지 필드 변수화
            ProductDto productDto = msg.getProductDto();
            String batchId = msg.getBatchId();

            // (1) 크롤링하여 가격/재고 세팅
            productService.crawlAndSetPriceStock(
                    productDto, msg.getMarginRate(), msg.getCouponRate(), msg.getMinMarginPrice());
            recordProcessSuccess(batchId, productDto.getCode(), JobType.CRAWL_AND_UPDATE_PRICE_STOCK, "UPDATE_PRODUCT_CRAWL", "크롤링 성공");

            // (2) DB 저장
            saveProduct(batchId, productDto);

            // (3) 채널 발행
            publishChannelUpdates(batchId, productDto, true, true);

        } catch (Exception e) {
            handleConsumerError(msg.getBatchId(), msg.getProductDto().getCode(), JobType.CRAWL_AND_UPDATE_PRICE_STOCK,
                    "크롤링 업데이트 실패", e);
        }
    }

    // ===== 2. 수동 가격/재고만 업데이트 처리 =====
    @RabbitListener(queues = "manual-update-price-stock", concurrency = "1")
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleManualUpdatePriceStock(ManualUpdatePriceStockMessage msg) {
        try {
            // 메세지 필드 변수화
            ProductDto productDto = msg.getProductDto();
            String batchId = msg.getBatchId();

            // (1) 수동 입력값 검증
            // validatePriceStock(productDto);
            recordProcessSuccess(batchId, productDto.getCode(), JobType.MANUAL_UPDATE_PRICE_STOCK, "UPDATE_PRODUCT_VALIDATE", "검증 성공");

            // (2) DB 저장
            saveProduct(batchId, productDto);

            // (3) 채널 발행
            publishChannelUpdates(batchId, productDto, true, true);

        } catch (Exception e) {
            handleConsumerError(msg.getBatchId(), msg.getProductDto().getCode(), JobType.MANUAL_UPDATE_PRICE_STOCK,
                    "수동 가격/재고 업데이트 실패", e);
        }
    }

    // ===== 3. 수동 전체 필드 업데이트 처리 =====
    @RabbitListener(queues = "manual-update-all-fields", concurrency = "1")
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleManualUpdateAllFields(ManualUpdateAllFieldsMessage msg) {
        try {
            // 메세지 필드 변수화
            ProductDto productDto = msg.getProductDto();
            String batchId = msg.getBatchId();

            // (1) 전체 필드 검증
            // validateAllFields(productDto);
            recordProcessSuccess(batchId, productDto.getCode(), JobType.MANUAL_UPDATE_ALL_FIELDS, "UPDATE_PRODUCT_VALIDATE", "검증 성공");

            // (2) DB 저장
            saveProduct(batchId, productDto);

            // (3) 채널 발행 (가격/재고 변경된 경우에만 채널 업데이트 발행)
            // 변경 사항 감지 (DB 저장 전 기존 데이터와 비교)
            PriceStockChangeInfo changeInfo = detectPriceStockChanges(productDto);

            // 가격/재고가 변경된 경우에만 채널 업데이트 발행
            if (changeInfo.isPriceChanged() || changeInfo.isStockChanged()) {
                publishChannelUpdates(batchId, productDto,
                        changeInfo.isPriceChanged(), changeInfo.isStockChanged());
                log.info("[{}] 가격/재고 변경 감지 - 가격변경:{}, 재고변경:{}",
                        productDto.getCode(), changeInfo.isPriceChanged(), changeInfo.isStockChanged());
            } else {
                log.info("[{}] 가격/재고 변경 없음 - 채널 업데이트 스킵", productDto.getCode());
                recordProcessSuccess(batchId, productDto.getCode(), JobType.MANUAL_UPDATE_ALL_FIELDS,
                        "UPDATE_PRODUCT_PUBLISH", "가격/재고 변경 없음 - 채널 업데이트 스킵");
            }

        } catch (Exception e) {
            handleConsumerError(msg.getBatchId(), msg.getProductDto().getCode(), JobType.MANUAL_UPDATE_ALL_FIELDS,
                    "수동 전체 업데이트 실패", e);
        }
    }

    // ===== 공통 처리 플로우 =====
    private void saveAndPublish(
            String batchId, ProductDto productDto,
            boolean updatePrice, boolean updateStock
    ) {
        // DB 저장
        productService.saveProductAndMapping(productDto);
        recordProcessSuccess(batchId, productDto.getCode(), JobType.MANUAL_UPDATE_ALL_FIELDS, "UPDATE_PRODUCT_SAVE", "DB저장 성공");

        // 채널 업데이트 발행
        productService.publishChannelUpdates(batchId, productDto, updatePrice, updateStock);
        recordProcessSuccess(batchId, productDto.getCode(), JobType.MANUAL_UPDATE_ALL_FIELDS, "UPDATE_PRODUCT_PUBLISH", "각 채널 메세지 발행 성공");
    }

    /**
     * 상품 DB 저장
     */
    private void saveProduct(String batchId, ProductDto productDto) {
        productService.saveProductAndMapping(productDto);
        recordProcessSuccess(batchId, productDto.getCode(), JobType.MANUAL_UPDATE_ALL_FIELDS, "UPDATE_PRODUCT_SAVE", "DB저장 성공");
    }

    /**
     * 채널 업데이트 메시지 발행
     */
    private void publishChannelUpdates(
            String batchId, ProductDto productDto,
            boolean priceChanged, boolean stockChanged
    ) {
        productService.publishChannelUpdates(batchId, productDto, priceChanged, stockChanged);
        recordProcessSuccess(batchId, productDto.getCode(), JobType.MANUAL_UPDATE_ALL_FIELDS,
                "UPDATE_PRODUCT_PUBLISH", "각 채널 메세지 발행 성공");
    }

    // ===== 공통 유틸리티 =====
    private void recordProcessSuccess(String batchId, String productCode, JobType jobType, String type, String message) {
        log.info("[{}] {}", productCode, message);
        pss.upsertProcessStatus(batchId, productCode, null, jobType, type, "SUCCESS", message);
    }

    // 가격/재고 검증 (2번 기능용)
    /*private void validatePriceStock(ProductDto productDto) {
        if (productDto.getSalePrice() == null) {
            throw new IllegalArgumentException("판매가격은 필수입니다");
        }
        if (productDto.getStock() == null) {
            throw new IllegalArgumentException("재고는 필수입니다");
        }
        if (productDto.getSalePrice() < 0) {
            throw new IllegalArgumentException("판매가격은 0 이상이어야 합니다");
        }
        if (productDto.getStock() < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다");
        }
    }*/

    // 전체 필드 검증 (3번 기능용)
    /*private void validateAllFields(ProductDto productDto) {
        // 기본 가격/재고 검증
        validatePriceStock(productDto);

        // 추가 필드 검증
        if (productDto.getCode() == null || productDto.getCode().isEmpty()) {
            throw new IllegalArgumentException("상품코드는 필수입니다");
        }
        if (productDto.getTitle() == null || productDto.getTitle().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }

        // 필요한 추가 검증 로직...
    }*/

    /**
     * 가격/재고 변경 여부 감지
     */
    private PriceStockChangeInfo detectPriceStockChanges(ProductDto productDto) {
        try {
            // DB에서 기존 상품 정보 조회
            Product existingProduct = productService.getProductOrThrow(productDto.getCode());

            // 가격 변경 여부
            boolean priceChanged = !Objects.equals(
                    existingProduct.getSalePrice(),
                    productDto.getSalePrice()
            );

            // 재고 변경 여부
            boolean stockChanged = !Objects.equals(
                    existingProduct.getStock(),
                    productDto.getStock()
            );

            return new PriceStockChangeInfo(priceChanged, stockChanged);

        } catch (Exception e) {
            log.warn("[{}] 기존 데이터 조회 실패 - 신규 상품으로 간주하여 채널 업데이트 발행",
                    productDto.getCode());
            // 기존 데이터가 없으면 신규 상품으로 간주하여 변경된 것으로 처리
            return new PriceStockChangeInfo(true, true);
        }
    }

    // Consumer 에러 처리 (공통)
    private void handleConsumerError(String batchId, String productCode, JobType jobType, String errorType, Exception e) {
        log.error("[{}] {}: {}", productCode, errorType, e.getMessage(), e);
        pss.upsertProcessStatus(batchId, productCode, null,
                jobType,
                "UPDATE_PRODUCT_ERROR", "FAILED", errorType + ": " + e.getMessage());
        throw new AmqpRejectAndDontRequeueException("MQ 폐기(" + errorType + ")", e);
    }
}
