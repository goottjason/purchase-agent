package com.jason.purchase_agent.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceUpdateChannelMessage {
    private String batchId;         // 배치 ID (상태추적용)
    private String channel;         // coupang/smartstore/elevenst/cafe
    private String channelId1;      // vendorItemId 등 외부 채널 상품 ID
    private String channelId2;      // vendorItemId 등 외부 채널 상품 ID
    private String productCode;     // 내부 상품코드
    private Integer salePrice;      // 가격 (원)
    private Double marginRate;
}
