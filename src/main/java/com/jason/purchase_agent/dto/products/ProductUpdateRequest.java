package com.jason.purchase_agent.dto.products;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateRequest {
    private String code;
    private ProductDto productDto;
    private boolean priceChanged;
    private boolean stockChanged;
}