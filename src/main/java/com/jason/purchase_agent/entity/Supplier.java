package com.jason.purchase_agent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "supplier")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
  @Id
  @Column(name = "supplier_code", length = 10)
  private String supplierCode;

  @Column(name = "supplier_name", length = 255)
  private String supplierName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "currency_code")
  private Currency currency;


}