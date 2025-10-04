package com.jason.purchase_agent.dto.products;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelUpdateMessage {
    private String type; // PRICE_UPDATE, STOCK_UPDATE
    private String channel; // coupang, smartstore, elevenst
    private String productCode;
    private String channelProductId;
    private Integer newPrice;
    private Integer newStock;
    private LocalDateTime timestamp;
}