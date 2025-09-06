package com.jason.purchase_agent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.jason.purchase_agent.enums.SaleChannel;

import java.math.BigDecimal;

@Entity
@Table(name = "listing")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Listing {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "listing_id")
  private Integer listingId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_code", nullable = false)
  private Product product;

  @Enumerated(EnumType.STRING)
  @Column(name = "channel_code", nullable = false)
  private SaleChannel saleChannel;

  @Column(name = "channel_product_code", length = 50, unique = true)
  private String channelProductCode;

  @Column(name = "stock")
  private Integer stock;

  @Column(name = "sale_price", precision = 10, scale = 2)
  private BigDecimal salePrice;

  @Column(name = "margin_rate", precision = 5, scale = 4)
  private BigDecimal marginRate;
}
