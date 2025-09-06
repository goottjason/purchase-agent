package com.jason.purchase_agent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Currency {
  @Id
  @Column(name = "currency_code", length = 3)
  private String currencyCode;

  @Column(name = "exchange_rate", precision = 10, scale = 2)
  private BigDecimal exchangeRate;
}