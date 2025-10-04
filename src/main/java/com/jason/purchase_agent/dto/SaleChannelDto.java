package com.jason.purchase_agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleChannelDto {
    private String channelCode; // 수정시 필요
    private String channelName;
    private BigDecimal feeRate;
}
