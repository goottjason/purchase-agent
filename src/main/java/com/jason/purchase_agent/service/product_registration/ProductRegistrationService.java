package com.jason.purchase_agent.service.product_registration;

import com.jason.purchase_agent.dto.categories.CategoryTreeDto;
import com.jason.purchase_agent.dto.process_status.ProcessStatusDto;
import com.jason.purchase_agent.dto.product_registration.ProductImageUploadResult;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import com.jason.purchase_agent.dto.products.ProductDto;
import com.jason.purchase_agent.external.iherb.IherbCategoryCrawler;
import com.jason.purchase_agent.external.iherb.IherbProductCrawler;
import com.google.gson.Gson;
import com.jason.purchase_agent.external.iherb.dto.IherbProductDto;
import com.jason.purchase_agent.messaging.MessageQueueService;
import com.jason.purchase_agent.repository.jpa.CategoryRepository;
import com.jason.purchase_agent.repository.jpa.ProcessStatusRepository;
import com.jason.purchase_agent.repository.jpa.ProductRepository;
import com.jason.purchase_agent.service.process_status.ProcessStatusService;
import com.jason.purchase_agent.util.downloader.ImageDownloader;
import com.jason.purchase_agent.util.uploader.UploadImagesApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jason.purchase_agent.external.iherb.IherbProductCrawler.crawlProductAsJson;
import static com.jason.purchase_agent.util.JsonUtils.safeJsonString;
import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;
import static com.jason.purchase_agent.util.exception.ExceptionUtils.uncheck;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRegistrationService {
    private final UploadImagesApi uploadImagesApi;
    private final CategoryRepository categoryRepository; // 카테고리 DB 테이블 엑세스
    private final ProductRepository productRepository; // 상품 DB 테이블 엑세스
    private final IherbProductCrawler iherbProductCrawler; // iHerb 크롤러
    private final IherbCategoryCrawler iherbCategoryCrawler;
    private final ProcessStatusRepository psr;
    private final ProcessStatusService pss;
    private static final Gson gson = new Gson();
    private final MessageQueueService messageQueueService;
    private Double BASE_MARGIN_RATE = 20.0;

    /**
     * [Step 2]
     * 카테고리별로 사용자가 요청한 개수만큼 외부 마켓에서 상품을 수집하여 반환하는 메서드.
     *
     * @param categoryTreeDtos - 카테고리별 id/상품수 리스트(사용자 선택값)
     * @return List<IherbProductDto> - 각 상품에 대한 상세 DTO 목록 (중복/등록 제외)
     */
    public List<IherbProductDto> fetchProducts(
            List<CategoryTreeDto> categoryTreeDtos
    ) {
        // 1. 이미 등록된 IHB 상품번호 SET
        Set<String> registeredProductIds = new HashSet<>(productRepository.findAllProdIdsByIherbFromSourceLink());
        // 2. 크롤링 결과 담을 리스트
        List<IherbProductDto> resultDtos = new ArrayList<>();

        // 3. 카테고리별 크롤링 & 상품정보 수집
        for (CategoryTreeDto category : categoryTreeDtos) {
            // IHB 상품번호 크롤링 + categoryTreeDto에 set
            List<String> productIds = iherbCategoryCrawler.getTopProductIds(
                    category.getLink(), category.getProductCount(), category.getSortOrder());
            category.setProductIds(productIds);

            for (String productId : productIds) {
                // 이미 등록된 제품은 skip
                if (registeredProductIds.contains(productId)) continue;
                // 크롤러 차단/딜레이 우회
                try { Thread.sleep(iherbProductCrawler.getRandomDelay()); } catch (InterruptedException ignored) {}
                // 상품 JSON → DTO 변환, 예외처리
                try {
                    String productJson = iherbProductCrawler.crawlProductAsJson(productId);
                    IherbProductDto iherbDto = IherbProductDto.fromJsonWithLinks(productJson);

                    // 사용자 지정 카테고리 정보 세팅
                    iherbDto.setUserCategoryName(category.getName());
                    iherbDto.setUserCategoryPath(category.getPath());

                    resultDtos.add(iherbDto);
                } catch (Exception e) {
                    log.error("상품 상세 크롤링 실패: {} ({})", productId, e.toString());
                }
            }
        }
        return resultDtos;
    }


    /**
     * [Step 3]
     * 선택된 상품 리스트를 받아, 오픈마켓 등록을 위한 최종 상품 등록 폼(EnrollProductFormDto) 리스트를 생성한다.
     * - 각 DTO는 실제로 사용자가 수정 후 제출하면 쿠팡·스마트스토어 등 마켓 등록에 바로 사용할 수 있는 완성형 형태임
     * - 마켓별 정책이나 UI 요구에 맞는 필드값/가공/가격설정 로직을 포함한다.
     *
     * @param selectedIherbProductDtos 선택된 Iherb 상품 DTO 리스트 (상품정보 원본)
     * @return 오픈마켓용 최종 등록 정보 리스트 (EnrollProductFormDto)
     */
    public List<ProductRegistrationRequest> convertToProductRegistrationDto(
            List<IherbProductDto> selectedIherbProductDtos
    ) {
        List<ProductRegistrationRequest> requests = new ArrayList<>();

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String codePrefix = today + "IHB";

        int seq = productRepository.findMaxSeqByCodePrefix(codePrefix) + 1;

        for (IherbProductDto iherbDto : selectedIherbProductDtos) {
            ProductRegistrationRequest request = new ProductRegistrationRequest();
            ProductDto productDto = new ProductDto();
            request.setProductDto(productDto);
            // 1. 상품 코드
            productDto.setCode("%sIHB%03d".formatted(today, seq++));
            productDto.setSupplierCode("IHB");
            productDto.setWeight(parseWeight(iherbDto.getActualWeight()));
            productDto.setShippingCost(0.0);
            // 2. 원본 링크
            productDto.setLink(iherbDto.getUrl());
            // 3. 패키지 단위 자동 파싱
            parseUnitAndValue(iherbDto.getPackageQuantity(), iherbDto.getDisplayName(), productDto);
            // 4. 실구매가
            productDto.setBuyPrice(iherbDto.getDiscountPriceAmount());
            // 5. 가격 산정 및 패킹수량
            setPackQtyAndSalePrice(iherbDto.getDiscountPriceAmount(), productDto);
            // 6. 브랜드명(한글만)
            String brandName = iherbDto.getBrandName();
            productDto.setBrandName((brandName != null && brandName.matches(".*\\(.*\\).*"))
                    ? brandName.replaceAll(".*\\(([^)]*)\\).*", "$1").trim()
                    : brandName);
            // 7~9. 각종 이름, 타이틀, 재고
            productDto.setEngName(iherbDto.getDisplayEngName());
            productDto.setKorName(iherbDto.getDisplayName());
            productDto.setTitle(iherbDto.getDisplayName());
            productDto.setStock(Boolean.TRUE.equals(iherbDto.getIsAvailableToPurchase()) ? 500 : 0);
            // 10~13. 기타 상세 정보
            productDto.setDetailsHtml(null);
            String imageLinksJson = uncheck(() -> objectMapper.writeValueAsString(iherbDto.getImageLinks()));
            productDto.setImageLinks(imageLinksJson);
            request.setImageLinks(iherbDto.getImageLinks());
            request.setUploadedImageLinks(null);
            // 14. 상품 타입(카테고리 기반 분류)
            productDto.setProductType(parseProductType(iherbDto.getUserCategoryPath()));
            // 15. 카테고리명 리스트(Fast flatMap)
            request.setCategoryNames(Optional.ofNullable(iherbDto.getCanonicalPaths())
                    .map(paths -> paths.stream()
                            .flatMap(List::stream)
                            .map(IherbProductDto.CanonicalPath::getDisplayName)
                            .filter(Objects::nonNull)
                            .toList())
                    .orElseGet(Collections::emptyList));

            // 16. 마진율
            productDto.setMarginRate(BASE_MARGIN_RATE);

            requests.add(request);
        }

        return requests;
    }


    // ----------- 보조 메서드 --------------
    private Double parseWeight(String actualWeight) {
        if (actualWeight == null || actualWeight.trim().isEmpty()) {
            return null;
        }
        try {
            // 숫자와 소수점만 남기고 모두 제거
            String numericWeight = actualWeight.replaceAll("[^0-9.]", "");
            return numericWeight.isEmpty() ? null : Double.parseDouble(numericWeight);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void parseUnitAndValue(String packageQuantity, String korName, ProductDto productDto) {
        // 기본값 초기화 (필수)
        productDto.setUnit("");
        productDto.setUnitValue(0);
        if (packageQuantity != null) {
            Pattern pat = Pattern.compile("^([\\d]+)(?:\\.\\d+)?\\s*([a-zA-Z가-힣]+)?$");
            Matcher m = pat.matcher(packageQuantity.trim());
            if (m.find()) {
                productDto.setUnitValue(Integer.parseInt(m.group(1)));
                productDto.setUnit(Optional.ofNullable(m.group(2)).orElse(""));
            }
            // korName에서 단위 재확인
            if (korName != null && productDto.getUnitValue() > 0) {
                Pattern unitPat = Pattern.compile(productDto.getUnitValue() + "(정|캡슐|포|알|타블릿)");
                Matcher unitM = unitPat.matcher(korName);
                if (unitM.find()) {
                    productDto.setUnit(unitM.group(1));
                }
            }
        } else {
            productDto.setUnit("");
            productDto.setUnitValue(0);
        }
    }

    private void setPackQtyAndSalePrice(Double price, ProductDto productDto) {
        int baseQty = 1;
        int sale = 0;
        double margin = (1 + (BASE_MARGIN_RATE / 100.0));
        for (int q = 1; q <= 1000; q++) {
            double result = Math.round(price * q * margin / 100.0) * 100;
            if (result >= 40000) {
                baseQty = q;
                sale = (int) result;
                break;
            }
        }
        productDto.setPackQty(baseQty);
        productDto.setSalePrice(sale);
    }

    private String parseProductType(String path) {
        if (path != null && path.length() >= 4) {
            String firstPath = path.substring(0, 4);
            if ("0001".equals(firstPath) || "0002".equals(firstPath)) return "HEALTH";
            if ("0003".equals(firstPath)) return "FOOD";
        }
        return null;
    }


    // 전체 현황
    public List<ProcessStatusDto> findAllStatuses() {
        // 모든 ProcessStatus 엔티티를 조회해서 DTO로 변환
        return psr.findAll().stream()
                .map(ProcessStatusDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Async
    public void registerProducts(
            List<ProductRegistrationRequest> requests
    ) {
        String batchId = UUID.randomUUID().toString();

        // 이미지 로컬에 다운로드
        for (ProductRegistrationRequest request : requests) {
            // 이미지 로컬에 다운로드
            List<String> localPaths = ImageDownloader.downloadImagesToLocal(
                    request.getProductDto().getCode(), request.getImageLinks());
            request.setImageFiles(localPaths);
        }

        // 이미지 업로드
        uploadImages(requests);

        // 메세지 발행
        for (ProductRegistrationRequest request : requests) {
            messageQueueService.publishRegisterEachProduct(request, batchId);
        }
        String batchInitMsg = String.format("%d개 배치 시작", requests.size());
        pss.upsertProcessStatus(batchId, null, null,
                "UPDATE_PRODUCTS", "PENDING", batchInitMsg);

    }

    public void uploadImages(List<ProductRegistrationRequest> requests
    ) {

        // 업로드 요청용 맵 리스트 생성
        List<Map<String, Object>> imageUploadRequests = requests.stream()
                .map(request ->
                        Map.of("code", request.getProductDto().getCode(), "imageFiles", request.getImageFiles()))
                .collect(Collectors.toList());

        // 이미지 업로드 시도
        try {
            // 이미지 업로드 API 호출
            List<ProductImageUploadResult> results = uploadImagesApi.batchUploadImages(imageUploadRequests);

            // 결과 매핑
            Map<String, List<String>> codeToLinks = new HashMap<>();
            for (ProductImageUploadResult result : results) {
                codeToLinks.put(result.getCode(), result.getUploadedImageLinks());
            }
            for (ProductRegistrationRequest request : requests) {
                // 업로드된 링크가 있으면 세팅, 없으면 실패 처리
                List<String> uploadedImageLinks = codeToLinks.get(request.getProductDto().getCode());
                if (uploadedImageLinks != null && !uploadedImageLinks.isEmpty()) {
                    // 업로드된 링크 세팅
                    request.setUploadedImageLinks(uploadedImageLinks);
                }
            }
        } catch (Exception e) {
        }
    }

}