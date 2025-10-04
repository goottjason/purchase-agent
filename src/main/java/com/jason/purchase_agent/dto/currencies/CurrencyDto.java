package com.jason.purchase_agent.dto.currencies;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyDto {
    private String currencyCode;
    private BigDecimal exchangeRate;
}
