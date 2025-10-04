package com.jason.purchase_agent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @Column(name = "code", length = 20)
    private String code; // PK

    @ManyToOne
    @JoinColumn(name = "supplier_code", referencedColumnName = "supplier_code")
    private Supplier supplier; // Supplier 엔티티와 연결 (FK)

    @Column(name = "link", length = 500, nullable = false)
    private String link;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "kor_name", length = 500, nullable = false)
    private String korName;

    @Column(name = "eng_name", length = 500, nullable = false)
    private String engName;

    @Column(name = "unit_value")
    private Integer unitValue;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "brand_name", length = 20)
    private String brandName;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "buy_price")
    private Double buyPrice;

    @Column(name = "shipping_cost")
    private Double shippingCost;

    @Column(name = "details_html", columnDefinition = "TEXT")
    private String detailsHtml;

    @Column(name = "pack_qty", nullable = false)
    private Integer packQty;

    @Column(name = "sale_price")
    private Integer salePrice;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "margin_rate", nullable = false)
    private Double marginRate = 25.0;

    @Column(name = "image_links", columnDefinition = "TEXT")
    private String imageLinks;

    @Column(name = "uploaded_image_links", columnDefinition = "TEXT")
    private String uploadedImageLinks;

    @Column(name = "product_type", length = 20)
    private String productType;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ProductChannelMapping과의 1:1 연관관계
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProductChannelMapping productChannelMapping;

}