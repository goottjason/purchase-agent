package com.jason.purchase_agent.external.coupang;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.channel.coupang.CoupangApiResponse;
import com.jason.purchase_agent.dto.channel.coupang.CoupangCategoryMetaInfoDto;
import com.jason.purchase_agent.dto.channel.coupang.CoupangProductRequest;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationDto;
import com.jason.purchase_agent.util.salechannelapi.coupang.CoupangApiUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jason.purchase_agent.util.salechannelapi.coupang.CoupangApiUtil.executeRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoupangApiService {

    public String findProductInfo(String sellerProductId) {
        log.info("[CoupangAPI][ProductInfo] 상품 조회 요청 - sellerProductId={}", sellerProductId);
        try {
            String path = String.format("/v2/providers/seller_api/apis/api/v1/marketplace/seller-products/%s", sellerProductId);
            log.debug("[CoupangAPI][ProductInfo] 요청 경로: {}", path);

            // 기존 executeRequest를 GET메서드로 활용
            String response = executeRequest("GET", path, null, null);
            log.debug("[CoupangAPI][ProductInfo] API 원본 응답: {}", response);

            return response;
        } catch (Exception e) {
            log.error("[CoupangAPI][ProductInfo] 요청/파싱 에러 - sellerProductId={}, 원인={}", sellerProductId, e.getMessage(), e);
            // 필요시 에러 JSON etc
            return "{}";
        }
    }


    public String updatePrice(String vendorItemId, Integer salePrice) {
        log.info("[CoupangAPI][Price] 가격 변경 요청 - vendorItemId={}, salePrice={}", vendorItemId, salePrice);

        try {
            String path = String.format(
                    "/v2/providers/seller_api/apis/api/v1/marketplace/vendor-items/%s/prices/%d",
                    vendorItemId, salePrice);
            log.debug("[CoupangAPI][Price] 요청 경로: {}", path);

            String response = executeRequest("PUT", path, null, null);
            log.debug("[CoupangAPI][Price] API 원본 응답: {}", response);

            return response;
        } catch (Exception e) {
            log.error("[CoupangAPI][Price] 요청/파싱 에러 - vendorItemId={}, price={}, 원인={}",
                    vendorItemId, salePrice, e.getMessage());
            // 필요시 에러 JSON etc
            return "{}";
        }
    }
    public String updateStock(String vendorItemId, Integer stock) {
        log.info("[CoupangAPI][Stock] 재고 변경 요청 - vendorItemId={}, stock={}", vendorItemId, stock);

        try {
            String path = String.format(
                    "/v2/providers/seller_api/apis/api/v1/marketplace/vendor-items/%s/quantities/%d",
                    vendorItemId, stock);
            log.debug("[CoupangAPI][Stock] 요청 경로: {}", path);

            String response = executeRequest("PUT", path, null, null);
            log.debug("[CoupangAPI][Stock] API 원본 응답: {}", response);

            return response;
        } catch (Exception e) {
            log.error("[CoupangAPI][Stock] 요청/파싱 에러 - vendorItemId={}, stock={}, 원인={}",
                    vendorItemId, stock, e.getMessage());
            return "{}";
        }
    }


    public void updatePriceStock(String vendorItemId, Integer salePrice, Integer stock) {
        String pathPrice = String.format("/v2/providers/seller_api/apis/api/v1/marketplace/vendor-items/%s/prices/%d",
                vendorItemId, salePrice);
        String resPrice = executeRequest("PUT", pathPrice, null, null);
        System.out.println("resPrice = " + resPrice);
        String pathStock = String.format("/v2/providers/seller_api/apis/api/v1/marketplace/vendor-items/%s/quantities/%d",
                vendorItemId, stock);
        String resStock = executeRequest("PUT", pathStock, null, null);
        System.out.println("resStock = " + resStock);
    }

    public String enrollProducts(ProductRegistrationDto product) {
        // 쿠팡 상품등록 API 호출 로직 구현

        // 1. CoupangProductRequest의 팩토리메서드로 DTO 생성
        CoupangProductRequest.CoupangProductRequestBuilder coupangProductRequest
                = CoupangProductRequest.from(product).toBuilder();

        // 2. product의 정보를 토대로 카테고리추천 API 호출, requestBuilder의 displayCategoryCode에 세팅
        String categoryId = null;
        try {
            categoryId = recommendDisplayCategory(product);
        } catch (JsonProcessingException e) {
            // 예외처리: 기본 카테고리 세팅 또는 에러 반환
            e.printStackTrace();
            return "Error: 카테고리 추천 실패 - " + e.getMessage();
        }
        coupangProductRequest.displayCategoryCode(Long.parseLong(categoryId));

        // 3. 카테고리 메타정보 API 호출 (필수 고시정보, 속성 등)
        CoupangCategoryMetaInfoDto metaInfo = null;
        try {
            metaInfo = fetchCategoryMetaInfo(Long.parseLong(categoryId));
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: 카테고리 메타정보 조회 실패 - " + e.getMessage();
        }

        // 4. Notice (고시정보) 세팅
        List<CoupangProductRequest.Notice> notices;
        if ("HEALTH".equalsIgnoreCase(product.getProductType())) {
            notices = CoupangProductRequest.HEALTH_FUNCTIONAL_NOTICES;
        } else {
            notices = CoupangProductRequest.PROCESSED_FOOD_NOTICES;
        }

        // 5. Attributes (옵션/속성) 세팅
        List<CoupangProductRequest.Attribute> attributes = buildRequiredAttributes(metaInfo, product);

        // 6. 이미지 세팅
        List<CoupangProductRequest.Image> images = buildImageList(product.getUploadedImageLinks());

        // 7. 상세페이지(contents) 생성 (이미지와 간단한 상품 요약 설명)
        List<CoupangProductRequest.Content> contents = buildProductContents(product);

        // 8. 아이템이름 세팅
        String itemName = buildItemName(product);

        // List<CoupangProductRequest.Certification> certifications = buildCertifications(product);

        // 9. Item(옵션) 객체 완성
        CoupangProductRequest.Item item = CoupangProductRequest.Item.builder()
                .itemName(itemName)
                .originalPrice((int) (Math.round(product.getSalePrice() * (1 + product.getMarginRate()/100.0) / 100.0) * 100))
                .salePrice(product.getSalePrice())
                .unitCount(product.getPackQty() > 0 ? product.getPackQty() : 1)
                .externalVendorSku(product.getCode())
                .images(images)
                .notices(notices)
                .attributes(attributes)
                .contents(contents)
                // .certifications(certifications)
                .build();

        // 10. Request DTO 최종 빌드
        CoupangProductRequest request = coupangProductRequest.items(List.of(item)).build();

        // 11. 최종 API 호출
        String path = "/v2/providers/seller_api/apis/api/v1/marketplace/seller-products";
        String method = "POST";
        Map<String, String> params = null; // 상품등록 API는 쿼리 파라미터 없음

        // request를 Map 변환 (Jackson 사용)
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> body = mapper.convertValue(request, Map.class);

        return executeRequest(method, path, params, body);
    }

    public String recommendDisplayCategory(ProductRegistrationDto product) throws JsonProcessingException {
        String path = "/v2/providers/openapi/apis/api/v1/categorization/predict";
        Map<String, Object> body = new HashMap<>();
        body.put("productName", product.getKorName());
        body.put("brand", product.getBrandName());
        String response = executeRequest("POST", path, null, body);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode dataNode = rootNode.path("data");
        String predictedCategoryId = dataNode.path("predictedCategoryId").asText();

        return predictedCategoryId;
    }

    public CoupangCategoryMetaInfoDto fetchCategoryMetaInfo(Long categoryId) throws IOException {
        String path = "/v2/providers/seller_api/apis/api/v1/marketplace/meta/category-related-metas/display-category-codes/" + categoryId;
        String response = executeRequest("GET", path, null, null);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode dataNode = rootNode.path("data");

        CoupangCategoryMetaInfoDto coupangCategoryMetaInfoDto =
                    dataNode.traverse(objectMapper).readValueAs(CoupangCategoryMetaInfoDto.class);
        return coupangCategoryMetaInfoDto;
    }


    private String buildItemName(ProductRegistrationDto product) {
        StringBuilder sb = new StringBuilder();

        // 단위/수량이 명확한 경우 (ex: 453ml_3개)
        if (product.getUnit() != null && !product.getUnit().isEmpty()) {
            sb.append(product.getUnitValue()).append(product.getUnit()).append("_").append(product.getPackQty()).append("개");
        } else {
            sb.append(product.getPackQty()).append("개");
        }
        // 추가로 제품명·브랜드명을 조합해 식별력을 보강할 수도 있음
        return sb.toString();
    }

    private List<CoupangProductRequest.Content> buildProductContents(
            ProductRegistrationDto product
    ) {

        // --- 1. HTML 조립용 파라미터 추출
        String korName = safe(product.getBrandName()) + " " + safe(product.getKorName());
        String engName = safe(product.getEngName());
        int packQty = product.getPackQty();
        int unitValue = product.getUnitValue();
        String unit = product.getUnit();
        List<String> imgLinks = product.getUploadedImageLinks();

        String imgTagBlock = imgLinks == null ? "" :
                imgLinks.stream()
                        .filter(url -> url != null && !url.trim().isEmpty())
                        .map(url -> String.format("<img style='width:100%%' src='%s'>", url.replace("\\", "").trim()))
                        .collect(Collectors.joining(""));

        // String ingredients = safe(product.getIngredients()); // 실제 raw 재료 정보는 별도 필드가 있으면 교체
        // String supplementsTable = safe(product.getSupplementFacts()); // 영양성분표 등, 필요하면 product에 확장 필드 추가

        String html = String.format("""
                <div style='margin:20px auto;width:800px;text-align:center'>
                    <img style='width:100%%' src='http://ai.esmplus.com/shouldbe2480/notice/sb_top.png'>
                    <img style='width:100%%' src='http://ai.esmplus.com/shouldbe2480/notice/notice_ihb_01.png'>
                    <img style='width:100%%' src='http://ai.esmplus.com/shouldbe2480/notice/notice_ihb_02.png'>
                    <img style='width:100%%' src='http://ai.esmplus.com/shouldbe2480/notice/notice_ihb_03.png'>
                    <span style='font-size: 16pt; color: rgb(122, 32, 150);'>%s</span><br>
                    <span style='font-size: 12pt; color: rgb(112, 112, 112);'>%s</span><br><br>
                    <span style='font-size: 20pt;color: rgb(202,75,75);'><b>[구성품] 총 %d개 (개당 %d%s)</b></span><br><br>
                    %s
                    <br><br>
                    <img style='width:100%%' src='http://ai.esmplus.com/shouldbe2480/notice/notice_ihb_01.png'>
                    <img style='width:100%%' src='http://ai.esmplus.com/shouldbe2480/notice/notice_ihb_02.png'>
                    <img style='width:100%%' src='http://ai.esmplus.com/shouldbe2480/notice/notice_ihb_03.png'>
                    <img style='width:100%%' src='http://ai.esmplus.com/shouldbe2480/notice/sb_bottom.png'>
                </div>
                """, korName, engName, packQty, unitValue, unit, imgTagBlock)
                .replaceAll("\\s*\\n\\s*", "");;
        System.out.println("●html = " + html);
        product.setDetailsHtml(html); // html은 깔끔하게 잘 생성되었음

        CoupangProductRequest.ContentDetail contentDetail = CoupangProductRequest.ContentDetail.builder()
                .content(html)
                .detailType("TEXT")  // 쿠팡 상세 html(이미지+텍스트+스타일)
                .build();

        CoupangProductRequest.Content content = CoupangProductRequest.Content.builder()
                .contentsType("TEXT")
                .contentDetails(List.of(contentDetail))
                .build();

        return List.of(content);
    }


    // == 안전하게 null, 인덱스 예외 방지
    private String safe(String val) {
        return val == null ? "" : val.trim();
    }


    private List<CoupangProductRequest.Image> buildImageList(List<String> uploadedUrls) {
        List<CoupangProductRequest.Image> result = new ArrayList<>();
        if (uploadedUrls == null) return result;
        for (int i = 0; i < uploadedUrls.size(); i++) {
            result.add(CoupangProductRequest.Image.builder()
                    .imageOrder(i)
                    .imageType(i == 0 ? "REPRESENTATION" : "DETAIL")
                    .vendorPath(uploadedUrls.get(i))
                    .build());
        }
        return result;
    }


    private List<CoupangProductRequest.Attribute> buildRequiredAttributes(
            CoupangCategoryMetaInfoDto metaInfo, ProductRegistrationDto product
    ) {
        List<CoupangProductRequest.Attribute> attributes = new ArrayList<>();
        if (metaInfo == null || metaInfo.getAttributes() == null) return attributes;

        // 1. groupNumber가 NONE인 것은 모두 추가
        for (CoupangCategoryMetaInfoDto.AttributeDto attr : metaInfo.getAttributes()) {
            if("MANDATORY".equals(attr.getRequired())) {
                if ("NONE".equals(attr.getGroupNumber())) {
                    // 그룹 없는 단순 mandatory 속성
                    attributes.add(
                            CoupangProductRequest.Attribute.builder()
                                    .attributeTypeName(attr.getAttributeTypeName())
                                    .attributeValueName(String.valueOf(product.getPackQty()) + "개")
                                    .exposed(attr.getExposed())
                                    .build());
                } else {
                    // attr.getUsableUnits() 리스트 순회
                    List<String> usableUnits = attr.getUsableUnits();
                    String unit = product.getUnit();
                    String unitValue = String.valueOf(product.getUnitValue());
                    if (usableUnits != null && unit != null) {
                        for (String usable : usableUnits) {
                            // 공백/케이스 통일 후 비교(정확히 같을 때)
                            if (unit.trim().equalsIgnoreCase(usable.trim())) {
                                attributes.add(
                                        CoupangProductRequest.Attribute.builder()
                                                .attributeTypeName(attr.getAttributeTypeName())
                                                .attributeValueName(unitValue + unit)  // 혹은 String.valueOf(product.getPackQty()) + usable.trim()
                                                .exposed(attr.getExposed())
                                                .build()
                                );
                                break; // 일치하는 항목을 한 번만 추가(반복 방지)
                            }
                        }
                    }
                    // attr.getUsableUnits 리스트의 항목을 순회하면서, product.getUnit()과 동일한게 있으면 그 부분에서 attributes.add()
                }
            }
        }
        return attributes;
    }

    private List<CoupangProductRequest.Certification> buildCertifications(ProductRegistrationDto product) {
        // 실제 파일명/번호는 DB, 관리자 세팅값, 폼 데이터 등에서 가져올 것
        String certificationCode;
        String vendorPath;

        if ("HEALTH".equalsIgnoreCase(product.getProductType())) {
            // certificationCode = "제2019-0036987호";
            // vendorPath = "https://ai.esmplus.com/shouldbe2480/notice/Health_Functional_Foods_Business_Certificate.jpg";
            return List.of(
                    CoupangProductRequest.Certification.builder()
                            .certificationType("NOT_REQUIRED")
                            .certificationCode("해당없음").build()
            );
        } else if ("FOOD".equalsIgnoreCase(product.getProductType())) {
            // certificationCode = "제20190004785호";
            // vendorPath = "https://ai.esmplus.com/shouldbe2480/notice/Import_Food_Business_License.jpg";
            return List.of(
                    CoupangProductRequest.Certification.builder()
                            .certificationType("NOT_REQUIRED")
                            .certificationCode("해당없음").build()
            );
        }
        // type 미일치 or NOT_REQUIRED면 빈 리스트 반환
        return null;
    }

}
