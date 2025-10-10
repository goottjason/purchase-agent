package com.jason.purchase_agent.external.smartstore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreEnrollRequest;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import com.jason.purchase_agent.util.salechannelapi.smartstore.SmartstoreApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;
import static com.jason.purchase_agent.util.downloader.ImageDownloader.findProductImageFiles;
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartstoreApiService {

    public String updatePrice(
            String originProductNo, Integer salePrice
    ) {

        try {
            String getPath = String.format("/v2/products/origin-products/%s", originProductNo);
            String getResponseJson = SmartstoreApiUtil.simpleHttpExecute("GET", getPath, null);

            Map<String, Object> getResponseMap = objectMapper.readValue(
                    getResponseJson, new TypeReference<Map<String,Object>>() {});

            Map<String, Object> originProduct = (Map<String, Object>) getResponseMap.get("originProduct");
            originProduct.put("salePrice", salePrice);
            if ("OUTOFSTOCK".equals(originProduct.get("statusType"))) {
                originProduct.put("statusType", "SALE");
                originProduct.put("stockQuantity", 0); // 0이면 statusType 무시됨
            }

            Map<String, Object> body = new HashMap<>();
            body.put("originProduct", originProduct);

            String putPath = String.format("/v2/products/origin-products/%s", originProductNo);
            String putResponseJson = SmartstoreApiUtil.simpleHttpExecute("PUT", putPath, body);

            return putResponseJson;
        } catch (Exception e) {
            log.error("[SmartstoreUpdatePrice] 요청 에러 (originProductNo={}, salePrice={}, e.getMessage()={})",
                    originProductNo, salePrice, e.getMessage());
            return "{}";
        }
    }

    public String updateStock(String originProductNo, Integer stock) {

        try {
            // 1. 상품 조회(기존 정보 가져오기)
            String getPath = String.format("/v2/products/origin-products/%s", originProductNo);
            String getResponseJson = SmartstoreApiUtil.simpleHttpExecute("GET", getPath, null);

            Map<String, Object> getResponseMap = objectMapper.readValue(
                    getResponseJson, new TypeReference<Map<String,Object>>() {});

            Map<String, Object> originProduct = (Map<String, Object>) getResponseMap.get("originProduct");
            if ("OUTOFSTOCK".equals(originProduct.get("statusType"))) {
                originProduct.put("statusType", "SALE");
            }
            originProduct.put("stockQuantity", stock);

            Map<String, Object> body = new HashMap<>();
            body.put("originProduct", originProduct);

            // 2. 수정(재고 변경) API 호출
            String putPath = String.format("/v2/products/origin-products/%s", originProductNo);
            String putResponseJson = SmartstoreApiUtil.simpleHttpExecute("PUT", putPath, body);

            // 3. 응답 그대로 반환
            return putResponseJson;

        } catch (Exception e) {
            log.error("[SmartstoreUpdateStock] 요청 에러 (originProductNo={}, stock={}, e.getMessage()={})",
                    originProductNo, stock, e.getMessage());
            return "{}";
        }
    }



    public void updatePriceStock(String originProductNo, Integer salePrice, Integer stock) {
        try {
            System.out.println("originProductNo = " + originProductNo);

            // 1. 기존 상품 정보 조회 (필수!)
            String getPath = String.format("/v2/products/origin-products/%s", originProductNo);
            String getResult = SmartstoreApiUtil.simpleHttpExecute("GET", getPath, null);
            // getResult를 JSON으로 파싱해서 전체 상품 정보 맵 추출 필요 (여기선 org.json 사용 예시)
            JSONObject json = new JSONObject(getResult);
            JSONObject originProduct = json.getJSONObject("originProduct");

            // 2. 수정할 필드 업데이트 (가격/재고만 변경)
            originProduct.put("salePrice", salePrice);
            originProduct.put("stockQuantity", stock);
            // 필요시 판매상태, 이미지, detailAttribute 등도 그대로 유지

            // 3. body 구성 및 PUT 요청
            Map<String, Object> body = new HashMap<>();
            // JSONObject → Map 변환 (예시, 라이브러리에 따라 다름)
            body.put("originProduct", originProduct.toMap());

            String putPath = String.format("/v2/products/origin-products/%s", originProductNo);
            String putResult = SmartstoreApiUtil.simpleHttpExecute("PUT", putPath, body);

            // 성공 시 로깅 (필요시 result 처리)
            System.out.println("상품 가격/재고 업데이트 성공: " + putResult);

        } catch (Exception e) {
            // 에러 발생 시 로그만 출력, 예외는 throw 하지 않음
            System.err.println("상품 가격/재고 업데이트 실패: " + originProductNo + ", salePrice=" + salePrice + ", stock=" + stock);
            e.printStackTrace();
            // 필요시: 관리자 알림/에러 카운트 처리 등
        }
    }


    public String enrollProducts(ProductRegistrationRequest product) throws Exception {

        // 1. 파일 경로 기준 이미지 검색 (예: product.code 포함된 파일 1~4장)
        List<File> imageFiles = findProductImageFiles(product.getCode()); // 직접 로직 작성 필요
        System.out.println("imageFiles = " + imageFiles);
        
        // 2. 이미지 업로드 API 호출
        String resStr = SmartstoreApiUtil.multipartHttpExecute( "/v1/product-images/upload", imageFiles);
        System.out.println("resStr = " + resStr);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resStr); // JSON 파싱
        List<String> smartstoreImageLinks = new ArrayList<>();
        if (root.has("images")) {
            for (JsonNode imageNode : root.get("images")) {
                String url = imageNode.get("url").asText();
                smartstoreImageLinks.add(url);
            }
        }
        product.setSmartstoreImageLinks(smartstoreImageLinks);

        // 2. 실제 DTO 객체 생성
        SmartstoreEnrollRequest req = SmartstoreEnrollRequest.from(product);

        // 3. ObjectMapper 생성
        Map<String, Object> params = mapper.convertValue(req, Map.class);

        // 4. path 설정
        try {
            String response = SmartstoreApiUtil.simpleHttpExecute("POST", "/v2/products", params);
            System.out.println("response = " + response);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
