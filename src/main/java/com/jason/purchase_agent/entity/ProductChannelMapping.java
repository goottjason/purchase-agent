package com.jason.purchase_agent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity @Table(name = "product_channel_mapping")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductChannelMapping {

    @Id @Column(name="product_code")
    private String productCode; // Product 테이블의 code (PK & FK)
    // Product 엔티티와 1:1 연관 (양방향 매핑시)
    @OneToOne
    @JoinColumn(name = "product_code", referencedColumnName = "code", insertable = false, updatable = false)
    private Product product;

    @Column(length = 40)
    private String vendorItemId;      // 쿠팡 핵심 옵션/품목ID
    @Column(length = 40)
    private String sellerProductId;   // 쿠팡 대표상품ID
    @Column(length = 40)
    private String elevenstId;        // 11번가 상품ID
    @Column(length = 40)
    private String smartstoreId;      // 스마트스토어 노출상품ID
    @Column(length = 40)
    private String originProductNo;   // 스마트스토어 원상품ID(API에서 실제 사용)



    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}