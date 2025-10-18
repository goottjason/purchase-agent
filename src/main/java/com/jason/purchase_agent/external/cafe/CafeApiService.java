package com.jason.purchase_agent.external.cafe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jason.purchase_agent.dto.channel.cafe.CafeProductRequest;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class CafeApiService {

    @Value("${cafe24.token.path:refresh_token.txt}")
    private String tokenPath;

    public static final String MALL_ID = "younzara";
    public static final String CLIENT_ID = "r0Z9nXoDDNfOrf5F6wYzTA";
    public static final String CLIENT_SECRET = "cr0USvbIP9uiTJ9uh8w7ZB";
    public static final String REDIRECT_URI = "https://younzara.cafe24.com/";
    public static final String SCOPE = "mall.read_product,mall.write_product"; // 필요한 권한
    public static final String API_ENDPOINT = "https://younzara.cafe24api.com";
    public static final String STATE = "shouldbeshopping";

    public String accessToken;
    public String refreshToken;
    public Instant tokenExpiresAt;

    @PostConstruct
    public void init() {
        try {
            File tokenFile = new File(tokenPath);
            if (!tokenFile.exists()) {
                String url = generateAuthorizationUrl();
                log.info("아래 URL을 브라우저(로컬 PC 등)에서 직접 열어 인증코드를 복사해주세요:\n" + url);
            } else {
                loadRefreshToken();
                if (!isTokenValid()) {
                    refreshTokens();
                }
            }
        } catch (Exception e) {
            log.error("CafeApiService 초기화 실패!", e);
        }
    }

    private String generateAuthorizationUrl() {
        return String.format(
                "https://%s.cafe24api.com/api/v2/oauth/authorize?response_type=code&client_id=%s&state=%s&redirect_uri=%s&scope=%s",
                MALL_ID, CLIENT_ID, STATE,
                URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8),
                URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
        );
    }

    // 2. 인증 코드로 토큰 발급
    public void requestTokens(String code) throws Exception {
        String payload = String.format(
                "grant_type=authorization_code&code=%s&redirect_uri=%s",
                URLEncoder.encode(code, StandardCharsets.UTF_8),
                URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
        );

        processTokenRequest(payload);
        saveRefreshToken(); // 새로 발급된 리프레시 토큰 저장
    }

    // 3. 리프레시 토큰으로 토큰 갱신
    public void refreshTokens() throws Exception {
        String currentRefreshToken = loadRefreshToken();
        if(currentRefreshToken == null || currentRefreshToken.isEmpty()) {
            throw new IllegalStateException("No refresh token available");
        }

        String payload = String.format(
                "grant_type=refresh_token&refresh_token=%s",
                URLEncoder.encode(currentRefreshToken, StandardCharsets.UTF_8)
        );

        try {
            processTokenRequest(payload);
            saveRefreshToken(); // 새로 발급된 리프레시 토큰 갱신
        } catch (RuntimeException e) {
            if (e.getMessage().contains("invalid_grant") || e.getMessage().contains("Invalid refresh_token")) {
                // 잘못된 refresh_token 에러 발생 시 파일 삭제
                File tokenFile = new File(tokenPath);
                if (tokenFile.exists()) {
                    tokenFile.delete();
                }
                // 재시작 또는 재인증 유도
                throw new IllegalStateException("Invalid refresh_token detected. " +
                        "Refresh token file deleted. " +
                        "Please restart and re-authenticate.");
            } else {
                throw e;
            }
        }
    }


    // 공통 토큰 처리 로직
    private void processTokenRequest(String payload) throws Exception {
        String auth = Base64.getEncoder().encodeToString(
                (CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));

        HttpURLConnection conn = (HttpURLConnection) new URL(
                "https://" + MALL_ID + ".cafe24api.com/api/v2/oauth/token"
        ).openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Basic " + auth);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                JSONObject json = new JSONObject(br.readLine());
                updateTokenInfo(json);
            }
        } else {
            handleErrorResponse(conn);
        }
    }

    // 4. 토큰 정보 업데이트
    private void updateTokenInfo(JSONObject json) {
        this.accessToken = json.getString("access_token");
        this.refreshToken = json.getString("refresh_token");
        this.tokenExpiresAt = Instant.parse(json.getString("expires_at") + "Z");
    }

    // 5. 리프레시 토큰 저장/로드
    private void saveRefreshToken() throws IOException {
        try (FileWriter writer = new FileWriter(tokenPath)) { // 명시적 파일 경로 사용
            writer.write(refreshToken);
        }
    }

    private String loadRefreshToken() throws IOException {
        File file = new File(tokenPath); // C:/projects/refresh_token.txt
        if (!file.exists()) return null;  // 파일이 없으면 null 반환

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return br.readLine();
        }
    }

    // 6. 토큰 유효성 검사
    public boolean isTokenValid() {
        return tokenExpiresAt != null && Instant.now().isBefore(tokenExpiresAt);
    }

    // 7. 에러 처리
    private void handleErrorResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getErrorStream()))) {
            JSONObject error = new JSONObject(br.readLine());
            throw new RuntimeException("API Error: " + error.toString());
        }
    }

    // 공통 PUT 요청 처리 메서드
    @Synchronized
    private String executePutRequest(String method, String path, String requestBody) {
        try {
            // 전체 API URL 생성
            URL url = new URL(API_ENDPOINT + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Authorization", "Bearer " + this.accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "YourApp/1.0");
            // POST, PUT 등에서는 body 필요, DELETE/GET은 보통 body 없음
            boolean hasBody = requestBody != null && !requestBody.isEmpty()
                    && (method.equals("POST") || method.equals("PUT") || method.equals("PATCH"));
            conn.setDoOutput(hasBody);

            if (hasBody) {
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }

            // 응답 코드 확인 (200=성공, 그 외=실패)
            int responseCode = conn.getResponseCode();
            // 성공이면 getInputStream, 실패면 getErrorStream 사용
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream()));

            // 응답 데이터 읽기
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            try { Thread.sleep(350); } catch (InterruptedException ignore) {}

            // 결과 출력 (디버깅용)
            /*System.out.println("●Cafe24 : path: " + path);
            System.out.println("●Cafe24 : Response Code: " + responseCode);
            System.out.println("●Cafe24 : Response Body: " + response.toString());*/

            return response.toString();

        } catch (Exception e) {
            log.error("[CafeExecuteRequest] 요청 에러 (e.getMessage()={})", e.getMessage());
            return e.getMessage();
        }
    }

    public String updatePrice(
        String cafeNo, String productCode, Integer salePrice
    ) {
        try {
            String putPath = String.format("/api/v2/admin/products/%s", cafeNo);

            // JSON 본문 생성 (price 필드만 포함)
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("product_no", Integer.valueOf(cafeNo)); // 바깥쪽 필드
            JSONObject requestObj = new JSONObject();
            requestObj.put("display", "T");
            requestObj.put("selling", "T");
            requestObj.put("price", salePrice);
            requestObj.put("origin_classification", "E");
            requestObj.put("origin_place_no", 1800);
            requestObj.put("origin_place_value", "기타");
            requestObj.put("internal_product_name", productCode);
            jsonBody.put("request", requestObj); // 바깥쪽에 request 키로 추가
            // 실제 PUT 요청 실행
            String putResponseJson = executePutRequest("PUT", putPath, jsonBody.toString());

            return putResponseJson;
        } catch (Exception e) {
            log.error("[CafeUpdatePrice] 요청 에러 (cafeNo={}, salePrice={}, e.getMessage()={})",
                    cafeNo, salePrice, e.getMessage());
            return "{}";
        }
    }

    public String updateStock(
            String cafeNo, String cafeOptCode, Integer stock
    ) {
        try {
            String putPath = String.format("/api/v2/admin/products/%s/variants/%s/inventories", cafeNo, cafeOptCode);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("product_no", Integer.valueOf(cafeNo)); // 바깥쪽 필드
            jsonBody.put("variant_code", cafeOptCode); // 바깥쪽 필드

            JSONObject requestObj = new JSONObject();
            requestObj.put("use_inventory", "T");
            requestObj.put("display_soldout", "T");
            requestObj.put("quantity", stock);

            jsonBody.put("request", requestObj); // 바깥쪽에 request 키로 추가

            // 실제 PUT 요청 실행
            String putResponseJson = executePutRequest("PUT", putPath, jsonBody.toString());

            return putResponseJson;
        } catch (Exception e) {
            log.error("[CafeUpdateStock] 요청 에러 (cafeNo={}, cafeOptCode={}, stock={}, e.getMessage()={})",
                    cafeNo, cafeOptCode, stock, e.getMessage());
            return "{}";
        }
    }

    public String registerProduct(
            ProductRegistrationRequest request
    ) {
        try {
            // 1. 파일 경로 기준 이미지 파일 리스트 생성
            List<String> imagePathList = request.getImageFiles();
            List<File> imageFiles = imagePathList.stream()
                    .map(File::new)
                    .collect(Collectors.toList());
            // 2. 이미지 업로드 API 호출
            List<String> cafeUploadedImageLinks = uploadImages(imageFiles);
            // 3. DTO 세팅: 대표이미지/추가이미지
            CafeProductRequest cafeProductRequest = CafeProductRequest.from(request, cafeUploadedImageLinks);
            // 4. 상품 등록 API 호출
            String postPath = "/api/v2/admin/products";
            Map<String, Object> jsonBody = objectMapper.convertValue(cafeProductRequest, Map.class);
            String responseJson = executePutRequest("POST", postPath, jsonBody.toString());
            return responseJson;

        } catch (Exception e) {
            log.error("[CafeRegisterProduct] 요청 에러 (request={}, e.getMessage()={})",
                    request, e.getMessage());
            return "{}";
        }
    }

    private List<String> uploadImages(List<File> imageFiles) {
        try {
            // 1. 이미지를 Base64로 변환
            List<String> base64List = new ArrayList<>();
            for (File imageFile : imageFiles) {
                try (FileInputStream fis = new FileInputStream(imageFile)) {
                    byte[] bytes = fis.readAllBytes();
                    String encodedString = Base64.getEncoder().encodeToString(bytes);
                    base64List.add(encodedString);
                }
            }

            // 2. JSON body 준비
            ObjectNode rootNode = objectMapper.createObjectNode();
            ArrayNode requestsNode = objectMapper.createArrayNode();
            for (String base64 : base64List) {
                ObjectNode imageNode = objectMapper.createObjectNode();
                imageNode.put("image", base64);
                requestsNode.add(imageNode);
            }
            rootNode.set("requests", requestsNode);
            String jsonBody = objectMapper.writeValueAsString(rootNode);

            // 3. API 호출
            String postResponseJson = executePutRequest("POST", "/api/v2/admin/products/images", jsonBody);

            // 4. 업로드 결과 URL 리스트 추출 (예시: 응답에서 "src" 필드 추출)
            List<String> uploadedUrls = new ArrayList<>();
            JsonNode root = objectMapper.readTree(postResponseJson);
            if (root.has("images")) {
                for (JsonNode imageNode : root.get("images")) {
                    String url = imageNode.get("src").asText(); // 실제 url 필드는 "src"
                    uploadedUrls.add(url);
                }
            }

            // JSON 배열 반환이 필요하면: return mapper.writeValueAsString(uploadedUrls);
            // 문자열 리스트로 반환:
            return uploadedUrls;

        } catch (Exception e) {
            log.error("[Cafe24UploadImages] 요청 에러 (e.getMessage()={})", e.getMessage());
            return null;
        }
    }
}
