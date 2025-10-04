package com.jason.purchase_agent.service.product_registration;

import com.jason.purchase_agent.dto.categories.CategoryTreeDto;
import com.jason.purchase_agent.dto.process_status.ProcessStatusDto;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationDto;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRetryMessage;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRetryRequest;
import com.jason.purchase_agent.external.iherb.IherbCategoryCrawler;
import com.jason.purchase_agent.external.iherb.IherbProductCrawler;
import com.google.gson.Gson;
import com.jason.purchase_agent.external.iherb.dto.IherbProductDto;
import com.jason.purchase_agent.repository.jpa.CategoryRepository;
import com.jason.purchase_agent.repository.jpa.ProcessStatusRepository;
import com.jason.purchase_agent.repository.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRegistrationService {

    private final CategoryRepository categoryRepository; // 카테고리 DB 테이블 엑세스
    private final ProductRepository productRepository; // 상품 DB 테이블 엑세스
    private final IherbProductCrawler iherbProductCrawler; // iHerb 크롤러
    private final IherbCategoryCrawler iherbCategoryCrawler;
    private final ProcessStatusRepository processStatusRepository;
    private static final Gson gson = new Gson();
    private Double BASE_MARGIN_RATE = 23.0;

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
        log.info("■ [STEP 2] □ 서비스 PARAMETER :: List<CategoryTreeDto> categoryTreeDtos");

        // 1. IHB상품번호 누적용 빈 리스트
        List<String> productIds = new ArrayList<>();

        // 2. IherbProductDto를 담을 빈 리스트
        List<IherbProductDto> iherbProductDtos = new ArrayList<>();

        // 3. Product 테이블에 이미 등록된 IHB상품번호 SET
        Set<String> registeredProductIdsFromProduct =
                new HashSet<>(productRepository.findAllProdIdsByIherbFromSourceLink());

        // 4. 카테고리별로 상품수에 따라 상품 크롤링
        for (CategoryTreeDto cat : categoryTreeDtos) {
            Integer sortOrder = cat.getSortOrder();
            // 바로 카테고리별 상품 ID 리스트 얻기
            List<String> ids = iherbCategoryCrawler.getTopProductIds(
                    cat.getLink(), cat.getProductCount(), sortOrder
            );
            cat.setProductIds(ids);
        }

        // 5. 수집한 링크 각각에 대해
        for (CategoryTreeDto cat : categoryTreeDtos) {
            productIds = cat.getProductIds();
            for (String productId : productIds) {

                // 5-1) 이미 등록된 제품인 경우 continue (skip)
                if (registeredProductIdsFromProduct.contains(productId)) continue;

                // 5-2) 크롤러 차단/딜레이 우회 (2~7초)
                try {
                    Thread.sleep(iherbProductCrawler.getRandomDelay());
                } catch (Exception ignored) {
                }

                // 5-3) OkHttp 등으로 상품 json 수집 및 파싱
                try {
                    // String 형태의 json 수집
                    String productJson = iherbProductCrawler.crawlProductAsJson(productId);

                    // String 형태의 json -> IherbProductDto로 변환
                    IherbProductDto dto = IherbProductDto.fromJsonWithLinks(productJson);

                    // supplementFacts, ingredients의 "를 '로 치환
                    dto.setSupplementFacts(
                            dto.getSupplementFacts() == null ? null : dto.getSupplementFacts().replace("\"", "'")
                    );
                    dto.setIngredients(
                            dto.getIngredients() == null ? null : dto.getIngredients().replace("\"", "'")
                    );

                    // 각 상품에 categoryName, categoryPath 세팅
                    dto.setUserCategoryName(cat.getName());
                    dto.setUserCategoryPath(cat.getPath());

                    // iherbProductDtos 리스트에 담기
                    iherbProductDtos.add(dto);
                } catch (Exception e) {
                    log.error("상품 상세 크롤링 실패: {} ({})", productId, e.getMessage());
                }
            }
        }

        log.info("■ [STEP 2] □ 서비스 RETURN :: List<IherbProductDtos> iherbProductDtos");

        return iherbProductDtos;
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
    public List<ProductRegistrationDto> convertToProductRegistrationDto(
            List<IherbProductDto> selectedIherbProductDtos
    ) {

        List<ProductRegistrationDto> productRegistrationDtos = new ArrayList<>();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String codePrefix = today + "IHB";

        // 1. 오늘자 prefix로 시작하는 기등록 상품코드 중 가장 큰 seq 찾기
        int maxSeq = productRepository.findMaxSeqByCodePrefix(codePrefix);
        int seq = maxSeq + 1; // 신규 등록은 다음 번호부터
        // 3. 각 상품(IherbProductDto)에 대하여 ProductRegistrationDto 변환
        for (IherbProductDto iherbDto : selectedIherbProductDtos) {

            ProductRegistrationDto dto = new ProductRegistrationDto();

            // 1. code : 상품코드 (예: 250910IHB001) - 날짜+타입+세자리번호
            String code = today + "IHB" + String.format("%03d", seq++);
            dto.setCode(code);

            // 2. link : 상품원본링크
            dto.setLink(iherbDto.getUrl());

            // 3. unitValue, unit : 제품의 개당 단위
            String packageQuantity = iherbDto.getPackageQuantity();
            String unit = "";
            int unitValue = 0;

            if (packageQuantity != null) {
                // 1. 패턴: 소수점 포함 (공백 구분, 단위 포함)
                // ex: "946.733 ml", "500 개", "180 정"
                Pattern pat = Pattern.compile("^([\\d]+)(?:\\.\\d+)?\\s*([a-zA-Z가-힣]+)?$");
                Matcher m = pat.matcher(packageQuantity.trim());
                if (m.find()) {
                    String numStr = m.group(1); // 정수부분
                    String unitStr = m.group(2) != null ? m.group(2) : ""; // 단위

                    unitValue = Integer.parseInt(numStr);
                    unit = unitStr;
                }
            }
            String korName = iherbDto.getDisplayName();
            if (korName != null && unitValue > 0) {
                Pattern unitPat = Pattern.compile(unitValue + "(정|캡슐|포|알|타블릿)");
                Matcher unitM = unitPat.matcher(korName);
                if (unitM.find()) {
                    unit = unitM.group(1); // korName에서 찾은 실제 단위로 변경
                }
            }
            dto.setUnitValue(unitValue);  // 예: 946
            dto.setUnit(unit);            // 예: "ml"

            // 4. buyPrice : 실구매가(Iherb 기준 온라인 할인가 기준)
            dto.setBuyPrice(iherbDto.getDiscountPriceAmount());

            // 5. packQty, salePrice : 패킹수량, 최종판매가 산정
            //    - 패킹수와 마진을 조정해 4만원 이상 대의 가격을 맞추고, 가장 근접한 결과 반환
            Double discountPrice = iherbDto.getDiscountPriceAmount();
            int packQty = 1;          // 기본 패킹수량
            int salePrice = 0;          // 계산된 최종판매가
            double margin = (1 + (BASE_MARGIN_RATE/100.0));        // 최초 마진율(23%) : 기본값

            for (int q = 1; q <= 1000; q++) {
                // 4만원 이상 대의 가격이 나오도록 패킹수량 증가
                double price = Math.round(discountPrice * q * margin / 100.0) * 100; // 백원단위까지 반올림
                if (price >= 40000) {
                    packQty = q;
                    salePrice = (int) price;
                    break;
                }
            }
            dto.setSalePrice(salePrice);
            dto.setPackQty(packQty);

            // 6. brandName : 브랜드명
            //    - brandName 컬럼값 수정 : '영문 (한글)' 형식이라면 '한글'로 수정
            String brandName = iherbDto.getBrandName();
            if (brandName != null && brandName.matches(".*\\(.*\\).*")) {
                // 괄호 안 내용 전체를 추출 (한글, 영문, 숫자, 공백 모두 OK)
                String inside = brandName.replaceAll(".*\\(([^)]*)\\).*", "$1").trim();
                dto.setBrandName(inside);
            }

            // 7. korName, engName, title : 상품명
            dto.setEngName(iherbDto.getDisplayEngName());
            dto.setKorName(iherbDto.getDisplayName());
            dto.setTitle(iherbDto.getDisplayName()); // 자바스크립트에서 패킹수량, 브랜드명과 조합

            // 8. stock : 재고
            dto.setStock(iherbDto.getIsAvailableToPurchase() ? 500 : 0);

            // 9. detailsHtml : 상세페이지(HTML)
            dto.setDetailsHtml(null); // 추후 최종 등록 단계에서 수정

            // 10. imageLinks : 이미지링크
            dto.setImageLinks(iherbDto.getImageLinks());

            // 11. uploadedImageLinks : 업로드된 이미지링크
            dto.setUploadedImageLinks(null); // 추후 최종 등록 단계에서 수정

            // 12. ingredients : 성분표
            dto.setIngredients(iherbDto.getIngredients());

            // 13. supplementFacts : 상품 설명
            dto.setSupplementFacts(iherbDto.getSupplementFacts());

            // 14. productType : 고시정보 구분
            String path = iherbDto.getUserCategoryPath();
            String productType = null;
            if (path != null && path.length() >= 4) {
                String firstPath = path.substring(0, 4);
                if ("0001".equals(firstPath) || "0002".equals(firstPath)) {
                    productType = "HEALTH";
                } else if ("0003".equals(firstPath)) {
                    productType = "FOOD";
                }
                // else는 (필요하다면) productType = "ETC" 등으로 세팅
            }
            dto.setProductType(productType);

            // 15. categoryNames : 카테고리리스트
            List<List<IherbProductDto.CanonicalPath>> canonicalPaths = iherbDto.getCanonicalPaths();
            List<String> categoryNames = canonicalPaths == null ? Collections.emptyList() :
                    canonicalPaths.stream()
                            .flatMap(list -> list.stream())                  // List<List<..>> → List<..> 평탄화
                            .map(IherbProductDto.CanonicalPath::getDisplayName)              // displayName만 추출
                            .filter(Objects::nonNull)                        // null 값 필터
                            .collect(Collectors.toList());

            dto.setCategoryNames(categoryNames);

            // 16. marginRate
            dto.setMarginRate(BASE_MARGIN_RATE); // 최초 마진율 설정

            // 16. 생성된 DTO를 결과 리스트에 추가
            productRegistrationDtos.add(dto);
        }

        // 완성된 오픈마켓 등록 상품 DTO 리스트 반환 (화면 렌더/유저 수정/최종 등록 모두 사용)
        return productRegistrationDtos;
    }

    // 전체 현황
    public List<ProcessStatusDto> findAllStatuses() {
        // 모든 ProcessStatus 엔티티를 조회해서 DTO로 변환
        return processStatusRepository.findAll().stream()
                .map(ProcessStatusDto::fromEntity)
                .collect(Collectors.toList());
    }


}