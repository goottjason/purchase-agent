package com.jason.purchase_agent.controller.enroll;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Controller
@RequestMapping("/enroll")
public class ProductEnrollController {

  // 홈 화면
  @GetMapping("")
  public String home(Model model){
    /*model.addAttribute("statusCode", "");
    model.addAttribute("responseBody", "");*/
    return "enroll/index"; // 아래 템플릿 이름과 맞춤
  }

  // ■API : iHerb 링크 가져오기
  @GetMapping("/fetch-iherb-links")
  @ResponseBody // 템플릿 이름 아님, 리턴하는 문자열을 그대로 응답 본문에 담아서 보냄
  public List<String> fetchIherbLinks(Model model) {
    // Flask API 호출 (그 API에서 id만 리스트로 뽑아서 return)

    /*String responseBody = "";
    int statusCode = 0;
    try {
      String apiUrl = "http://localhost:5000/api/iherb/top10";
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(apiUrl))
        .GET()
        .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      responseBody = response.body();
      statusCode = response.statusCode();
      System.out.println("response = " + response);
      System.out.println("statusCode = " + statusCode);
    } catch (Exception e) {
      responseBody = "서버 연결 실패 : " + e.getMessage();
      e.printStackTrace();    // 반드시 추가!!
    }
    model.addAttribute("statusCode", statusCode);
    model.addAttribute("responseBody", responseBody);*/

    return List.of("147971", "1479", "147973"); // 임시
  }


}
