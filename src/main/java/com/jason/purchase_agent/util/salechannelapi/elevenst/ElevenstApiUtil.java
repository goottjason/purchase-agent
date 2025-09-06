package com.jason.purchase_agent.util.salechannelapi.elevenst;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ElevenstApiUtil {
  private static final String API_KEY = "b7ac38eb89852b178b17a6a73da0b0c2";

  /**
   * 지정한 URL로 HTTP 요청을 보내고, 그 결과를 문자열로 반환하는 유틸리티 메서드.
   *
   * @param urlStr  요청을 보낼 URL (예: "http://api.example.com/...").
   * @param method  HTTP 메서드(GET, POST, PUT 등).
   * @return        응답 바디(텍스트, XML 등). 오류시 기본 XML 에러 문자열 반환.
   */
  public static String sendRequest(String urlStr, String method, String body) {

    HttpURLConnection conn = null;          // HTTP 연결 인스턴스
    BufferedReader in = null;               // 응답을 읽어올 리더
    OutputStreamWriter out = null;

    try {

      // 요청 URL 객체 생성
      URL url = new URL(urlStr);
      // URL로부터 HttpURLConnection 생성 및 캐스팅
      conn = (HttpURLConnection) url.openConnection();
      // HTTP 메서드(예: GET, POST 등) 설정
      conn.setRequestMethod(method);
      // API 인증키 요청 헤더 세팅 (API 별 요구)
      conn.setRequestProperty("openapikey", API_KEY);
      // 응답 문자셋(EUC-KR) 명시적 지정 (특정 OpenAPI에서 한글깨짐 방지용)
      conn.setRequestProperty("Accept-Charset", "EUC-KR");

      // ※ 전송할 Body가 있을 때만 설정
      if (body != null && !body.isEmpty()) {
        conn.setDoOutput(true);                                       // 바디 전송 허용
        conn.setRequestProperty("Content-Type",
          "application/xml; charset=EUC-KR");                   // 필요시 JSON으로 변경
        out = new OutputStreamWriter(conn.getOutputStream(), "EUC-KR");
        out.write(body);                                              // 2) 바디 쓰기
        out.flush();
      }

      // 실제 요청을 보내고 응답코드를 받음 (네트워크 레벨 요청 발생)
      int responseCode = conn.getResponseCode();

      // 요청 응답 처리를 위한 입력 스트림 생성 (문자셋 반드시 맞춤)
      in = new BufferedReader(new InputStreamReader(
        responseCode >= 200 && responseCode < 400
          ? conn.getInputStream()
          : conn.getErrorStream(),
        "EUC-KR"));

      // 응답 내용을 한 줄씩 읽어서 StringBuilder에 누적
      String inputLine;
      StringBuilder response = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }

      // 완성된 응답 문자열 반환
      return response.toString();

    } catch (Exception e) {

      // 네트워크 오류/파싱 실패 시 콘솔에 오류 및 요청 URL, 유형 출력
      System.err.println("Elevenst API 요청 실패: " + urlStr);
      System.err.println("Elevenst 오류 내용: " + e.getClass().getSimpleName() + " - " + e.getMessage());
      // XML 파싱에 실패해도 기본 결과 코드 XML 반환(서비스 장애때도 XML 파싱이 깨지지 않기 위함)
      return "<resultCode>ERROR</resultCode><message>네트워크 오류</message>";

    } finally {

      // 사용한 BufferedReader 꼭 close (자원 반환)
      try { if (in != null) in.close(); } catch (Exception ignore) {}
      // HttpURLConnection도 반드시 연결 해제
      if (conn != null) conn.disconnect();
    }
  }



}
