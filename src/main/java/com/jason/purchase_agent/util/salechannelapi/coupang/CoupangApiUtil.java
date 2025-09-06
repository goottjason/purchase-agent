package com.jason.purchase_agent.util.salechannelapi.coupang;

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
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import static com.jason.purchase_agent.util.http.UrlUtils.buildParamsString;
import static com.jason.purchase_agent.util.http.UrlUtils.buildPathWithParams;


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

  public static void executeRequest(String method, String path, Map<String, String> params) {
    try {
      System.out.println("method = " + method);
      System.out.println("path = " + path);
      // 1. GMT 표준 시간 생성 (쿠팡 API 요구사항)
      SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd'T'HHmmss'Z'");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      String datetime = sdf.format(new Date());

      String queryString = buildParamsString(params);
      System.out.println("queryString = " + queryString);

      // 2. HMAC 서명 생성
      String signature = generateHmacSignature(method, path, datetime, queryString);

      // 3. CEA 인증 헤더 포맷팅
      String authorization = String.format(
        "CEA algorithm=HmacSHA256, access-key=%s, signed-date=%s, signature=%s",
        ACCESS_KEY, datetime, signature
      );
      String fullPath = buildPathWithParams(path, params);
      String fullUrl = API_ENDPOINT + fullPath;
      System.out.println("fullUrl = " + fullUrl);
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
        conn.setRequestProperty("Content-Length", "0");
        try (OutputStream os = conn.getOutputStream()) {
          os.write(new byte[0]); // 빈 본문 전송
        }

      } else if ("DELETE".equals(method)) {
        // DELETE가 빈 본문을 요구하는 경우 (쿠팡 API 스펙에 따라)
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Length", "0");
        try (OutputStream os = conn.getOutputStream()) {
          os.write(new byte[0]); // 빈 본문 전송
        }

      } else if ("GET".equals(method) || "HEAD".equals(method)) {
        // GET/HEAD는 본문 없음 - 아무것도 하지 않음
      }

      // 5. 응답 처리 로직
      int responseCode = conn.getResponseCode();

      // ▼▼▼ 수정된 코드 ▼▼▼
      InputStream inputStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
      if ("gzip".equalsIgnoreCase(conn.getContentEncoding())) {
        inputStream = new GZIPInputStream(inputStream);
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

      // 응답 데이터 수집
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        response.append(line);
      }
      br.close();

      // 결과 출력
      System.out.println("●Coupang : Response Code: " + responseCode);
      System.out.println("●Coupang : Response Body: " + response.toString());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}


  /*public static String generate(
    String method, String uri, String secretKey, String accessKey
  ) throws Exception {

    long timestamp = System.currentTimeMillis();
    String message = method + " " + uri + "\n" + timestamp + "\n" + accessKey;

    Mac hasher = Mac.getInstance("HmacSHA256");
    hasher.init(new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256"));
    byte[] hash = hasher.doFinal(message.getBytes("UTF-8"));
    String signature = Base64.getEncoder().encodeToString(hash);

    return "CEA algorithm=HmacSHA256, access-key=" + accessKey +
      ", signed-date=" + timestamp +
      ", signature=" + signature;
  }

  public static void getReturnRequests(String createdAtFrom, String createdAtTo, String status) {
    String method = "GET";
    String path = "/v2/providers/openapi/apis/api/v4/vendors/" + VENDOR_ID + "/returnRequests";
    CloseableHttpClient client = null;
    try {
      client = HttpClients.createDefault();

      // 1. URI(쿼리파라미터 포함) 먼저 작성 (헤더용, 실제 요청용 공통)
      URIBuilder uriBuilder = new URIBuilder()
        .setScheme(SCHEMA)
        .setHost(HOST)
        .setPort(PORT)
        .setPath(path)
        .addParameter("createdAtFrom", createdAtFrom)
        .addParameter("createdAtTo", createdAtTo)
        .addParameter("status", status);

      String fullUriForAuth = uriBuilder.toString(); // 헤더 서명용 (쿼리포함 전체)
      String fullUriForRequest = uriBuilder.build().toString(); // 요청용

      // 인증 헤더 생성 (공식 Hmac.generate와 동등)
      String authorization = generate(method, fullUriForAuth, SECRET_KEY, ACCESS_KEY);
      System.out.println("Authorization = " + authorization);

      // 요청객체 빌드
      HttpGet get = new HttpGet(fullUriForRequest);
      get.addHeader("Authorization", authorization);
      get.addHeader("content-type", "application/json");

      CloseableHttpResponse response = client.execute(get);

      System.out.println("status code:" + response.getStatusLine().getStatusCode());
      System.out.println("status message:" + response.getStatusLine().getReasonPhrase());
      HttpEntity entity = response.getEntity();
      System.out.println("result:" + EntityUtils.toString(entity));
      response.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (client != null)
          client.close();
      } catch (Exception ignore) {}
    }*/



























  /*// Coupang OpenAPI 헤더 생성 (인증)
  public static HttpHeaders buildHeaders(String method, String url, String query) {
    long timestamp = System.currentTimeMillis();
    String msg =
      method + " " + url + "\n"   // GET /path..
        + timestamp + "\n"
        + ACCESS_KEY;
    String signature;
    try {
      Mac hmac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "HmacSHA256");
      hmac.init(secretKey);
      byte[] hash = hmac.doFinal(msg.getBytes("UTF-8"));
      signature = Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      throw new RuntimeException("쿠팡 HMAC 실패", e);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization",
      "CEA algorithm=HmacSHA256, access-key=" + ACCESS_KEY +
        ", signed-date=" + timestamp +
        ", signature=" + signature);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  public static String generateHmacSignature(String method, String path, String datetime) throws Exception {

    // 요청 메시지 조립: 타임스탬프 + HTTP 메서드 + API 경로
    String message = datetime + method + path;

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

  public static String executeRequest(String method, String path) {
    try {

      // 1. GMT 표준 시간 생성 (쿠팡 API 요구사항)
      SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd'T'HHmmss'Z'");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      String datetime = sdf.format(new Date());

      // 2. HMAC 서명 생성
      String signature = generateHmacSignature(method, path, datetime);
      System.out.println("signature = " + signature);

      // 3. CEA 인증 헤더 포맷팅
      String authorization = String.format(
        "CEA algorithm=HmacSHA256, access-key=%s, signed-date=%s, signature=%s",
        ACCESS_KEY, datetime, signature
      );

      // 4. HTTP 연결 설정
      URL url = new URL(API_ENDPOINT + path);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(method);
      conn.setRequestProperty("Authorization", authorization);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Content-Length", "0");
      conn.setDoOutput(true);
      try (OutputStream os = conn.getOutputStream()) {
        // 빈 바이트 배열 전송
        os.write(new byte[0]);
      }
      // 5. 응답 처리 로직
      int responseCode = conn.getResponseCode();
      // ▼▼▼ 수정된 코드 ▼▼▼
      InputStream inputStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
      if ("gzip".equalsIgnoreCase(conn.getContentEncoding())) {
        inputStream = new GZIPInputStream(inputStream);
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

      // 응답 데이터 수집
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        response.append(line);
      }
      br.close();

      // 결과 출력
      *//*System.out.println("●Coupang : Response Code: " + responseCode);
      System.out.println("●Coupang : Response Body: " + response.toString());*//*

      return response.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }*/
