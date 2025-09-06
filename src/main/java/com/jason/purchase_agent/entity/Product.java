package com.jason.purchase_agent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  @Id
  @Column(name = "product_code", length = 20)
  private String productCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_code", nullable = false)
  private Supplier supplier;

  @Column(name = "source_link", columnDefinition = "TEXT")
  private String sourceLink;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_code")
  private Category category;

  @Column(name = "eng_name", length = 255)
  private String engName;

  @Column(name = "kor_name", length = 255)
  private String korName;

  @Column(name = "unit_value", precision = 10, scale = 2)
  private BigDecimal unitValue;

  @Column(name = "unit", length = 20)
  private String unit;

  @Column(name = "purchase_cost", precision = 10, scale = 2)
  private BigDecimal purchaseCost;

  @Column(name = "shipping_cost", precision = 10, scale = 2)
  private BigDecimal shippingCost;

  @Column(name = "pack_size")
  private Integer packSize;

  @Column(name = "is_available")
  private Boolean isAvailable;

  @Column(name = "details_html", columnDefinition = "TEXT")
  private String detailsHtml;

  // 편의 메서드 추가
  public void setSupplierCode(String supplierCode) {
    if (supplierCode == null || supplierCode.isBlank()) {
      this.supplier = null;
    } else {
      Supplier supplier = new Supplier();
      supplier.setSupplierCode(supplierCode);
      this.supplier = supplier;
    }
  }
  public String getSupplierCode() {
    return supplier != null ? supplier.getSupplierCode() : null;
  }

  public void setCategoryCode(String categoryCode) {
    if (categoryCode == null || categoryCode.isBlank()) {
      this.category = null;
    } else {
//      Category category = new Category();
//      category.setCategoryCode(categoryCode);
//      this.category = category;
    }
  }
//  public String getCategoryCode() {
//    return category != null ? category.getCategoryCode() : null;
//  }
}
