package com.jason.purchase_agent;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreCategoryResult;
import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreProductPageResultDto;
import com.jason.purchase_agent.util.salechannelapi.smartstore.SmartstoreApiUtil;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
public class SmartstoreApiTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 카테고리_조회() throws Exception {
        String BASE_PATH = "/v1/categories";


        Map<String, Object> params = new HashMap();
        params.put("last", true);

        Optional<List<SmartstoreCategoryResult>> response =
                SmartstoreApiUtil.executeRequest(
                        "GET", BASE_PATH, params, SmartstoreCategoryResult.class,true);

        if (response.isPresent()) {
            List<SmartstoreCategoryResult> categories = response.get();
            System.out.println("■ 총 카테고리: " + categories.size());

            // 리프 카테고리 필터링
            List<SmartstoreCategoryResult> leafCategories = categories.stream()
                    .filter(SmartstoreCategoryResult::isLeafCategory)
                    .collect(Collectors.toList());

            System.out.println("■ 리프 카테고리: " + leafCategories.size());
            assertThat(categories).isNotEmpty();
        }
    }

    @Test
    void 주소록_목록_조회() throws Exception {
        String BASE_PATH = "/v1/seller/addressbooks-for-page";
        Map<String, Object> params = new HashMap();
        params.put("page", 1);
        String response = SmartstoreApiUtil.simpleHttpExecute("GET", BASE_PATH, params);
    }

    @Test
    void 카테고리별_속성_조회() throws Exception {
        String BASE_PATH = "/v1/product-attributes/attributes";
        Map<String, Object> params = new HashMap();
        params.put("categoryId", "50002266");
        String response = SmartstoreApiUtil.simpleHttpExecute("GET", BASE_PATH, params);
    }
    @Test
    void 카테고리별_속성값_조회() throws Exception {
        String BASE_PATH = "/v1/product-attributes/attribute-values";
        Map<String, Object> params = new HashMap();
        params.put("categoryId", "50002266");
        String response = SmartstoreApiUtil.simpleHttpExecute("GET", BASE_PATH, params);
    }

    @Test
    void 상품_목록_원상품코드_매핑_조회() throws Exception {
        String BASE_PATH = "/v1/products/search";
        int page = 1;
        int pageSize = 500;
        boolean hasMore = true;

        List<Map<String, Object>> results = new ArrayList<>();

        while (hasMore) {
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("size", pageSize); // size 최대 500

            String response = SmartstoreApiUtil.simpleHttpExecute("POST", BASE_PATH, params);
            org.json.JSONObject json = new org.json.JSONObject(response);

            org.json.JSONArray contents = json.getJSONArray("contents"); // ← 응답에 맞춰 "contents" 배열 사용
            if (contents.length() == 0) break;

            for (int i = 0; i < contents.length(); i++) {
                org.json.JSONObject contentItem = contents.getJSONObject(i);
                long originProductNo = contentItem.optLong("originProductNo");

                org.json.JSONArray channelProducts = contentItem.getJSONArray("channelProducts");
                for (int j = 0; j < channelProducts.length(); j++) {
                    org.json.JSONObject channelProduct = channelProducts.getJSONObject(j);
                    String sellerManagementCode = channelProduct.optString("sellerManagementCode");

                    Map<String, Object> record = new HashMap<>();
                    record.put("originProductNo", originProductNo);
                    record.put("sellerManagementCode", sellerManagementCode);
                    results.add(record);
                }
            }

            // 페이지 증가, 더 없는지 체크
            hasMore = (contents.length() == pageSize);
            page++;
        }

        // 결과 출력 or 활용
        System.out.println("상품 매핑 결과: " + results);
    }
}
