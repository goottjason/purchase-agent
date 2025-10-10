package com.jason.purchase_agent.service.products;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.products.ManualPriceStockUpdateRequest;
import com.jason.purchase_agent.dto.products.ProductUpdateRequest;
import com.jason.purchase_agent.dto.suppliers.SupplierDto;
import com.jason.purchase_agent.entity.ProcessStatus;
import com.jason.purchase_agent.external.coupang.CoupangApiService;
import com.jason.purchase_agent.external.iherb.IherbProductCrawler;
import com.jason.purchase_agent.external.iherb.dto.IherbProductDto;
import com.jason.purchase_agent.messaging.MessageQueueService;
import com.jason.purchase_agent.dto.products.ProductDto;
import com.jason.purchase_agent.dto.products.ProductSearchDto;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import static com.jason.purchase_agent.common.calculator.Calculator.calculateSalePrice;
import static com.jason.purchase_agent.external.iherb.IherbProductCrawler.crawlProductAsJson;
import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;
import static com.jason.purchase_agent.util.exception.ExceptionUtils.uncheck;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProcessStatusService pss;
    private final CoupangApiService coupangApiService;
    private final ProductRepository productRepository;
    private final ProductChannelMappingRepository channelMappingRepository;
    private final ProductChannelMappingRepository mappingRepository;
    private final ProcessStatusRepository psr;
    private final MessageQueueService messageQueueService;
    private final ObjectMapper objectMapper;

    public List<ProductUpdateRequest> makeRequestsBySupplier(
            String supplierCode
    ) {
        List<Product> products = productRepository.findBySupplier_SupplierCode(supplierCode);

        // 상품별로 채널 매핑 정보 불러와서 ProductDto에 세팅
        return products.stream().map(prod -> {
            ProductDto dto = ProductDto.fromEntity(prod);

            // 예: 채널 매핑 정보를 불러서 DTO에 세팅
            Optional<ProductChannelMapping> mapping = channelMappingRepository.findByProductCode(prod.getCode());

            if (mapping.isPresent()) {
                ProductChannelMapping map = mapping.get();
                dto.setVendorItemId(map.getVendorItemId());
                dto.setSellerProductId(map.getSellerProductId());
                dto.setSmartstoreId(map.getSmartstoreId());
                dto.setOriginProductNo(map.getOriginProductNo());
                dto.setElevenstId(map.getElevenstId());
                dto.setCafeNo(map.getCafeNo());
                dto.setCafeCode(map.getCafeCode());
                dto.setCafeOptCode(map.getCafeOptCode());
            }

            return ProductUpdateRequest.builder()
                    .code(prod.getCode())
                    .productDto(dto)
                    .priceChanged(true)
                    .stockChanged(true)
                    .build();
        }).toList();
    }

    public void crawlAndSetPriceStock (
            ProductDto productDto, Integer marginRate, Integer couponRate, Integer minMarginPrice
    ) {
        String prodId = productRepository.findIherbProductIdFromLinkByCode(productDto.getCode());
        String productJson = uncheck(() -> crawlProductAsJson(prodId));
        IherbProductDto iherbProductDto = IherbProductDto.fromJsonWithLinks(productJson);

        Integer salePrice = calculateSalePrice(marginRate, couponRate, minMarginPrice, productDto.getPackQty(), iherbProductDto);
        Integer stock = Boolean.TRUE.equals(iherbProductDto.getIsAvailableToPurchase()) ? 500 : 0;

        productDto.setSalePrice(salePrice);
        productDto.setStock(stock);
    }

    public void saveProductAndMapping(
            ProductDto productDto
    ) {
        Product product = getProductOrThrow(productDto.getCode());
        ProductChannelMapping mapping = findOrCreateChannelMapping(productDto.getCode());

        updateProductFields(product, productDto);
        updateChannelMappingFields(mapping, productDto);

        productRepository.save(product);
        mappingRepository.save(mapping);
    }

    public void publishChannelUpdates(
            String batchId, ProductDto productDto, boolean priceChanged, boolean stockChanged
    ) {
        boolean hasSellerProductId =
                productDto.getSellerProductId() != null && !productDto.getSellerProductId().isBlank();
        boolean hasVendorItemId =
                productDto.getVendorItemId() != null && !productDto.getVendorItemId().isBlank();
        // sellerProductId와 vendorItemId가 모두 있을 경우, 메세지 발행
        if (hasSellerProductId) {
            if (!hasVendorItemId) {
                // vendorItemId 조회해서 세팅하고 DB 저장
                String responseJson = coupangApiService.findProductInfo(productDto.getSellerProductId());
                JsonNode root = uncheck(() -> objectMapper.readTree(responseJson));
                JsonNode dataNode = root.path("data");
                JsonNode itemsNode = dataNode.path("items");
                if (itemsNode.isArray() && itemsNode.size() > 0) {
                    JsonNode item = itemsNode.get(0);
                    String vendorItemId = item.path("vendorItemId").asText();
                    if (vendorItemId != null && !vendorItemId.isBlank()) {
                        productDto.setVendorItemId(vendorItemId);
                        saveProductAndMapping(productDto);
                        // hasVendorItemId = true;
                    }
                }
            }
            // 실시간 체크로 변경
            if (productDto.getVendorItemId() != null && !productDto.getVendorItemId().isBlank() && priceChanged) {
                messageQueueService.publishPriceUpdateChannel(batchId, "coupang", productDto);
            }
            if (productDto.getVendorItemId() != null && !productDto.getVendorItemId().isBlank() && stockChanged) {
                messageQueueService.publishStockUpdateChannel(batchId, "coupang", productDto);
            }
        }
        syncToChannelsForOtherChannels(batchId, productDto, priceChanged, stockChanged);
    }

    private void syncToChannelsForOtherChannels(
            String batchId, ProductDto productDto, boolean priceChanged, boolean stockChanged
    ) {

        Map<String, String> channelIdProps = new HashMap<>();
        if (productDto.getOriginProductNo() != null && !productDto.getOriginProductNo().trim().isEmpty()) {
            channelIdProps.put("smartstore", productDto.getOriginProductNo());
        }
        if (productDto.getElevenstId() != null && !productDto.getElevenstId().trim().isEmpty()) {
            channelIdProps.put("elevenst", productDto.getElevenstId());
        }
        if (productDto.getCafeNo() != null && !productDto.getCafeNo().trim().isEmpty()) {
            channelIdProps.put("cafe", productDto.getCafeNo());
        }

        if (priceChanged) {
            channelIdProps.forEach((channel, id) -> {
                if (id != null) {
                    messageQueueService.publishPriceUpdateChannel(batchId, channel, productDto);
                }
            });
        }
        if (stockChanged) {
            channelIdProps.forEach((channel, id) -> {
                if (id != null) {
                    messageQueueService.publishStockUpdateChannel(batchId, channel, productDto);
                }
            });
        }
    }
























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
            dto.setCafeNo(channelMapping.getCafeNo());
            dto.setCafeCode(channelMapping.getCafeCode());
            dto.setCafeOptCode(channelMapping.getCafeOptCode());
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
     * @param productDto 요청 정보 (수정 파라미터 DTO)
     * @param priceChanged 가격 변경 여부
     * @param stockChanged 재고 변경 여부
     * @param batchId 재시도 시 기존 배치ID, 최초 요청이면 null
     * @return 처리 배치ID
     */
    @Transactional(rollbackFor = Exception.class)
    public String updateProductAndMappingWithSync(
            String code, ProductDto productDto,
            boolean priceChanged, boolean stockChanged, String batchId
    ) {

        log.info("[Start] 상품 업데이트 시작 - code={}, priceChanged={}, stockChanged={}, batchId={}",
                code, priceChanged, stockChanged, batchId);

        boolean isRetry = (batchId != null && !batchId.isEmpty());
        String detailsJson = null;
        try {

            /** (1) details 관련 처리 (최초 / 재시도 분기)
             * 최초 시도시, batchId 생성 + 요청정보 JSON으로 직렬화하여 details 필드로 저장
             * 재시도시, ProcessStatus 테이블에서 요청의 details 필드를 역직렬화하여 요청정보에 저장
             */
            if (!isRetry) {
                batchId = UUID.randomUUID().toString();
                detailsJson = serializeDetails(productDto, priceChanged, stockChanged);
                log.info("[Batch] 최초 요청 - 새 batchId 생성: {}", batchId);
                log.debug("[Batch] 직렬화 details: {}", detailsJson);
            } else {
                log.info("[Batch] 재시도 요청 - batchId={}, code={}", batchId, code);
                RestoredData restored = restoreFailedRequest(batchId, code);
                productDto = restored.productDto;
                priceChanged = restored.priceChanged;
                stockChanged = restored.stockChanged;
                log.debug("[Batch] 이력 복원 후 productDto: {}", productDto);
                detailsJson = null; // 이미 기록됨 (null로 두면, 기록된 데이터 유지)
            }

            /** (2) process_status 테이블에 DB SAVE - PENDING 기록
             * 최초 시도시, insert
             * 재시도시, update
             */
            upsertProcessStatusStart(batchId, code, detailsJson, isRetry);
            log.info("[ProcessStatus] 처리 시작 상태 기록 완료 - batchId={}", batchId);

            /** (3) product, mapping 테이블 조회하여 엔티티로 저장
             * product 조회 (불가능시, throw)
             * mapping 조회 (불가능시, 각 채널 ID null로 하여 insert)
             */
            Product product = getProductOrThrow(code);
            ProductChannelMapping mapping = findOrCreateChannelMapping(code);
            log.info("[DB] 상품/채널맵핑 조회/생성 성공 - code={}", code);

            /** (4) request 값을 product, mapping 테이블에 업데이트 (DB 저장)
             */
            updateProductFields(product, productDto);
            updateChannelMappingFields(mapping, productDto);
            productRepository.save(product);
            mappingRepository.save(mapping);
            log.info("[DB] 상품/채널 정보 DB 저장 성공 - code={}", code);

            /** (5) process_status 테이블에 DB SAVE - SUCCESS|FAIL 기록
             *
             */
            pss.upsertProcessStatus(
                    batchId, code, null,
                    "CHANNEL UPDATE", "PENDING", "{}");
            log.info("[ProcessStatus] DB 저장 완료 상태 기록 - batchId={}", batchId);

            /** (6) 메시지 발행 (쿠팡 채널 / 타 채널 분기)
             * 쿠팡 채널
             *   - sellerProductId(O), vendorItemId(X), 가격/재고 업데이트 필요 : Sync 발행 (내부에서 Update 발행)
             *   - sellerProductId(O), vendorItemId(O), 가격/재고 업데이트 필요 : Update 발행
             * 타 채널
             *   - originProductNo(O), 가격/재고 업데이트 필요 : Update 발행
             *   - elevenstId(O), 가격/재고 업데이트 필요 : Update 발행
             */
            if (needCoupangSync(mapping, priceChanged, stockChanged)) {
                if (mapping.getVendorItemId() == null || mapping.getVendorItemId().isBlank()) {
                    messageQueueService.publishVendorItemIdSync(
                            mapping, product, batchId, priceChanged, stockChanged);
                    log.info("[MQ][VENDOR_ID_SYNC] 발행");
                } else {
                    // vendorItemId 존재 → Price/Stock 메시지 바로 발행
                    /*if (priceChanged) messageQueueService.publishPriceUpdate(
                            "coupang", product, mapping, batchId);
                    if (stockChanged) messageQueueService.publishStockUpdate(
                            "coupang", product, mapping, batchId);*/
                    log.info("[MQ][COUPANG] Price/Stock 업데이트 메시지 직접 발행");
                }
            }
            // syncToChannelsForOtherChannels(mapping, product, batchId, priceChanged, stockChanged);

            log.info("[Finish] 상품/연동정보 업데이트 성공적으로 종료 - batchId={}", batchId);

            /** (7) batchId 반환
             */
            return batchId;
        } catch (Exception  ex) {
            // 7. 에러 발생 시 실패 상태 기록 및 예외 전파
            log.error("[Error] 상품/연동정보 업데이트 실패 - batchId={}, code={}, 원인={}",
                    batchId, code, ex.getMessage(), ex);
            pss.upsertProcessStatus(
                    batchId, code, null,
                    "DB SAVE", "FAILED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * sellerProductId 있으면서 가격/재고 변경 플래그 하나라도 참일 경우
     */
    private boolean needCoupangSync(
            ProductChannelMapping mapping, boolean priceChanged, boolean stockChanged
    ) {
        boolean hasCoupangId = mapping.getSellerProductId() != null && !mapping.getSellerProductId().isBlank();
        // 만약 vendorItemId까지 체크하려면 아래처럼 (없어도 됨)
        // boolean hasVendorItemId = mapping.getVendorItemId() != null && !mapping.getVendorItemId().isBlank();
        // return hasCoupangId && (priceChanged || stockChanged);
        return hasCoupangId && (priceChanged || stockChanged);
    }




    /**
     * 최초 요청 시, 요청 데이터와 변경 플래그를 JSON으로 직렬화하여 details 필드로 저장.
     */
    private String serializeDetails(
            ProductDto productDto, boolean priceChanged, boolean stockChanged
    ) {
        Map<String, Object> details = Map.of(
                "productDto", productDto,
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
        ProductDto productDto = objectMapper.convertValue(detailsMap.get("productDto"), ProductDto.class);
        boolean priceChanged = Boolean.TRUE.equals(detailsMap.get("priceChanged"));
        boolean stockChanged = Boolean.TRUE.equals(detailsMap.get("stockChanged"));

        return new RestoredData(productDto, priceChanged, stockChanged);
    }

    /**
     * 처리 시작 시 프로세스 상태를 PENDING으로 기록 (최초/재시도 메시지 구분)
     */
    private void upsertProcessStatusStart(
            String batchId, String code, String detailsJson, boolean isRetry
    ) {
        String msg = isRetry ? "상품 수정 재시도 요청" : "상품 수정 처리 시작";
        pss.upsertProcessStatus(batchId, code, detailsJson,
                "DB SAVE", "PENDING", msg);
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
            Product product, ProductDto productDto
    ) {
        if (productDto.getTitle() != null) product.setTitle(productDto.getTitle());
        if (productDto.getKorName() != null) product.setKorName(productDto.getKorName());
        if (productDto.getEngName() != null) product.setEngName(productDto.getEngName());
        if (productDto.getBrandName() != null) product.setBrandName(productDto.getBrandName());
        if (productDto.getUnitValue() != null) product.setUnitValue(productDto.getUnitValue());
        if (productDto.getUnit() != null) product.setUnit(productDto.getUnit());
        if (productDto.getWeight() != null) product.setWeight(productDto.getWeight());
        if (productDto.getLink() != null) product.setLink(productDto.getLink());
        if (productDto.getBuyPrice() != null) product.setBuyPrice(productDto.getBuyPrice());
        if (productDto.getSalePrice() != null) product.setSalePrice(productDto.getSalePrice());
        if (productDto.getStock() != null) product.setStock(productDto.getStock());
        if (productDto.getPackQty() != null) product.setPackQty(productDto.getPackQty());
        if (productDto.getMarginRate() != null) product.setMarginRate(productDto.getMarginRate());
        if (productDto.getShippingCost() != null) product.setShippingCost(productDto.getShippingCost());
        if (productDto.getDetailsHtml() != null) product.setDetailsHtml(productDto.getDetailsHtml());
        if (productDto.getMemo() != null) product.setMemo(productDto.getMemo());
    }

    /**
     * 채널 매핑 객체의 값을 요청 DTO로부터 업데이트 (변경 필드 한정).
     */
    private void updateChannelMappingFields(
            ProductChannelMapping mapping, ProductDto productDto
    ) {
        if (productDto.getVendorItemId() != null) mapping.setVendorItemId(productDto.getVendorItemId());
        if (productDto.getSellerProductId() != null) mapping.setSellerProductId(productDto.getSellerProductId());
        if (productDto.getSmartstoreId() != null) mapping.setSmartstoreId(productDto.getSmartstoreId());
        if (productDto.getOriginProductNo() != null) mapping.setOriginProductNo(productDto.getOriginProductNo());
        if (productDto.getElevenstId() != null) mapping.setElevenstId(productDto.getElevenstId());
        if (productDto.getCafeNo() != null) mapping.setCafeNo(productDto.getCafeNo());
        if (productDto.getCafeCode() != null) mapping.setCafeCode(productDto.getCafeCode());
        if (productDto.getCafeOptCode() != null) mapping.setCafeOptCode(productDto.getCafeOptCode());
    }

    // 리스트로 상품묶음 받아와서 개별 처리...
    // (상품목록 리스트에서 선택한 상품해도 되고, 자동 가격/재고 업데이트 에서 supplier별로 확인해도 여기로 받으면 될듯?)
    // 
    public void crawlAndSetPriceStock (
            int marginRate, int couponRate, int minMarginPrice, List<ProductUpdateRequest> requests
    ) {
        log.info("[crawlAndSetPriceStock] 호출 시작: marginRate={}, couponRate={}, minMarginPrice={}, 요청건수={}",
                marginRate, couponRate, minMarginPrice, requests.size());
        for (ProductUpdateRequest request : requests) {
            // 크롤링 및 가격/재고 계산
            ProductDto productDto = request.getProductDto();
            try {
                log.info("[crawlAndSetPriceStock] 상품 처리 시작: code={}", productDto.getCode());
                String prodId = productRepository.findIherbProductIdFromLinkByCode(productDto.getCode());
                String productJson = crawlProductAsJson(prodId);
                log.debug("[crawlAndSetPriceStock] iHerb 상품 크롤링 완료: prodId={}", prodId);

                IherbProductDto iherbProductDto = IherbProductDto.fromJsonWithLinks(productJson);
                log.debug("[crawlAndSetPriceStock] iHerbProductDto 변환 완료: iherbProductDto={}", iherbProductDto);

                Integer salePrice = calculateSalePrice(
                        marginRate, couponRate, minMarginPrice, productDto.getPackQty(), iherbProductDto);
                Integer stock = Boolean.TRUE.equals(iherbProductDto.getIsAvailableToPurchase()) ? 500 : 0;
                log.info("[crawlAndSetPriceStock] 계산 결과: code={}, salePrice={}, stock={}",
                        productDto.getCode(), salePrice, stock);
                productDto.setSalePrice(salePrice);
                productDto.setStock(stock);
            } catch (Exception ex) {
                log.error("[crawlAndSetPriceStock] 처리 실패: code={}, error={}",
                        productDto.getCode(), ex.toString());

            }
        }
        log.info("[crawlAndSetPriceStock] 전체 상품 처리 완료");
    }

    @Transactional
    public void manualPriceStockUpdate(
            ManualPriceStockUpdateRequest request
    ) {
        List<ProductUpdateRequest> requests = request.getItems().stream()
                .map(item -> {
                    ProductDto productDto = ProductDto.builder()
                            .code(item.getCode())
                            .salePrice(item.getSalePrice())
                            .stock(item.getStock())
                            .sellerProductId(item.getSellerProductId())
                            .vendorItemId(item.getVendorItemId())
                            .smartstoreId(item.getSmartstoreId())
                            .originProductNo(item.getOriginProductNo())
                            .elevenstId(item.getElevenstId())
                            .build();
                    return new ProductUpdateRequest(
                            item.getCode(), productDto,
                            item.isPriceChanged(), item.isStockChanged()
                    );
                })
                .collect(Collectors.toList());

        /*updateProductsBatch(requests, null);*/
    }



    public void crawlAndUpdateProductsIndividually(
            Integer marginRate,
            Integer couponRate,
            Integer minMarginPrice,
            List<ProductUpdateRequest> requests
    ) {
        for (ProductUpdateRequest request : requests) {
            try {
                // 1. 크롤링 (외부 API 호출)
                ProductDto productDto = request.getProductDto();
                String prodId = productRepository.findIherbProductIdFromLinkByCode(productDto.getLink());
                String productJson = crawlProductAsJson(prodId);
                IherbProductDto iherbProductDto = IherbProductDto.fromJsonWithLinks(productJson);

                Integer salePrice = calculateSalePrice(
                        marginRate, couponRate, minMarginPrice, productDto.getPackQty(), iherbProductDto);
                Integer stock = Boolean.TRUE.equals(iherbProductDto.getIsAvailableToPurchase()) ? 500 : 0;

                productDto.setSalePrice(salePrice);
                productDto.setStock(stock);

                String batchId = UUID.randomUUID().toString();
                // 2. 단일 상품 업데이트(RabbitMQ 등 메시지 발행 또는 DB 처리)
                /*updateSingleProductInBatch(batchId, request.getCode(), request.getProductDto(),
                        request.isPriceChanged(), request.isStockChanged(), false);*/

                // 3. (선택) 크롤링 간 간단한 delay 추가 (봇 감지 방지)
                Thread.sleep(200); // 0.2초 지연 (Case/SLA에 따라 조정)
            } catch (Exception ex) {
                log.error("[crawlAndUpdateProductsIndividually] 처리 실패: code={}, error={}", request.getCode(), ex.toString());
            }
        }
    }

    @Transactional
    public void crawlAndUpdateBySupplier (
            Integer marginRate, Integer couponRate, Integer minMarginPrice,
            List<ProductUpdateRequest> requests
    ) {
        String batchId = UUID.randomUUID().toString();

        for (ProductUpdateRequest request : requests) {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            // 각 상품에 대해 메세지 발행
            messageQueueService.publishCrawlAndUpdateEachProductBySupplier(
                    marginRate, couponRate, minMarginPrice, request, batchId);
        }

        String batchInitMsg = String.format("%d개 배치 시작", requests.size());
        pss.upsertProcessStatus(batchId, null, createBatchSummaryDetails(requests),
                "UPDATE_PRODUCTS", "PENDING", batchInitMsg);
    }


    /**
     * 실패 이력 복원용 임시 DTO.
     */
    private static class RestoredData {
        final ProductDto productDto;
        final boolean priceChanged;
        final boolean stockChanged;

        RestoredData(ProductDto productDto, boolean priceChanged, boolean stockChanged) {
            this.productDto = productDto;
            this.priceChanged = priceChanged;
            this.stockChanged = stockChanged;
        }
    }

    // ===================================================================================================
    // ===================================================================================================
    // ===================================================================================================
    // ===================================================================================================
    // ===================================================================================================

    /**
     * 여러 상품을 일괄 업데이트하는 배치 진입점
     * - 프론트에서 batchId를 null로 보내면 최초시도, 값으로 보내면 재시도
     * - 각 상품별 개별 처리 호출
     */
    @Transactional(rollbackFor = Exception.class)
    public String updateProductsBatch(
            List<ProductUpdateRequest> requests, String batchId
    ) {
        boolean isRetry = (batchId != null && !batchId.isEmpty());

        // (1) batchId 생성 (최초) 또는 재사용 (재시도)
        if (!isRetry) {
            batchId = UUID.randomUUID().toString();
            log.info("[Batch] 새 배치 시작 - batchId={}, 총 상품 수={}", batchId, requests.size());
        } else {
            log.info("[Batch] 배치 재시도 - batchId={}, 총 상품 수={}", batchId, requests.size());
        }

        // (2) 배치 총괄 상태 초기화 (productCode = null)
        String batchInitMsg = String.format("총 %d개 상품 업데이트 시작", requests.size());
        pss.upsertProcessStatus(batchId, null, createBatchSummaryDetails(requests),
                "BATCH_UPDATE", "PENDING", batchInitMsg);

        // (3) 각 상품별 개별 처리
        int successCount = 0;
        int failCount = 0;
        List<String> failedCodes = new ArrayList<>();

        for (ProductUpdateRequest request : requests) {
            try {
                /*updateSingleProductInBatch(batchId, request.getCode(), request.getProductDto(),
                        request.isPriceChanged(), request.isStockChanged(), isRetry);*/
                successCount++;

                // (4) 진행상황 업데이트 (총괄 row)
                String batchStatusMsg = String.format("%d/%d개 상품 처리 완료 (실패: %d)",
                        successCount + failCount, requests.size(), failCount);
                pss.upsertProcessStatus(batchId, null,  null,
                        "BATCH_UPDATE", "IN_PROGRESS", batchStatusMsg);

            } catch (Exception ex) {
                failCount++;
                failedCodes.add(request.getCode());
                log.error("[Batch] 상품 처리 실패 - batchId={}, code={}, 원인={}",
                        batchId, request.getCode(), ex.getMessage());

                // 개별 상품 실패는 배치 전체를 중단하지 않음
                String batchStatusMsg = String.format("%d/%d개 상품 처리 완료 (실패: %d)",
                        successCount + failCount, requests.size(), failCount);

                pss.upsertProcessStatus(batchId, null, null,
                        "BATCH_UPDATE", "IN_PROGRESS", batchStatusMsg);
            }
        }

        // (5) 배치 최종 상태 업데이트
        String finalStatus = (failCount == 0) ? "SUCCESS" :
                (successCount == 0) ? "FAILED" : "PARTIAL_SUCCESS";
        String finalMessage = String.format("완료: %d/%d (실패: %d)",
                successCount, requests.size(), failCount);

        pss.upsertProcessStatus(batchId, null, null,
                "BATCH_UPDATE", finalStatus, finalMessage);
        log.info("[Batch] 배치 완료 - batchId={}, 성공={}, 실패={}, 실패코드={}",
                batchId, successCount, failCount, failedCodes);
        return batchId;
    }

    /**
     * 배치 내에서 단일 상품 업데이트 처리
     * - batchId는 외부에서 받아서 사용
     * - productCode별 개별 상태 기록
     */
    // 부모 트랜잭션과 독립적인 새 트랜잭션 시작
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateSingleProductInBatch(
            String batchId, String code, ProductDto productDto,
            boolean priceChanged, boolean stockChanged, boolean isRetry
    ) {
        log.info("[Batch][Item] 상품 처리 시작 - batchId={}, code={}", batchId, code);
        log.info("{}, {}, {}, {}", productDto, priceChanged, stockChanged, isRetry);

        String detailsJson = null;

        // ========== 1단계: DB 저장 처리 ==========
        try {
            // (1) 재시도인 경우 이력에서 복원
            if (isRetry) {
                RestoredData restored = restoreFailedRequest(batchId, code);
                productDto = restored.productDto;
                priceChanged = restored.priceChanged;
                stockChanged = restored.stockChanged;
            } else {
                detailsJson = serializeDetails(productDto, priceChanged, stockChanged);
            }

            // (2) 개별 상품 상태 기록 - PENDING
            String msg = isRetry ? "상품 수정 재시도 요청" : "상품 수정 처리 시작";
            pss.upsertProcessStatus(batchId, code, detailsJson,
                    "DB_SAVE", "PENDING", msg);

            // (3) Product, Mapping 조회/생성
            Product product = getProductOrThrow(code);
            ProductChannelMapping mapping = findOrCreateChannelMapping(code);

            // (4) 필드 업데이트 및 DB 저장
            updateProductFields(product, productDto);
            updateChannelMappingFields(mapping, productDto);
            productRepository.save(product);
            mappingRepository.save(mapping);
            // REQUIRES_NEW 트랜잭션이므로 즉시 커밋

            // (5) 개별 상품 DB 저장 완료 상태 기록
            pss.upsertProcessStatus(batchId, code, null,
                    "DB_SAVE", "SUCCESS", "상품 정보 DB 저장 완료");
        } catch (Exception ex) {
            log.error("[Batch][Item] 상품 처리 실패 - batchId={}, code={}, 원인={}",
                    batchId, code, ex.getMessage());
            pss.upsertProcessStatus(batchId, code, null,
                    "DB_SAVE", "FAILED", ex.getMessage());
            throw ex; // 트랜잭션 롤백 (REQUIRES_NEW이므로 이 상품만)
        }

        // ========== 2단계: 채널 동기화 처리 (별도 try-catch) ==========
        try {
            // (6) 채널 동기화 상태로 전환
            pss.upsertProcessStatus(batchId, code, null,
                    "CHANNEL_UPDATE", "PENDING", "채널 동기화 대기");

            // (7) 메시지 발행 (기존 로직 유지)
            Product product = getProductOrThrow(code);
            ProductChannelMapping mapping = findOrCreateChannelMapping(code);

            if (needCoupangSync(mapping, priceChanged, stockChanged)) {
                if (mapping.getVendorItemId() == null || mapping.getVendorItemId().isBlank()) {
                    // vendorItemId가 없으면, 조회해서 DB에 저장 후 메시지 발행
                    messageQueueService.publishVendorItemIdSync(
                            mapping, product, batchId, priceChanged, stockChanged);
                } else {
                    // vendorItemId가 있으면, 메시지 발행
                    /*if (priceChanged) messageQueueService.publishPriceUpdate(
                            "coupang", product, mapping, batchId);
                    if (stockChanged) messageQueueService.publishStockUpdate(
                            "coupang", product, mapping, batchId);*/
                }
            }
            // syncToChannelsForOtherChannels(mapping, product, batchId, priceChanged, stockChanged);

            log.info("[Batch][Item] 상품 처리 완료 - batchId={}, code={}", batchId, code);

        } catch (Exception ex) {
            // 메시지 발행 실패 - DB는 이미 저장됨
            log.error("[Batch][Item] 메시지 발행 실패 - batchId={}, code={}, 원인={}",
                    batchId, code, ex.getMessage(), ex);
            pss.upsertProcessStatus(batchId, code, null,
                    "CHANNEL UPDATE", "FAILED", "메시지 발행 실패: " + ex.getMessage());
            // 메시지 발행 실패는 예외를 다시 던지지 않음 (DB는 이미 성공)
            // 나중에 재시도하거나, Dead Letter Queue로 처리
        }
    }

    /**
     * 배치 요청 정보를 JSON으로 직렬화
     */
    private String createBatchSummaryDetails(
            List<ProductUpdateRequest> requests
    ) {
        // {"totalCount": 2000, "productCodes": [...], "timestamp":"2025-10-09T18:45:22.0733536"}
        Map<String, Object> summary = Map.of(
                "totalCount", requests.size(),
                "productCodes", requests.stream()
                        .map(ProductUpdateRequest::getCode)
                        .collect(Collectors.toList()),
                "timestamp", LocalDateTime.now()
        );
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (Exception e) {
            return "{}";
        }
    }












    @Transactional(rollbackFor = Exception.class)
    public String crawlAndUpdateProductsBatch(
            List<ProductUpdateRequest> requests, String batchId
    ) {
        boolean isRetry = (batchId != null && !batchId.isEmpty());

        // (1) batchId 생성 (최초) 또는 재사용 (재시도)
        if (!isRetry) {
            batchId = UUID.randomUUID().toString();
            log.info("[Batch] 새 배치 시작 - batchId={}, 총 상품 수={}", batchId, requests.size());
        } else {
            log.info("[Batch] 배치 재시도 - batchId={}, 총 상품 수={}", batchId, requests.size());
        }

        // (2) 배치 총괄 상태 초기화 (productCode = null)
        String batchInitMsg = String.format("총 %d개 상품 업데이트 시작", requests.size());
        pss.upsertProcessStatus(batchId, null, createBatchSummaryDetails(requests),
                "BATCH_UPDATE", "PENDING", batchInitMsg);

        // (3) 각 상품별 개별 처리
        int successCount = 0;
        int failCount = 0;
        List<String> failedCodes = new ArrayList<>();

        for (ProductUpdateRequest request : requests) {
            try {
                updateSingleProductInBatch(batchId, request.getCode(), request.getProductDto(),
                        request.isPriceChanged(), request.isStockChanged(), isRetry);
                successCount++;

                // (4) 진행상황 업데이트 (총괄 row)
                String batchStatusMsg = String.format("%d/%d개 상품 처리 완료 (실패: %d)",
                        successCount + failCount, requests.size(), failCount);
                pss.upsertProcessStatus(batchId, null,  null,
                        "BATCH_UPDATE", "IN_PROGRESS", batchStatusMsg);

            } catch (Exception ex) {
                failCount++;
                failedCodes.add(request.getCode());
                log.error("[Batch] 상품 처리 실패 - batchId={}, code={}, 원인={}",
                        batchId, request.getCode(), ex.getMessage());

                // 개별 상품 실패는 배치 전체를 중단하지 않음
                String batchStatusMsg = String.format("%d/%d개 상품 처리 완료 (실패: %d)",
                        successCount + failCount, requests.size(), failCount);

                pss.upsertProcessStatus(batchId, null, null,
                        "BATCH_UPDATE", "IN_PROGRESS", batchStatusMsg);
            }
        }

        // (5) 배치 최종 상태 업데이트
        String finalStatus = (failCount == 0) ? "SUCCESS" :
                (successCount == 0) ? "FAILED" : "PARTIAL_SUCCESS";
        String finalMessage = String.format("완료: %d/%d (실패: %d)",
                successCount, requests.size(), failCount);

        pss.upsertProcessStatus(batchId, null, null,
                "BATCH_UPDATE", finalStatus, finalMessage);
        log.info("[Batch] 배치 완료 - batchId={}, 성공={}, 실패={}, 실패코드={}",
                batchId, successCount, failCount, failedCodes);
        return batchId;
    }














}