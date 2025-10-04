package com.jason.purchase_agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.channel.coupang.CoupangApiResponse;
import com.jason.purchase_agent.dto.channel.coupang.CoupangCategoryMetaInfoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jason.purchase_agent.util.salechannelapi.coupang.CoupangApiUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class CoupangApiTests {

    @Test
    void 카테고리_추천() throws JsonProcessingException {
        // 영양제 -> 기타영양제 (73137)
        // 식료품 -> 사과식초 (59861)
        String path = "/v2/providers/openapi/apis/api/v1/categorization/predict";
        Map<String, Object> body = new HashMap<>();
        body.put("productName", "Bragg, 유기농 무가공 애플 사이다 식초, 초모 함유, 473ml(16fl oz)");
        String response = CoupangApiUtil.executeRequest("POST", path, null, body);
        System.out.println("■ response = " + response);

        // {"code":200,"message":"OK","data":{"autoCategorizationPredictionResultType":"SUCCESS","predictedCategoryId":"59861","predictedCategoryName":"사과식초","comment":null}}

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode dataNode = rootNode.path("data");
        String predictedCategoryId = dataNode.path("predictedCategoryId").asText();
        String predictedCategoryName = dataNode.path("predictedCategoryName").asText();
        System.out.println("□ predictedCategoryId = " + predictedCategoryId);
        System.out.println("□ predictedCategoryName = " + predictedCategoryName);
    }

    @Test
    void 카테고리_메타정보_조회() throws JsonProcessingException {
        long categoryId = 59861;
        String path = "/v2/providers/seller_api/apis/api/v1/marketplace/meta/category-related-metas/display-category-codes/" + categoryId;
        String response = CoupangApiUtil.executeRequest("GET", path, null, null);
        System.out.println("■ response = " + response);

        // {"code":"SUCCESS","message":"","data":{"isAllowSingleItem":false,"attributes": ... }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode dataNode = rootNode.path("data");
        try {
            CoupangCategoryMetaInfoDto coupangCategoryMetaInfoDto =
                    dataNode.traverse(objectMapper).readValueAs(CoupangCategoryMetaInfoDto.class);
            System.out.println("□ coupangCategoryMetaInfoDto = " + coupangCategoryMetaInfoDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
