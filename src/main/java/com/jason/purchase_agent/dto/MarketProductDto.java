package com.jason.purchase_agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketProductDto {
    private String korName;
    private String engName;
    private int buyPrice;
    private int packQty;
    private int salePrice;
    private String link;
    private String details;
}
