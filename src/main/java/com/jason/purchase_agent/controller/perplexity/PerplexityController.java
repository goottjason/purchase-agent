package com.jason.purchase_agent.controller.perplexity;

import com.jason.purchase_agent.service.perplexity.PerplexityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/perplexity")
public class PerplexityController {

  private final PerplexityService perplexityService;

  public PerplexityController(PerplexityService perplexityService) {
    this.perplexityService = perplexityService;
  }

  @PostMapping("/query")
  public Mono<ResponseEntity<String>> query(@RequestBody Map<String, String> request) {
    String message = request.get("message");

    if (message == null || message.trim().isEmpty()) {
      return Mono.just(ResponseEntity.badRequest().body("메시지가 필요합니다."));
    }

    return perplexityService.sendQuery(message)
      .map(response -> ResponseEntity.ok(response))
      .onErrorReturn(ResponseEntity.status(500).body("서버 오류가 발생했습니다."));
  }
}