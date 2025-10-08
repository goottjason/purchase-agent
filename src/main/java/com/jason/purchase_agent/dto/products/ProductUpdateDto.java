package com.jason.purchase_agent.dto.products;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductUpdateDto {
    private String batchId; // nullable (미전달시 서비스에서 생성)
    private List<ProductUpdateItemDto> updateItems;  // 업데이트할 상품 리스트

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductUpdateItemDto {
        private String code;                // 상품코드
        private Integer salePrice;          // 새로운 판매가격
        private Integer stock;              // 새로운 재고수량
        private boolean priceChanged;
        private boolean stockChanged;

        // 마켓별 상품ID
        private String vendorItemId;        // 쿠팡 핵심 옵션/품목ID
        private String sellerProductId;     // 쿠팡 대표상품ID
        private String smartstoreId;
        private String originProductNo;
        private String elevenstId;
    }
}
