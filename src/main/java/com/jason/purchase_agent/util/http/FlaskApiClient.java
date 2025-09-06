package com.jason.purchase_agent.util.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FlaskApiClient {
  public static void main(String[] args) throws Exception {
    String apiUrl = "http://localhost:5000/api/iherb/top10";  // Flask 서버 주소

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(apiUrl))
      .GET()
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // 응답 코드 및 Body 출력
    System.out.println("Status code: " + response.statusCode());
    System.out.println("Response body: " + response.body());
  }
}