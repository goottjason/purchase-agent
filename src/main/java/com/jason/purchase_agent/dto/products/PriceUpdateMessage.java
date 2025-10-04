package com.jason.purchase_agent.dto.products;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceUpdateMessage {
    private String channel;         // coupang/smartstore/elevenst
    private String channelId;       // vendorItemId 등 외부 채널 상품 ID
    private String batchId;         // 배치 ID (상태추적용)
    private String productCode;     // 내부 상품코드
    private Integer salePrice;      // 가격 (원)
}
