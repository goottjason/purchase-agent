package com.jason.purchase_agent.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrawlAndUpdateEachProductBySupplierMessage {
    private String batchId;
    private ProductDto productDto;
    private Integer marginRate;
    private Integer couponRate;
    private Integer minMarginPrice;
}
