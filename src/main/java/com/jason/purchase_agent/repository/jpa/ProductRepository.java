package com.jason.purchase_agent.repository.jpa;

import com.jason.purchase_agent.dto.suppliers.SupplierDto;
import com.jason.purchase_agent.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {












    // supplier가 IHB인 데이터 중에 link의 마지막 "/" 뒤 부분만 모두 모아서 List<String> 반환
    @Query("SELECT SUBSTRING_INDEX(p.link, '/', -1) FROM Product p WHERE p.supplier.supplierCode = 'IHB'")
    List<String> findAllProdIdsByIherbFromSourceLink();

    @Query("SELECT SUBSTRING_INDEX(p.link, '/', -1) FROM Product p WHERE p.code = :code")
    String findIherbProductIdFromLinkByCode(String code);

    // ========== 엑셀 필터 추가

    /**
     * 필터 조건에 따른 상품 목록 조회 (페이징 포함)
     * - 키워드 검색: code, title, kor_name, eng_name, brandName
     * - 공급업체 필터: supplierCodes 리스트
     * - 채널 매핑 null 필터: vendorItemId, sellerProductId, smartstoreId, elevenstId
     */
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.supplier s " +
            "LEFT JOIN ProductChannelMapping pcm ON p.code = pcm.productCode " +
            "WHERE " +
            // 키워드 검색 조건
            "(:keyword IS NULL OR :keyword = '' OR " +
            " LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(p.korName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(p.engName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(p.brandName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            // 공급업체 필터 조건
            "AND (:#{#supplierCodes == null} = true OR s.supplierCode IN :supplierCodes) " +
            // 쿠팡 품목ID null 필터
            "AND (:filterNullVendorItemId = false OR pcm.vendorItemId IS NULL) " +
            // 쿠팡 상품ID null 필터
            "AND (:filterNullSellerProductId = false OR pcm.sellerProductId IS NULL) " +
            // 스마트스토어ID null 필터
            "AND (:filterNullSmartstoreId = false OR pcm.smartstoreId IS NULL) " +
            // 11번가ID null 필터
            "AND (:filterNullElevenstId = false OR pcm.elevenstId IS NULL) ")
    Page<Product> findProductsWithFilters(
            @Param("keyword") String keyword,
            @Param("supplierCodes") List<String> supplierCodes,
            @Param("filterNullVendorItemId") Boolean filterNullVendorItemId,
            @Param("filterNullSellerProductId") Boolean filterNullSellerProductId,
            @Param("filterNullSmartstoreId") Boolean filterNullSmartstoreId,
            @Param("filterNullElevenstId") Boolean filterNullElevenstId,
            Pageable pageable
    );

    /**
     * 공급업체별 상품 개수 조회 (필터 옵션용)
     */
    @Query("SELECT new com.jason.purchase_agent.dto.suppliers.SupplierDto(" +
            "s.supplierCode, s.supplierName, COUNT(p)) " +
            "FROM Product p " +
            "JOIN p.supplier s " +
            "GROUP BY s.supplierCode, s.supplierName " +
            "ORDER BY s.supplierCode")
    List<SupplierDto> findSuppliersWithProductCount();

    // 공급업체 ID 기준 상품 전체 조회
    List<Product> findBySupplier_SupplierCode(String supplierCode);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(p.code, 9, 3) AS int)), 0) FROM Product p WHERE p.code LIKE :prefix%")
    int findMaxSeqByCodePrefix(@Param("prefix") String prefix);
}