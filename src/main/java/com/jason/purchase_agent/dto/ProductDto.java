package com.jason.purchase_agent.dto;

import com.jason.purchase_agent.entity.Product;
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
public class ProductDto {
  private String productCode;
  private String supplierCode;
  private String sourceLink;
  private String categoryCode;
  private String engName;
  private String korName;
  private BigDecimal unitValue;
  private String unit;
  private BigDecimal purchaseCost;
  private BigDecimal shippingCost;
  private Integer packSize;
  private Boolean isAvailable;
  private String detailsHtml;

  public static ProductDto fromEntity(Product p) {
    ProductDto dto = new ProductDto();
    dto.setProductCode(p.getProductCode());
    dto.setSupplierCode(
      p.getSupplier() != null ? p.getSupplier().getSupplierCode() : null
    );
    dto.setIsAvailable(p.getIsAvailable());
    dto.setSourceLink(p.getSourceLink());
    dto.setCategoryCode(
      p.getCategory() != null ? p.getCategory().getId() : null
    );
    dto.setKorName(p.getKorName());
    dto.setEngName(p.getEngName());
    dto.setUnitValue(p.getUnitValue());
    dto.setUnit(p.getUnit());
    dto.setDetailsHtml(p.getDetailsHtml());
    dto.setPurchaseCost(p.getPurchaseCost());
    dto.setPackSize(p.getPackSize());
    dto.setShippingCost(p.getShippingCost());
    return dto;
  }
}