package com.jason.purchase_agent.external.coupang;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import lombok.extern.slf4j.Slf4j;

import static com.jason.purchase_agent.util.http.UrlUtils.buildParamsString;
import static com.jason.purchase_agent.util.http.UrlUtils.buildPathWithParams;

@Slf4j
public class CoupangApiUtil {
    public static final String VENDOR_ID = "A00213055";
    public static final String API_ENDPOINT = "https://api-gateway.coupang.com";
    public static final String ACCESS_KEY = "97211801-f495-4fe2-bef3-b614e9d8aaba";
    public static final String SECRET_KEY = "5f72579c452d8aaf5c718156956ba102dab7863c";

    public static String generateHmacSignature(String method, String path, String datetime, String queryString) throws Exception {

        if (queryString != null && queryString.startsWith("?")) {
            queryString = queryString.substring(1);
        }

        // 요청 메시지 조립: 타임스탬프 + HTTP 메서드 + API 경로
        String message = datetime + method + path + queryString;

        // HMAC-SHA256 알고리즘 초기화
        Mac hmac = Mac.getInstance("HmacSHA256");

        // 비밀 키로 HMAC 인스턴스 초기화
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKey);

        // 메시지 해싱 및 16진수 문자열 변환
        byte[] rawHmac = hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(rawHmac);
    }

    // 바이트 배열 → 16진수 문자열 변환 유틸리티
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            // 각 바이트를 2자리 16진수로 포매팅
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static String executeRequest(
            String method, String path,
            Map<String, String> params, Map<String, Object> body
    ) {
        try {
            // 1. GMT 표준 시간 생성 (쿠팡 API 요구사항)
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd'T'HHmmss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String datetime = sdf.format(new Date());

            String queryString = (params != null) ? buildParamsString(params) : "";

            // 2. HMAC 서명 생성
            String signature = generateHmacSignature(method, path, datetime, queryString);

            // 3. CEA 인증 헤더 포맷팅
            String authorization = String.format(
                    "CEA algorithm=HmacSHA256, access-key=%s, signed-date=%s, signature=%s",
                    ACCESS_KEY, datetime, signature
            );
            String fullPath = buildPathWithParams(path, params);
            String fullUrl = API_ENDPOINT + fullPath;

            // 4. HTTP 연결 설정
            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Authorization", authorization);
            conn.setRequestProperty("Content-Type", "application/json");

            // ▼▼▼ Method별 조건부 처리 ▼▼▼
            if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
                // POST/PUT/PATCH는 본문이 있을 수 있음
                conn.setDoOutput(true);
                String bodyJson = "";
                if (body != null && !body.isEmpty()) {
                    bodyJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
                }
                byte[] bodyBytes = bodyJson.getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bodyBytes); // 빈 본문 전송
                }

            } else if ("DELETE".equals(method)) {
                // DELETE가 빈 본문을 요구하는 경우
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Length", "0");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(new byte[0]); // 빈 본문 전송
                }
            }
            // 5. 응답 처리 로직
            int responseCode = conn.getResponseCode();
            InputStream inputStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
            if ("gzip".equalsIgnoreCase(conn.getContentEncoding())) {
                inputStream = new GZIPInputStream(inputStream);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            return response.toString();
        } catch (Exception e) {
            log.error("[CoupangExecuteRequest] 요청 에러 (e.getMessage()={})", e.getMessage());
            return e.getMessage();
        }
    }
}
