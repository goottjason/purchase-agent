package com.jason.purchase_agent.controller;


import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreListingEnrollRequest;
import com.jason.purchase_agent.service.channel.smartstore.SmartstoreProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
public class SmartstoreProductController {

  private final SmartstoreProductService productService;

  // 등록 폼 페이지 GET (템플릿에서 JSON 예시/빈 폼 생성)
  @GetMapping("/smartstore/new")
  public String newProductForm(Model model) {
    // 템플릿에 빈 dto 객체 전달 (처음 렌더링용)
    model.addAttribute("dto", SmartstoreListingEnrollRequest.builder().build());
    return "smartstore/new/smartstore-product-form";
  }

  // 상품 등록 처리 POST
  @PostMapping("/smartstore/register")
  @ResponseBody
  public ResponseEntity<?> registerProduct(@RequestBody SmartstoreListingEnrollRequest dto) {
    // 실제 서비스에 등록 요청 (실서비스/Mock)
    String result = productService.registerProduct(dto);
    return ResponseEntity.ok().body(result);
  }
}