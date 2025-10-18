package com.jason.purchase_agent.dto.products.update;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PriceStockChangeInfo {
    private final boolean priceChanged;
    private final boolean stockChanged;
}
