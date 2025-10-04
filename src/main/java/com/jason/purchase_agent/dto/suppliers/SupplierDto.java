package com.jason.purchase_agent.dto.suppliers;

import com.jason.purchase_agent.entity.Supplier;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierDto {
    private String supplierCode;   // 공급처 코드 (PK)
    private String supplierName;   // 공급처 이름
    private String currencyCode;   // 통화 코드 (외래키)
    private Long productCount;     // 해당 공급업체의 상품 개수

    /**
     * JPA SELECT new 구문에서 사용할 생성자
     * COUNT() 함수는 Long 타입을 반환하므로 Long 타입으로 받아야 합니다
     */
    public SupplierDto(String supplierCode, String supplierName, Long productCount) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.productCount = productCount;
    }

    /**
     * Supplier에 supplierName 필드가 없는 경우를 위한 생성자
     */
    public SupplierDto(String supplierCode, Long productCount) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierCode; // 코드를 이름으로 사용
        this.productCount = productCount;
    }

    public SupplierDto(Supplier supplier, Long productCount) {
        this.supplierCode = supplier.getSupplierCode();
        this.supplierName = supplier.getSupplierName();
        this.currencyCode = supplier.getCurrency() != null ? supplier.getCurrency().getCurrencyCode() : null;
        this.productCount = productCount;
    }

}
