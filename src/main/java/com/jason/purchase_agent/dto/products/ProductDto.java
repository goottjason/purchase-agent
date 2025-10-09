package com.jason.purchase_agent.dto.products;
import com.jason.purchase_agent.entity.Product;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDto {
    // Product 테이블 필드들
    private String code;                    // 상품코드 (PK)
    private String supplierCode;            // 공급업체코드
    private String link;                    // 상품링크
    private String title;                   // 상품명
    private String korName;                 // 한국어명 (검색용)
    private String engName;                 // 영어명 (검색용)
    private Integer unitValue;              // 단위값
    private String unit;                    // 단위
    private Double weight;
    private Double buyPrice;
    private Double shippingCost;
    private String detailsHtml;
    private Integer packQty;                // 포장수량
    private Integer salePrice;              // 판매가격
    private Integer stock;                  // 재고수량
    private Double marginRate;
    private String imageLink;
    private String uploadedImageLink;
    private String productType;
    private String memo;
    private String brandName;               // 브랜드명 (검색용)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // ProductChannelMapping 테이블 필드들
    private String vendorItemId;            // 쿠팡 핵심 옵션/품목ID
    private String sellerProductId;         // 쿠팡 대표상품ID
    private String smartstoreId;            // 스마트스토어 상품ID
    private String originProductNo;
    private String elevenstId;              // 11번가 상품ID
    private String cafeNo;
    private String cafeCode;
    private String cafeOptCode;

    // 정적 메서드 추가 (직접 매핑)
    public static ProductDto fromEntity(Product product) {
        return ProductDto.builder()
                .code(product.getCode())
                .supplierCode(String.valueOf(product.getSupplier()))
                .link(product.getLink())
                .title(product.getTitle())
                .korName(product.getKorName())
                .engName(product.getEngName())
                .unitValue(product.getUnitValue())
                .unit(product.getUnit())
                .weight(product.getWeight())
                .buyPrice(product.getBuyPrice())
                .shippingCost(product.getShippingCost())
                .detailsHtml(product.getDetailsHtml())
                .packQty(product.getPackQty())
                .salePrice(product.getSalePrice())
                .stock(product.getStock())
                .marginRate(product.getMarginRate())
                .imageLink(product.getImageLinks())
                .uploadedImageLink(product.getUploadedImageLinks())
                .productType(product.getProductType())
                .memo(product.getMemo())
                .brandName(product.getBrandName())
                // 채널 매핑 필드는 필요 시 별도 세팅
                .build();
    }

}
