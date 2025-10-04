package com.jason.purchase_agent.dto.products;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateRequest {
    private String title;
    private String korName;
    private String engName;
    private String brandName;
    private Integer unitValue;
    private String unit;
    private Double weight;
    private String link;
    private Double buyPrice;
    private Integer salePrice;
    private Integer stock;
    private Integer packQty;
    private Double marginRate;
    private Double shippingCost;
    private String detailsHtml;
    private String memo;

    // 채널 매핑 필드
    private String vendorItemId;
    private String sellerProductId;
    private String smartstoreId;
    private String originProductNo;
    private String elevenstId;
}