package com.jason.purchase_agent.service.products;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.products.ProductUpdateRequest;
import com.jason.purchase_agent.dto.suppliers.SupplierDto;
import com.jason.purchase_agent.entity.ProcessStatus;
import com.jason.purchase_agent.messaging.MessageQueueService;
import com.jason.purchase_agent.dto.products.ProductDto;
import com.jason.purchase_agent.dto.products.ProductSearchDto;
import com.jason.purchase_agent.dto.products.ProductUpdateDto;
import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.entity.ProductChannelMapping;
import com.jason.purchase_agent.repository.jpa.ProcessStatusRepository;
import com.jason.purchase_agent.repository.jpa.ProductRepository;
import com.jason.purchase_agent.repository.jpa.ProductChannelMappingRepository;
import com.jason.purchase_agent.service.process_status.ProcessStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProcessStatusService processStatusService;

    private final ProductRepository productRepository;
    private final ProductChannelMappingRepository channelMappingRepository;
    private final ProductChannelMappingRepository mappingRepository;
    private final ProcessStatusRepository psr;
    private final MessageQueueService messageQueueService;
    private final ObjectMapper objectMapper;

    /**
     * 상품목록 조회 (검색 + 필터 + 페이징)
     */
    public Page<ProductDto> getProductList(ProductSearchDto searchDto, Pageable pageable) {

        log.info("상품목록 조회 시작 - 검색어: {}, 공급업체 필터: {}, 페이지크기: {}, 페이지번호: {}",
                searchDto.getSearchKeyword(),
                searchDto.getSupplierCodes(),
                searchDto.getPageSize(),
                searchDto.getPageNumber());

        // 페이징 객체 생성
        // Pageable pageable = PageRequest.of(searchDto.getPageNumber(), searchDto.getPageSize());

        // 필터 조건 로그 출력
        if (searchDto.hasChannelNullFilters()) {
            log.info("채널 null 필터 적용 - 쿠팡품목ID: {}, 쿠팡상품ID: {}, 스마트스토어노출ID: {}, 스마트스토어상품ID: {}, 11번가상품ID: {}",
                    searchDto.getFilterNullVendorItemId(),
                    searchDto.getFilterNullSellerProductId(),
                    searchDto.getFilterNullSmartstoreId(),
                    searchDto.getFilterNullOriginProductNo(),
                    searchDto.getFilterNullElevenstId());
        }

        // 상품 데이터 조회 (새로운 필터 쿼리 사용)
        Page<Product> productPage = productRepository.findProductsWithFilters(
                searchDto.getSearchKeyword(),
                searchDto.getSupplierCodes(),
                Boolean.TRUE.equals(searchDto.getFilterNullVendorItemId()),
                Boolean.TRUE.equals(searchDto.getFilterNullSellerProductId()),
                Boolean.TRUE.equals(searchDto.getFilterNullSmartstoreId()),
                Boolean.TRUE.equals(searchDto.getFilterNullOriginProductNo()),
                Boolean.TRUE.equals(searchDto.getFilterNullElevenstId()),
                pageable
        );

        // 상품 코드 리스트 추출
        List<String> productCodes = productPage.getContent().stream()
                .map(Product::getCode)
                .collect(Collectors.toList());

        // 채널매핑 정보 조회
        List<ProductChannelMapping> channelMappings = channelMappingRepository.findByProductCodeIn(productCodes);
        Map<String, ProductChannelMapping> mappingMap = channelMappings.stream()
                .collect(Collectors.toMap(ProductChannelMapping::getProductCode, mapping -> mapping));

        // DTO 변환
        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(product -> convertToListDto(product, mappingMap.get(product.getCode())))
                .collect(Collectors.toList());

        log.info("상품목록 조회 완료 - 총 {}개 상품", productDtos.size());

        return new PageImpl<>(productDtos, pageable, productPage.getTotalElements());
    }

    /**
     * 공급업체 목록 조회 (필터 옵션용)
     */
    public List<SupplierDto> getSupplierList() {
        log.info("공급업체 목록 조회");
        List<SupplierDto> suppliers = productRepository.findSuppliersWithProductCount();
        log.info("공급업체 목록 조회 완료 - {}개 공급업체", suppliers.size());
        return suppliers;
    }

    /**
     * 상품 상세정보 조회
     */
    public Product getProductDetail(String code) {
        log.info("상품 상세정보 조회 - 코드: {}", code);
        return productRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Not Found : " + code));
    }

    /**
     * 상품 가격/재고 일괄 업데이트
     */
    @Transactional
    public void updateProductPriceAndStock(ProductUpdateDto updateDto) {
        log.info("상품 가격/재고 일괄 업데이트 시작 - 업데이트 대상: {}개", updateDto.getUpdateItems().size());

        for (ProductUpdateDto.ProductUpdateItemDto item : updateDto.getUpdateItems()) {
            Product product = null;
            try {
                product = productRepository.findById(item.getCode())
                        .orElseThrow(() -> new Exception("Not Found : " + item.getCode()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (product != null) {
                // 가격 업데이트
                if (item.getSalePrice() != null) {
                    product.setSalePrice(item.getSalePrice());
                }
                // 재고 업데이트
                if (item.getStock() != null) {
                    product.setStock(item.getStock());
                }
                productRepository.save(product);
                log.info("상품 업데이트 완료 - 코드: {}, 가격: {}, 재고: {}",
                        item.getCode(), item.getSalePrice(), item.getStock());
            }
        }

        log.info("상품 가격/재고 일괄 업데이트 완료");
    }

    /**
     * Product 엔티티를 ProductDto로 변환
     */
    public static ProductDto convertToListDto(Product product, ProductChannelMapping channelMapping) {
        ProductDto dto = ProductDto.builder()
                .code(product.getCode())
                .supplierCode(product.getSupplier() != null ? product.getSupplier().getSupplierCode() : null)
                .title(product.getTitle())
                .link(product.getLink())
                .unitValue(product.getUnitValue())
                .unit(product.getUnit())
                .packQty(product.getPackQty())
                .salePrice(product.getSalePrice())
                .stock(product.getStock())
                .korName(product.getKorName())
                .engName(product.getEngName())
                .brandName(product.getBrandName())
                .build();

        // 채널매핑 정보 추가
        if (channelMapping != null) {
            dto.setVendorItemId(channelMapping.getVendorItemId());
            dto.setSellerProductId(channelMapping.getSellerProductId());
            dto.setSmartstoreId(channelMapping.getSmartstoreId());
            dto.setOriginProductNo(channelMapping.getOriginProductNo());
            dto.setElevenstId(channelMapping.getElevenstId());
        }

        return dto;
    }

    /**
     * 상품 정보를 신규로 저장하거나,
     * 코드가 이미 존재하면 덮어쓸(Override) 수 있게 한다.
     */
    @Transactional
    public Product saveOrUpdate(Product product) {
        return productRepository.save(product);
    }

    /**
     * 공급업체별 상품목록 반환
     * @param supplierCode 공급업체 PK
     * @return List<Product>
     */
    public List<Product> getProductsBySupplier(String supplierCode) {
        return productRepository.findBySupplier_SupplierCode(supplierCode);
    }






    /**
     * [비즈니스 진입점]
     * 상품 정보 및 채널 연동 정보 업데이트 전체 흐름 컨트롤.
     * 트랜잭션 경계 내에서 상태 기록/DB 저장/채널 동기화/에러 대응 등 모든 절차를 수행한다.
     *
     * @param code 상품 코드
     * @param request 요청 정보 (수정 파라미터 DTO)
     * @param priceChanged 가격 변경 여부
     * @param stockChanged 재고 변경 여부
     * @param batchId 재시도 시 기존 배치ID, 최초 요청이면 null
     * @return 처리 배치ID
     */
    @Transactional(rollbackFor = Exception.class)
    public String updateProductAndMappingWithSync(
            String code, ProductUpdateRequest request,
            boolean priceChanged, boolean stockChanged, String batchId
    ) {

        log.info("[Start] 상품/연동정보 업데이트 시작 - code={}, priceChanged={}, stockChanged={}, batchId={}",
                code, priceChanged, stockChanged, batchId);
        log.debug("[RequestDTO] = {}", request);

        boolean isRetry = (batchId != null && !batchId.isEmpty());
        String detailsJson = null;
        try {
            // 1. 요청 데이터 직렬화/복원 – 최초/재시도 분기
            if (!isRetry) {
                batchId = UUID.randomUUID().toString();
                detailsJson = serializeDetails(request, priceChanged, stockChanged);
                log.info("[Batch] 최초 요청 - 새 batchId 생성: {}", batchId);
                log.debug("[Batch] 직렬화 details: {}", detailsJson);
            } else {
                log.info("[Batch] 재시도 요청 - batchId={}, code={}", batchId, code);
                RestoredData restored = restoreFailedRequest(batchId, code);
                request = restored.request;
                priceChanged = restored.priceChanged;
                stockChanged = restored.stockChanged;
                log.debug("[Batch] 이력 복원 후 Request: {}", request);
                detailsJson = null; // 이미 기록됨
            }

            // 2. 처리 시작 상태 기록
            upsertProcessStatusStart(batchId, code, detailsJson, isRetry);
            log.info("[ProcessStatus] 처리 시작 상태 기록 완료 - batchId={}", batchId);

            // 3. DB에서 상품/채널 정보 조회 및 값 업데이트
            Product product = getProductOrThrow(code);
            log.info("[DB] 상품 조회 성공 - code={}", code);
            ProductChannelMapping mapping = findOrCreateChannelMapping(code);
            log.info("[DB] 채널맵핑 조회/생성 성공 - code={}", code);

            updateProductFields(product, request);
            log.debug("[DB] 상품 필드 업데이트 완료 - {}", product);
            updateChannelMappingFields(mapping, request);
            log.debug("[DB] 채널 매핑 필드 업데이트 완료 - {}", mapping);

            productRepository.save(product);
            mappingRepository.save(mapping);
            log.info("[DB] 상품/채널 정보 DB 저장 성공 - code={}", code);

            // 4. DB처리 성공 상태 기록
            processStatusService.upsertProcessStatus(
                    batchId, code, null, "DB SAVE", "SUCCESS", "상품/연동정보 DB 저장 완료");
            log.info("[ProcessStatus] DB 저장 완료 상태 기록 - batchId={}", batchId);

            // 5. 채널별 가격/재고 동기화 메시지 처리
            log.info("[MessageQueue] 채널별 동기화 메시지 전송 시작 - priceChanged={}, stockChanged={}",
                    priceChanged, stockChanged);

            // 1) 쿠팡 채널만 별도 분기
            if (needCoupangSync(mapping, priceChanged, stockChanged)) {
                if (mapping.getVendorItemId() == null || mapping.getVendorItemId().isBlank()) {
                    // vendorItemId 없음(필요) → vendorItemIdSyncMessage만 발행 후 종료
                    messageQueueService.publishVendorItemIdSync(mapping, product, batchId, priceChanged, stockChanged);
                    log.info("[MQ][VENDOR_ID_SYNC] 큐 발행 후 후속 동기화는 비동기로 실행됨");
                } else {
                    // vendorItemId 존재 → Price/Stock 메시지 바로 발행
                    if (priceChanged) messageQueueService.publishPriceUpdate("coupang", product, mapping, batchId);
                    if (stockChanged) messageQueueService.publishStockUpdate("coupang", product, mapping, batchId);
                    log.info("[MQ][COUPANG] Price/Stock 업데이트 메시지 직접 발행(동기진행)");
                }
            }

            // 2) 타 채널 메시지는 기존 로직 유지
            syncToChannelsForOtherChannels(mapping, product, batchId, priceChanged, stockChanged);
            // syncToChannels(mapping, product, batchId, priceChanged, stockChanged);

            log.info("[MessageQueue] 채널별 메시지 전송 완료");

            // 6. 처리 완료 batchId 반환
            log.info("[Finish] 상품/연동정보 업데이트 성공적으로 종료 - batchId={}", batchId);
            return batchId;
        } catch (Exception  ex) {
            // 7. 에러 발생 시 실패 상태 기록 및 예외 전파
            log.error("[Error] 상품/연동정보 업데이트 실패 - batchId={}, code={}, 원인={}",
                    batchId, code, ex.getMessage(), ex);
            processStatusService.upsertProcessStatus(
                    batchId, code, null,
                    "DB SAVE", "FAILED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 쿠팡 채널에 대한 동기화(가격/재고) 작업이 필요한지 여부 판단.
     * vendorItemId 또는 sellerProductId 있는지, 가격/재고 변경 플래그 등 조건 조합 가능
     */
    private boolean needCoupangSync(ProductChannelMapping mapping, boolean priceChanged, boolean stockChanged) {
        boolean hasCoupangId = mapping.getSellerProductId() != null && !mapping.getSellerProductId().isBlank();
        // 만약 vendorItemId까지 체크하려면 아래처럼 (없어도 됨)
        // boolean hasVendorItemId = mapping.getVendorItemId() != null && !mapping.getVendorItemId().isBlank();
        // return hasCoupangId && (priceChanged || stockChanged);
        return hasCoupangId && (priceChanged || stockChanged);
    }

    /**
     * 쿠팡 외 타 채널(스마트스토어, 11번가 등) 동기화 메시지 발송 (기존 방식 유지)
     */
    private void syncToChannelsForOtherChannels(
            ProductChannelMapping mapping, Product product,
            String batchId, boolean priceChanged, boolean stockChanged
    ) {
        Map<String, String> channelIdProps = new HashMap<>();
        // Coupang은 제외!
        // if (mapping.getVendorItemId() != null) channelIdProps.put("coupang", mapping.getVendorItemId());
        if (mapping.getOriginProductNo() != null) channelIdProps.put("smartstore", mapping.getOriginProductNo());
        if (mapping.getElevenstId() != null) channelIdProps.put("elevenst", mapping.getElevenstId());

        log.info("[SYNC][OTHERS] 타 채널 동기화 시작 - batchId={}, priceChanged={}, stockChanged={}, channels={}",
                batchId, priceChanged, stockChanged, channelIdProps.keySet());

        if (priceChanged) {
            channelIdProps.forEach((channel, id) -> {
                if (id != null) {
                    messageQueueService.publishPriceUpdate(channel, product, mapping, batchId);
                    log.debug("[MQ][PRICE][{}] 메시지 발송 완료 - id={}, batchId={}", channel, id, batchId);
                }
            });
        }
        if (stockChanged) {
            channelIdProps.forEach((channel, id) -> {
                if (id != null) {
                    messageQueueService.publishStockUpdate(channel, product, mapping, batchId);
                    log.debug("[MQ][STOCK][{}] 메시지 발송 완료 - id={}, batchId={}", channel, id, batchId);
                }
            });
        }

        log.info("[SYNC][OTHERS] 타 채널 동기화 종료 - batchId={}", batchId);
    }


    /**
     * 최초 요청 시, 요청 데이터와 변경 플래그를 JSON으로 직렬화하여 details 필드로 저장.
     */
    private String serializeDetails(
            ProductUpdateRequest request, boolean priceChanged, boolean stockChanged
    ) {
        Map<String, Object> details = Map.of(
                "request", request,
                "priceChanged", priceChanged,
                "stockChanged", stockChanged
        );
        try {
            return objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            return "{}"; // 직렬화 실패 시 대비
        }
    }

    /**
     * 재시도 요청인 경우, 이력 테이블(ProcessStatus)에서 실패한 요청의 원본 파라미터 복원.
     */
    private RestoredData restoreFailedRequest(
            String batchId, String code
    ) {
        ProcessStatus ps = psr.findByBatchIdAndProductCode(batchId, code)
                .orElseThrow(() -> new IllegalArgumentException("해당 이력 없음"));

        Map<String, Object> detailsMap;
        try {
            detailsMap = objectMapper.readValue(ps.getDetails(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Details JSON 역직렬화 실패: " + e.getMessage(), e);
        }
        ProductUpdateRequest req = objectMapper.convertValue(detailsMap.get("request"), ProductUpdateRequest.class);
        boolean priceChanged = Boolean.TRUE.equals(detailsMap.get("priceChanged"));
        boolean stockChanged = Boolean.TRUE.equals(detailsMap.get("stockChanged"));

        return new RestoredData(req, priceChanged, stockChanged);
    }

    /**
     * 처리 시작 시 프로세스 상태를 PENDING으로 기록 (최초/재시도 메시지 구분)
     */
    private void upsertProcessStatusStart(
            String batchId, String code, String detailsJson, boolean isRetry
    ) {
        String msg = isRetry ? "상품 수정 재시도 요청" : "상품 수정 처리 시작";
        processStatusService.upsertProcessStatus(batchId, code, detailsJson, "DB SAVE", "PENDING", msg);
    }

    /**
     * 상품 코드로 DB에서 상품을 조회하고, 없으면 예외 발생.
     */
    private Product getProductOrThrow(
            String code
    ) {
        return productRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException(code + " 상품을 찾을 수 없습니다."));
    }

    /**
     * 상품 코드로 채널 매핑 정보 조회, 없으면 신규 생성.
     */
    private ProductChannelMapping findOrCreateChannelMapping(
            String code
    ) {
        return mappingRepository.findById(code).orElseGet(() -> createNewChannelMapping(code));
    }

    /**
     * 신규 채널 매핑 객체 생성 및 저장.
     */
    private ProductChannelMapping createNewChannelMapping(
            String productCode
    ) {

        ProductChannelMapping newMapping = ProductChannelMapping.builder()
                .productCode(productCode).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        mappingRepository.save(newMapping);
        return newMapping;
    }

    /**
     * 상품 객체의 값을 요청 DTO로부터 업데이트 (변경 필드 한정).
     */
    private void updateProductFields(
            Product product, ProductUpdateRequest request
    ) {
        product.setTitle(request.getTitle());
        product.setKorName(request.getKorName());
        product.setEngName(request.getEngName());
        product.setBrandName(request.getBrandName());
        product.setUnitValue(request.getUnitValue());
        product.setUnit(request.getUnit());
        product.setWeight(request.getWeight());
        product.setLink(request.getLink());
        product.setBuyPrice(request.getBuyPrice());
        product.setSalePrice(request.getSalePrice());
        product.setStock(request.getStock());
        product.setPackQty(request.getPackQty());
        product.setMarginRate(request.getMarginRate());
        product.setShippingCost(request.getShippingCost());
        product.setDetailsHtml(request.getDetailsHtml());
        product.setMemo(request.getMemo());
        product.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * 채널 매핑 객체의 값을 요청 DTO로부터 업데이트 (변경 필드 한정).
     */
    private void updateChannelMappingFields(
            ProductChannelMapping mapping, ProductUpdateRequest request
    ) {
        mapping.setVendorItemId(request.getVendorItemId());
        mapping.setSellerProductId(request.getSellerProductId());
        mapping.setSmartstoreId(request.getSmartstoreId());
        mapping.setOriginProductNo(request.getOriginProductNo());
        mapping.setElevenstId(request.getElevenstId());
        mapping.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * 가격 또는 재고 변경 시, 활성화된 채널별로 동기화 메시지 발송.
     * (쿠팡/스마트스토어/11번가 각각 상태에 따라 처리)
     */
    private void syncToChannels(
            ProductChannelMapping mapping, Product product,
            String batchId, boolean priceChanged, boolean stockChanged
    ) {
        Map<String, String> channelIdProps = new HashMap<>();
        if (mapping.getVendorItemId() != null) channelIdProps.put("coupang", mapping.getVendorItemId());
        if (mapping.getOriginProductNo() != null) channelIdProps.put("smartstore", mapping.getOriginProductNo());
        if (mapping.getElevenstId() != null) channelIdProps.put("elevenst", mapping.getElevenstId());

        log.info("[SYNC] 채널별 동기화 시작 - batchId={}, priceChanged={}, stockChanged={}, channels={}",
                batchId, priceChanged, stockChanged, channelIdProps.keySet());

        if (priceChanged) {
            channelIdProps.forEach((channel, id) -> {
                log.info("[SYNC][PRICE] {} -> id={}, productCode={}, batchId={}",
                        channel, id, product.getCode(), batchId);

                if (id != null) {
                    messageQueueService.publishPriceUpdate(channel, product, mapping, batchId);
                    log.debug("[MQ][PRICE] 메시지 발송 완료 - channel={}, id={}, batchId={}", channel, id, batchId);

                }
            });
        }
        if (stockChanged) {
            channelIdProps.forEach((channel, id) -> {
                log.info("[SYNC][STOCK] {} -> id={}, productCode={}, batchId={}", channel, id, product.getCode(), batchId);
                if (id != null) {
                    messageQueueService.publishStockUpdate(channel, product, mapping, batchId);
                    log.debug("[MQ][STOCK] 메시지 발송 완료 - channel={}, id={}, batchId={}", channel, id, batchId);

                }
            });
        }

        log.info("[SYNC] 채널별 동기화 종료 - batchId={}", batchId);
    }

    /**
     * 실패 이력 복원용 임시 DTO.
     */
    private static class RestoredData {
        final ProductUpdateRequest request;
        final boolean priceChanged;
        final boolean stockChanged;

        RestoredData(ProductUpdateRequest req, boolean priceChanged, boolean stockChanged) {
            this.request = req;
            this.priceChanged = priceChanged;
            this.stockChanged = stockChanged;
        }
    }
}