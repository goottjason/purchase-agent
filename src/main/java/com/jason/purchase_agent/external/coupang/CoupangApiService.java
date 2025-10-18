package com.jason.purchase_agent.external.coupang;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.channel.coupang.CoupangCategoryMetaInfoDto;
import com.jason.purchase_agent.dto.channel.coupang.CoupangProductRequest;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import com.jason.purchase_agent.dto.products.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jason.purchase_agent.external.coupang.CoupangApiUtil.executeRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoupangApiService {

    private final ObjectMapper objectMapper;

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
        try {
            String path = String.format(
                    "/v2/providers/seller_api/apis/api/v1/marketplace/vendor-items/%s/prices/%d",
                    vendorItemId, salePrice);
            Map<String, String> params = new HashMap<>();
            params.put("forceSalePriceUpdate", "true");

            String response = executeRequest("PUT", path, params, null);
            return response;
        } catch (Exception e) {
            log.error("[CoupangUpdatePrice] 요청 에러 (vendorItemId={}, salePrice={}, e.getMessage()={})",
                    vendorItemId, salePrice, e.getMessage());
            return "{}";
        }
    }
    public String updateStock(String vendorItemId, Integer stock) {
        try {
            String path = String.format(
                    "/v2/providers/seller_api/apis/api/v1/marketplace/vendor-items/%s/quantities/%d",
                    vendorItemId, stock);

            String response = executeRequest("PUT", path, null, null);
            return response;
        } catch (Exception e) {
            log.error("[CoupangUpdateStock] 요청 에러 (vendorItemId={}, stock={}, e.getMessage()={})",
                    vendorItemId, stock, e.getMessage());
            return "{}";
        }
    }

    public String registerProduct(
            CoupangProductRequest request
    ) {
        try {
            String path = "/v2/providers/seller_api/apis/api/v1/marketplace/seller-products";
            Map<String, Object> body = objectMapper.convertValue(request, Map.class);
            String response = executeRequest("POST", path, null, body);
            return response;
        } catch (Exception e) {
            log.error("[CoupangRegister] 요청 에러 (request={}, e.getMessage()={})",
                    request, e.getMessage());
            return "{}";
        }
    }

    public String recommendDisplayCategory(ProductDto product) throws JsonProcessingException {
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


    public String buildItemName(ProductDto product) {
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

    public List<CoupangProductRequest.Content> buildProductContents(
            ProductRegistrationRequest request
    ) {
        ProductDto product = request.getProductDto();
        // --- 1. HTML 조립용 파라미터 추출
        String korName = safe(product.getBrandName()) + " " + safe(product.getKorName());
        String engName = safe(product.getEngName());
        int packQty = product.getPackQty();
        int unitValue = product.getUnitValue();
        String unit = product.getUnit();
        List<String> imgLinks = request.getUploadedImageLinks();

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


    public List<CoupangProductRequest.Image> buildImageList(List<String> uploadedUrls) {
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


    public List<CoupangProductRequest.Attribute> buildRequiredAttributes(
            CoupangCategoryMetaInfoDto metaInfo, ProductDto product
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

    private List<CoupangProductRequest.Certification> buildCertifications(ProductDto product) {
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

    public List<CoupangProductRequest.Notice> buildNoticeByProductType(
            ProductDto productDto) {
        List<CoupangProductRequest.Notice> notices;
        if ("HEALTH".equalsIgnoreCase(productDto.getProductType())) {
            notices = CoupangProductRequest.HEALTH_FUNCTIONAL_NOTICES;
        } else {
            notices = CoupangProductRequest.PROCESSED_FOOD_NOTICES;
        }
        return notices;
    }
}
