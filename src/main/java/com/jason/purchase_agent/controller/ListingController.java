package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.dto.channel.SmartstoreProductDto;
import com.jason.purchase_agent.service.channel.coupang.CoupangListingSyncService;
import com.jason.purchase_agent.service.channel.smartstore.SmartstoreListingSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/sync")
@RequiredArgsConstructor
public class ListingController {

  private final CoupangListingSyncService coupangListingSyncService;
  private final SmartstoreListingSyncService smartstoreListingSyncService;

  // 브라우저에서 직접 호출
  @GetMapping("/coupang")
  @ResponseBody
  public String syncCoupang() throws Exception {
    coupangListingSyncService.syncCoupangProducts();
    return "Coupang 상품 동기화 완료!";
  }
  // 브라우저에서 직접 호출
  @GetMapping("/smartstore")
  public String syncSmartstore(Model model) throws Exception {
    List<SmartstoreProductDto> smartstoreProductDtos = smartstoreListingSyncService.syncSmartstoreProducts();

    System.out.println("smartstoreProductDtos = " + smartstoreProductDtos);

    model.addAttribute("smartstoreProductDtos", smartstoreProductDtos);

    return "admin/sync/smartstore-result"; // 결과를 보여줄 템플릿 이름
  }
}