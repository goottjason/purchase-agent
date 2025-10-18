package com.jason.purchase_agent.external.smartstore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreProductRequest;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;
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

    public String registerProduct(ProductRegistrationRequest request) {
        try {
            // 1. 파일 경로 기준 이미지 파일 리스트 생성
            List<String> imagePathList = request.getImageFiles();
            List<File> imageFiles = imagePathList.stream()
                    .map(File::new)
                    .collect(Collectors.toList());

            // 2. 이미지 업로드 API 호출
            String uploadResponseJson = SmartstoreApiUtil.multipartHttpExecute("/v1/product-images/upload", imageFiles);
            JsonNode root = objectMapper.readTree(uploadResponseJson);

            List<String> smartstoreImageLinks = new ArrayList<>();
            if (root.has("images")) {
                for (JsonNode imageNode : root.get("images")) {
                    String url = imageNode.get("url").asText();
                    smartstoreImageLinks.add(url);
                }
            }
            request.setSmartstoreImageLinks(smartstoreImageLinks);

            // 2. 실제 DTO 객체 생성
            SmartstoreProductRequest smartstoreProductRequest = SmartstoreProductRequest.from(request);

            // 3. ObjectMapper 생성
            Map<String, Object> params = objectMapper.convertValue(smartstoreProductRequest, Map.class);
            // 4. API 호출
            String responseJson = SmartstoreApiUtil.simpleHttpExecute("POST", "/v2/products", params);
            return responseJson;
        } catch (Exception e) {
            log.error("[SmartstoreRegisterProduct] 요청 에러 (request={}, e.getMessage()={})",
                    request, e.getMessage());
            return "{}";
        }
    }



    public String enrollProducts(ProductRegistrationRequest request) throws Exception {

        // 1. 파일 경로 기준 이미지 파일 리스트 생성
        List<String> imagePathList = request.getImageFiles();
        List<File> imageFiles = imagePathList.stream()
                .map(File::new)
                .collect(Collectors.toList());

        // 2. 이미지 업로드 API 호출
        String responseJson = SmartstoreApiUtil.multipartHttpExecute( "/v1/product-images/upload", imageFiles);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseJson); // JSON 파싱
        List<String> smartstoreImageLinks = new ArrayList<>();
        if (root.has("images")) {
            for (JsonNode imageNode : root.get("images")) {
                String url = imageNode.get("url").asText();
                smartstoreImageLinks.add(url);
            }
        }
        request.setSmartstoreImageLinks(smartstoreImageLinks);

        // 2. 실제 DTO 객체 생성
        SmartstoreProductRequest req = SmartstoreProductRequest.from(request);

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
