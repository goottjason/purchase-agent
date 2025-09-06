package com.jason.purchase_agent.service.channel.elevenst;

import com.jason.purchase_agent.dto.ProductDto;
import com.jason.purchase_agent.dto.channel.elevenst.ElevenstListingEnrollRequest;
import com.jason.purchase_agent.util.salechannelapi.elevenst.ElevenstApiUtil;
import com.jason.purchase_agent.service.channel.ChannelService;
import com.jason.purchase_agent.util.converter.ElevenstXmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElevenstService implements ChannelService {
  private final ElevenstXmlConverter xmlConverter = new ElevenstXmlConverter();

  @Override
  public void register(ProductDto productDto) {
    // 1) ProductDto → ElevenstListingEnrollRequest
    ElevenstListingEnrollRequest request = mapToElevenst(productDto);
    // 2. API 요청 직전에 XML 변환
    String xmlRequest = xmlConverter.convertToXml(request);
    // 3) 전송
    String response = ElevenstApiUtil.sendRequest(
      "POST",
      "/openapi/OpenApiService.tmall",
      xmlRequest);
  }

  private ElevenstListingEnrollRequest mapToElevenst(ProductDto p) {
    // 1. 파트너 카테고리 객체부터 빌더로 구성
    ElevenstListingEnrollRequest.PartnerCategory partnerCategory =
      ElevenstListingEnrollRequest.PartnerCategory.builder()
        .leafCategoryYn("Y")                       // 예: 마지막 노드
//        .categoryName1(p.getCategoryLv1())         // 대분류
//        .categoryName2(p.getCategoryLv2())         // 중분류
//        .categoryName3(p.getCategoryLv3())         // 소분류
//        .categoryName4(p.getCategoryLv4())         // 세분류
        .lastCategoryNo(p.getCategoryCode())       // 11번가 최종 코드
        .build();

    // 2. 최상위 Product 객체 빌더
    return ElevenstListingEnrollRequest.builder()
      // ───────── 기본(필수) 정보 ─────────
      .prdNm(p.getKorName())                         // 상품명
      .prdNmEng(p.getEngName())                      // 영문명 (없으면 null)
//      .brand(p.getBrand())
//      .selPrc(String.valueOf(p.getPrice()))
      .dispCtgrNo(p.getCategoryCode())               // 11번가 전시 카테고리
      .partnerCategory(partnerCategory)              // 위에서 만든 중첩 객체

      // ───────── 이미지 예시 ─────────
//      .prdImage01(p.getMainImageUrl())               // 대표 이미지
//      .prdImage02(p.getSubImages().get(0))           // 서브 1
//      .prdImage03(p.getSubImages().get(1))           // 서브 2

      // ───────── 상세설명 & 고시정보 ─────────
      .htmlDetail(p.getDetailsHtml())
      .productNotification(
        ElevenstListingEnrollRequest.ProductNotification.builder()
          .type("전자상거래에 관한 상품정보 제공에 관한 고시")
          .item(
            ElevenstListingEnrollRequest.ProductNotification.Item.builder()
              .code("A001")              // 예시 코드
              .name("상세설명 참조")
              .build())
          .build())

      // ───────── 배송 기본값 예시 ─────────
      .dlvCnAreaCd("01")          // 전국
      .dlvWyCd("01")              // 택배
      .bndlDlvCnYn("Y")           // 묶음배송
      .dlvCstPayTypCd("10")       // 선불
      .jejuDlvCst("3000")
      .islandDlvCst("5000")
      .prdWght("500")             // g

      // 필요 필드는 계속 체인 형태로 추가
      .build();
  }
}
