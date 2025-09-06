package com.jason.purchase_agent.service.channel.smartstore;


import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreListingEnrollRequest;
import org.springframework.stereotype.Service;

@Service
public class SmartstoreProductService {

  public String registerProduct(SmartstoreListingEnrollRequest dto) {
    // 여기서 실제 API 호출 로직(OkHttp/WebClient 등), 유효성 체크 가능
    // Mock 성공 메시지 반환
    return "상품 등록 테스트 성공! 입력값: " + (dto.getOriginProduct() != null ? dto.getOriginProduct().getName() : "null");
  }
}