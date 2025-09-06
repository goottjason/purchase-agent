package com.jason.purchase_agent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_channel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleChannel {
  @Id
  @Column(name = "channel_code", length = 10)
  private String channelCode;

  @Column(name = "channel_name", length = 50)
  private String channelName;

  @Column(name = "fee_rate", precision = 5, scale = 4)
  private BigDecimal feeRate;
}
