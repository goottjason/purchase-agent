package com.jason.purchase_agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDto {
  private String supplierCode;   // 공급처 코드 (PK)
  private String supplierName;   // 공급처 이름
  private String currencyCode;   // 통화 코드 (외래키)
}
