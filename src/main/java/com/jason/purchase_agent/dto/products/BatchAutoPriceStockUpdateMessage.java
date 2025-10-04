package com.jason.purchase_agent.dto.products;
import lombok.*;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BatchAutoPriceStockUpdateMessage {
    private String batchId;
    private String requestedBy;
    private LocalDateTime requestedAt;
    private Integer marginRate;
    private Integer couponRate;
    private Integer minMarginPrice;
    private ProductDto productDto;
}
