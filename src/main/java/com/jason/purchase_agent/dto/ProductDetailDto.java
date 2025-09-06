package com.jason.purchase_agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDto {
  private String productCode;
  private String engName;
  private String supplierCode;
  private String supplierName;
  private BigDecimal purchasePrice;
  private BigDecimal weight;
  private Boolean isAvailable;
  private String categoryName;
  private String sourceLink;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // 추가 계산 필드들 (필요시)
  private BigDecimal exchangeRate;
  private String currencyCode;
}