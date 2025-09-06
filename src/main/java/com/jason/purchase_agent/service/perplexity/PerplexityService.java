package com.jason.purchase_agent.service.perplexity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.perplexity.PerplexityMessage;
import com.jason.purchase_agent.dto.perplexity.PerplexityRequest;
import com.jason.purchase_agent.dto.perplexity.PerplexityResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class PerplexityService {

  private final WebClient webClient;

  @Value("${perplexity.api.key}")
  private String apiKey;

  private final String API_URL = "https://api.perplexity.ai/chat/completions";

  public PerplexityService(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  public Mono<String> sendQuery(String userMessage) {
    Map<String, Object> request = createRequest(userMessage);

    return webClient.post()
      .uri(API_URL)
      .header("Authorization", "Bearer " + apiKey)
      .header("Content-Type", "application/json")
      .bodyValue(request)
      .retrieve()
      /*.onStatus(HttpStatusCode::is4xxClientError, response -> {
        return response.bodyToMono(String.class)
          .flatMap(errorBody -> {
            System.err.println("API 오류 응답: " + errorBody);
            return Mono.error(new RuntimeException("클라이언트 오류: " + errorBody));
          });
      })*/
      .bodyToMono(String.class)
      .doOnNext(response -> printDecodedJson(response))
      .doOnError(error -> {
        System.err.println("API 호출 오류 상세: " + error.getMessage());
      });
  }

  private Map<String, Object> createRequest(String userMessage) {
    Map<String, Object> systemMessage = Map.of(
      "role", "system",
      "content", "정확하고 간결하게 대답하세요."
    );

    Map<String, Object> userMessageObj = Map.of(
      "role", "user",
      "content", userMessage
    );

    return Map.of(
      "model", "sonar", // 업데이트된 모델명
      "messages", List.of(systemMessage, userMessageObj),
      "max_tokens", 500,
      "temperature", 0.7
    );
  }

  public void printDecodedJson(String unicodeJson) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Object obj = mapper.readValue(unicodeJson, Object.class);
      // 한글이 깨지지 않도록 pretty print해서 sout
      String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
      System.out.println(prettyJson);
    } catch (Exception e) {
      System.out.println("파싱 실패: " + e.getMessage());
      System.out.println("원본: " + unicodeJson);
    }
  }
}
