package com.jason.purchase_agent.dto.products;


import lombok.*;

import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchAutoPriceStockUpdateRequest {
    private Integer marginRate;
    private Integer couponRate;
    private Integer minMarginPrice;
    private List<ProductDto> products; // 상품 ID 리스트 (or Product DTO 리스트, 실제 구조에 따라)
}
