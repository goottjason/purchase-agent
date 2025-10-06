package com.jason.purchase_agent.util.salechannelapi.smartstore;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreCategoryResult;
import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreProductDto;
import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreProductPageResultDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.poi.ss.formula.functions.T;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;
import static com.jason.purchase_agent.util.http.HttpClientUtil.client;

@Slf4j
public class SmartstoreApiUtil {

    private static final String clientId = "1l5fRuKFzyNJGQF3AP27AE";
    private static final String clientSecret = "$2a$04$24Vxb0j6X3HK.ZVUA43Wk."; // 실제 앱 secret
    private static final String API_URL = "https://api.commerce.naver.com/external";

    // BCrypt로 client_secret_sign 생성
    public static String generateSignature(String clientId, String clientSecret, Long timestamp) {
        String password = clientId + "_" + timestamp;
        String hashedPw = BCrypt.hashpw(password, clientSecret);
        return Base64.getEncoder().encodeToString(hashedPw.getBytes(StandardCharsets.UTF_8));
    }

    public static String getAccessToken() throws Exception {
        Long timestamp = System.currentTimeMillis();
        String clientSecretSign = generateSignature(clientId, clientSecret, timestamp);
        String type = "SELF";

        OkHttpClient client = new OkHttpClient();

        String form = "grant_type=client_credentials"
                + "&client_id=" + clientId
                + "&timestamp=" + timestamp
                + "&client_secret_sign=" + clientSecretSign
                + "&type=" + type;

        RequestBody body = RequestBody.create(
                form,
                MediaType.parse("application/x-www-form-urlencoded")
        );

        Request request = new Request.Builder()
                .url("https://api.commerce.naver.com/external/v1/oauth2/token")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String result = response.body().string();
            JSONObject obj = new JSONObject(result);
            if (obj.has("access_token")) {
                return obj.getString("access_token");
            } else {
                throw new RuntimeException("토큰 발급 실패: " + result);
            }
        }
    }

    // 401+GW.AUTHN시 토큰 재발급, 1회만 자동재시도
    public static Optional<SmartstoreProductPageResultDto> fetchSmartStoreProducts(
            String method, String path, Map<String, Object> bodyParams
    ) throws Exception {
        Optional<SmartstoreProductPageResultDto> smartstoreProductPageResultDto = fetchSmartStoreProductsInternal(method, path, bodyParams, true);
        return smartstoreProductPageResultDto;
    }

    public static Optional<SmartstoreProductPageResultDto> fetchSmartStoreProductsInternal(
            String method, String path, Map<String, Object> bodyParams, boolean allowRetry
    ) throws Exception {

        String accessToken = getAccessToken();

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonBody = new JSONObject(bodyParams);
        String url = API_URL + path;

        RequestBody body = RequestBody.create(mediaType, jsonBody.toString());

        Request request = new Request.Builder()
                .url(url)
                .method(method, body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String resStr = response.body().string();
            if (response.code() == 401 && resStr.contains("\"code\":\"GW.AUTHN\"") && allowRetry) {
                // 토큰 재발급 및 1회 재시도
                System.out.println("[재발급/재시도] 액세스 토큰 만료 또는 권한오류로 재시도...");
                fetchSmartStoreProductsInternal(method, path, bodyParams, false); // 두번째(재발급)에서는 재재시도하지 않음
            } else {
                System.out.println("■resStr = " + resStr);
                // JSON → DTO 매핑
                JSONObject obj = new JSONObject(resStr);
                System.out.println("■obj = " + obj);
                SmartstoreProductPageResultDto result = new SmartstoreProductPageResultDto();
                result.setPage(obj.optInt("page", 1));
                result.setSize(obj.optInt("size", 50));
                result.setTotalPages(obj.optInt("totalPages"));
                result.setTotalElements(obj.optInt("totalElements"));
                result.setFirst(obj.optBoolean("first"));
                result.setLast(obj.optBoolean("last"));

                System.out.println("■result = " + result);

                // 상품 리스트 파싱
                List<SmartstoreProductDto> prodList = new ArrayList<>();
                JSONArray contentsArr = obj.optJSONArray("contents");

                System.out.println("■contentsArr = " + contentsArr);
                if (contentsArr != null) {
                    // 한 페이지에 반환된 상품의 개수만큼 순회
                    for (int i = 0; i < contentsArr.length(); i++) {

                        JSONObject prodObj = contentsArr.getJSONObject(i);
                        SmartstoreProductDto dto = new SmartstoreProductDto();
                        dto.setOriginProductNo(prodObj.optLong("originProductNo"));

                        // channelProducts(리스트)
                        List<SmartstoreProductDto.ChannelProduct> cprodList = new ArrayList<>();
                        JSONArray chanArr = prodObj.optJSONArray("channelProducts");

                        if (chanArr != null) {
                            for (int j = 0; j < chanArr.length(); j++) {
                                JSONObject chObj = chanArr.getJSONObject(j);
                                System.out.println("■chObj = " + chObj);
                                SmartstoreProductDto.ChannelProduct cp = new SmartstoreProductDto.ChannelProduct();

                                cp.setStatusType(chObj.optString("statusType", ""));
                                cp.setManufacturerName(chObj.optString("manufacturerName", ""));
                                cp.setExchangeFee(chObj.optInt("exchangeFee", 0));
                                cp.setManagerPurchasePoint(chObj.optInt("managerPurchasePoint", 0));
                                cp.setSaleEndDate(chObj.optString("saleEndDate", ""));
                                cp.setStockQuantity(chObj.optInt("stockQuantity", 0));
                                cp.setSaleStartDate(chObj.optString("saleStartDate", ""));
                                cp.setRegDate(chObj.optString("regDate", ""));
                                cp.setWholeCategoryName(chObj.optString("wholeCategoryName", ""));
                                cp.setDeliveryAttributeType(chObj.optString("deliveryAttributeType", ""));
                                cp.setSellerManagementCode(chObj.optString("sellerManagementCode", ""));
                                cp.setChannelProductNo(chObj.optLong("channelProductNo", 0L));
                                cp.setBrandName(chObj.optString("brandName", ""));
                                cp.setKnowledgeShoppingProductRegistration(chObj.optBoolean("knowledgeShoppingProductRegistration", false));
                                cp.setSalePrice(chObj.optInt("salePrice", 0));
                                cp.setMobileDiscountedPrice(chObj.optInt("mobileDiscountedPrice", 0));
                                cp.setChannelProductDisplayStatusType(chObj.optString("channelProductDisplayStatusType", ""));
                                cp.setChannelServiceType(chObj.optString("channelServiceType", ""));
                                cp.setOriginProductNo(chObj.optLong("originProductNo", 0L));
                                cp.setDeliveryFee(chObj.optInt("deliveryFee", 0));
                                cp.setDiscountedPrice(chObj.optInt("discountedPrice", 0));
                                cp.setReturnFee(chObj.optInt("returnFee", 0));
                                cp.setName(chObj.optString("name", ""));
                                cp.setModifiedDate(chObj.optString("modifiedDate", ""));
                                cp.setWholeCategoryId(chObj.optString("wholeCategoryId", ""));
                                cp.setCategoryId(chObj.optString("categoryId", ""));

                                // representativeImage 파싱 (동일 계층)
                                SmartstoreProductDto.RepresentativeImage rim = null;
                                if (chObj.has("representativeImage") && !chObj.isNull("representativeImage")) {
                                    JSONObject imgObj = chObj.getJSONObject("representativeImage");
                                    rim = new SmartstoreProductDto.RepresentativeImage();
                                    rim.setUrl(imgObj.optString("url", ""));
                                }
                                cp.setRepresentativeImage(rim);
                                System.out.println("■cp = " + cp);
                                cprodList.add(cp);
                            }
                        }

                        dto.setChannelProducts(cprodList);
                        System.out.println("■dto = " + dto);
                        prodList.add(dto);
                    }
                }
                result.setContents(prodList);
                System.out.println("■■■result = " + result);
                return Optional.of(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("API 호출 실패: " + e.getMessage());
        }
        return Optional.empty();
    }

    public static <T> Optional<List<T>> executeRequest(
            String method,
            String path,
            Map<String, Object> bodyParams,
            Class<T> responseClass,
            boolean allowRetry
    ) throws Exception {

        // 토큰 발행
        String accessToken = getAccessToken();

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        String url = API_URL + path;

        // HTTP 메서드별 Request 생성
        Request request;
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken);

        // GET, DELETE는 body 없음, POST, PUT, PATCH는 body 포함
        if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            // GET/DELETE: body 없이 요청
            request = requestBuilder.method(method, null).build();
        } else {
            // POST/PUT/PATCH: body 포함
            JSONObject jsonBody = new JSONObject(bodyParams != null ? bodyParams : new HashMap<>());
            RequestBody body = RequestBody.create(mediaType, jsonBody.toString());

            request = requestBuilder
                    .method(method, body)
                    .addHeader("Content-Type", "application/json")
                    .build();
        }

        try (Response response = client.newCall(request).execute()) {
            String resStr = response.body().string();

            if (response.code() == 401 && resStr.contains("\"code\":\"GW.AUTHN\"") && allowRetry) {
                // 토큰 재발급 및 1회 재시도
                System.out.println("[재발급/재시도] 액세스 토큰 만료 또는 권한오류로 재시도...");
                return executeRequest(
                        method, path, bodyParams,responseClass, false);
            }
            else if (response.isSuccessful()) {
                // 성공 응답 처리
                System.out.println("■ Response Code: " + response.code());
                System.out.println("■ Response Body: " + resStr);

                return parseJsonArrayResponse(resStr, responseClass);
            }
            else {
                // HTTP 오류 응답 처리
                System.out.println("■ API 호출 실패 - Code: " + response.code());
                System.out.println("■ Error Response: " + resStr);
                return Optional.empty();
            }
        } catch (Exception e) {
            System.out.println("■ API 호출 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * JSON 응답 파싱 (배열/객체 형태 모두 처리)
     */
    private static Optional<JSONObject> parseJsonResponse(String responseStr) {
        if (responseStr == null || responseStr.trim().isEmpty()) {
            System.out.println("■ 응답이 비어있습니다.");
            return Optional.empty();
        }

        String trimmed = responseStr.trim();

        try {
            // 배열 형태 JSON 처리 (스마트스토어 카테고리 API)
            if (trimmed.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(trimmed);

                // 배열을 표준 API 응답 형태로 래핑
                JSONObject wrapper = new JSONObject();
                wrapper.put("success", true);
                wrapper.put("data", jsonArray);
                wrapper.put("totalCount", jsonArray.length());
                wrapper.put("message", "조회 성공");

                System.out.println("■ 배열 응답을 객체로 래핑 완료");
                return Optional.of(wrapper);
            }
            // 객체 형태 JSON 처리
            else if (trimmed.startsWith("{")) {
                JSONObject obj = new JSONObject(trimmed);
                System.out.println("■ JSON 객체 파싱 완료");
                return Optional.of(obj);
            }
            else {
                System.out.println("■ 예상하지 못한 응답 형식:");
                System.out.println(trimmed.substring(0, Math.min(200, trimmed.length())));
                return Optional.empty();
            }
        } catch (JSONException e) {
            System.out.println("■ JSON 파싱 실패: " + e.getMessage());
            System.out.println("■ 응답 내용 일부: " + trimmed.substring(0, Math.min(100, trimmed.length())));
            return Optional.empty();
        }
    }


    /**
     * JSON 배열 응답을 바로 리스트로 반환 (래퍼 없음)
     */
    private static <T> Optional<List<T>> parseJsonArrayResponse(
            String responseStr, Class<T> clazz
    ) {
        if (responseStr == null || responseStr.trim().isEmpty()) {
            return Optional.empty();
        }

        String trimmed = responseStr.trim();

        try {
            if (trimmed.startsWith("[")) {
                // 배열을 바로 List<SmartstoreCategoryResult>로 변환
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                List<T> list = objectMapper.readValue(
                        trimmed,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
                );

                return Optional.of(list);
            } else {
                System.out.println("■ 예상과 다른 응답 형식: " + trimmed.substring(0, Math.min(100, trimmed.length())));
                return Optional.empty();
            }
        } catch (Exception e) {
            System.out.println("■ JSON 파싱 실패: " + e.getMessage());
            return Optional.empty();
        }
    }



    public static String simpleHttpExecute(String method, String path, Map<String, Object> bodyParams) {
        String accessToken;
        try {
            accessToken = getAccessToken();
        } catch (Exception e) {
            log.error("API 토큰 발행 실패: {}", e.getMessage(), e);
            // 네트워크 등 치명적 장애일 경우 null 반환 또는 특별 코드 반환
            return e.getMessage();
            // throw new RuntimeException("Smartstore API 토큰 반환 불가", e);
        }

        MediaType mediaType = MediaType.parse("application/json");
        String url = API_URL + path;

        try {
            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer " + accessToken);

            if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
                url += buildQueryString(bodyParams);
                builder.url(url).method(method, null);
            } else {
                String jsonBody = objectMapper.writeValueAsString(bodyParams);
                RequestBody body = RequestBody.create(mediaType, jsonBody);
                builder.method(method, body)
                        .addHeader("Content-Type", "application/json");
            }

            try (Response response = client.newCall(builder.build()).execute()) {
                ResponseBody respBody = response.body();
                if (respBody == null) {
                    throw new IOException("API 응답이 비어있음 (body == null)");
                }
                return respBody.string();
            }

        } catch (Exception e) {
            log.error("API 호출 예외 ({} {}): {}", method, url, e.getMessage(), e);
            return e.getMessage();
            // throw new RuntimeException("Smartstore 서버 API 호출 실패", e);
        }
    }



    /* 좀 저 심플하게 만들어줬어 위에

    public static String simpleHttpExecute(
            String method, String path, Map<String, Object> bodyParams
    ) {
        // 토큰 발행
        String accessToken = null;
        try {
            accessToken = getAccessToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        String url = API_URL + path;

        Request request;
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken);

        if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            url += buildQueryString(bodyParams);
            // System.out.println("■■■url = " + url);
            request = builder.url(url).method(method, null).build();

        } else {
            // bodyParams는 DTO 전체를 맵으로 받은 것!
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(bodyParams); // Map을 다시 전체 JSON으로 변환
            // System.out.println("■■■jsonBody = " + jsonBody);
            RequestBody body = RequestBody.create(mediaType, jsonBody);
            request = builder
                    .method(method, body)
                    .addHeader("Content-Type", "application/json")
                    .build();
        }

        try (Response response = client.newCall(request).execute()) {
            String resStr = response.body().string();
            // 그냥 내용만 콘솔에 뿌리고, String 반환
            // System.out.println(resStr);
            return resStr;
        } catch (Exception e) {
            System.out.println("■ API 호출 예외: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }*/

    public static String buildQueryString(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return "";
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(String.valueOf(e.getValue()), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&", "?", ""));
    }

    public static String multipartHttpExecute(
            String path,
            List<File> imageFiles       // 업로드할 파일 목록
    ) throws Exception {
        // 토큰 발행
        String accessToken = getAccessToken();
        OkHttpClient client = new OkHttpClient();
        String url = API_URL + path;

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        // 필수: imageFiles[] (input name)
        for (File img : imageFiles) {
            bodyBuilder.addFormDataPart(
                    "imageFiles",
                    img.getName(),
                    RequestBody.create(MediaType.parse("image/jpeg"), img)
            );
        }


        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json;charset=UTF-8")
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(bodyBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            String resStr = response.body().string();
            System.out.println(resStr);
            return resStr;
        } catch (Exception e) {
            System.out.println("■ multipart API 호출 예외: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
