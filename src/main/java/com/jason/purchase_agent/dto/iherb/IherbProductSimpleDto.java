package com.jason.purchase_agent.dto.iherb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IherbProductSimpleDto {
  private String campaignImage; // 대표이미지 (campaignImages[0])
  private Long id;              // 상품아이디
  private String displayName;   // 제품명
  private Double discountPriceAmount; // 구매가
}
