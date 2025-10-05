package com.jason.purchase_agent.dto.products;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductSearchDto {
    private String searchKeyword;           // 검색 키워드
    private Integer pageSize;               // 페이지 크기 (50, 100, 250, 500, 1000)
    private Integer pageNumber;             // 페이지 번호 (0부터 시작)

    // 기본값 설정
    public Integer getPageSize() {
        return pageSize != null ? pageSize : 50;
    }

    public Integer getPageNumber() {
        return pageNumber != null ? pageNumber : 0;
    }

    // ========== 엑셀 필터 추가

    // 새로운 필터 조건들
    private List<String> supplierCodes;     // 공급업체 코드 리스트 (다중 선택)
    private Boolean filterNullVendorItemId; // 쿠팡 품목ID null 필터
    private Boolean filterNullSellerProductId; // 쿠팡 상품ID null 필터
    private Boolean filterNullSmartstoreId;  // 스마트스토어ID null 필터
    private Boolean filterNullOriginProductNo;  // 스마트스토어ID null 필터
    private Boolean filterNullElevenstId;    // 11번가ID null 필터

    // 필터 적용 여부 체크
    public boolean hasSupplierFilter() {
        return supplierCodes != null && !supplierCodes.isEmpty();
    }

    public boolean hasChannelNullFilters() {
        return Boolean.TRUE.equals(filterNullVendorItemId) ||
                Boolean.TRUE.equals(filterNullSellerProductId) ||
                Boolean.TRUE.equals(filterNullSmartstoreId) ||
                Boolean.TRUE.equals(filterNullOriginProductNo) ||
                Boolean.TRUE.equals(filterNullElevenstId);
    }
}
