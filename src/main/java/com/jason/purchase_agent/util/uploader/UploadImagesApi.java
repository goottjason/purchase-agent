package com.jason.purchase_agent.util.uploader;

import com.jason.purchase_agent.dto.product_registration.ProductImageUploadResult;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class UploadImagesApi {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String uploadApiUrl = "http://localhost:5000/api/upload-img";

    /**
     * @param products 각 상품: {"code":..., "imageFiles":[이미지 로컬 경로...]}
     * @return code, uploadedImageLinks 매핑이 담긴 리스트
     */
    public List<ProductImageUploadResult> batchUploadImages(List<Map<String, Object>> products) {
        // 1. 요청 바디 구성
        Map<String, Object> body = new HashMap<>();
        body.put("products", products);

        // 2. 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. HttpEntity로 감싸기
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 4. POST 호출 (Flask API)
        ResponseEntity<ProductImageUploadResult[]> res = restTemplate.postForEntity(
                uploadApiUrl, entity, ProductImageUploadResult[].class);

        // 5. 결과 반환
        ProductImageUploadResult[] arr = res.getBody();
        return arr != null ? Arrays.asList(arr) : Collections.emptyList();
    }


}