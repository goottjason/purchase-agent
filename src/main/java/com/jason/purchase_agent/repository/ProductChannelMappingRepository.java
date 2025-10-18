package com.jason.purchase_agent.repository;

import com.jason.purchase_agent.entity.ProductChannelMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductChannelMappingRepository extends JpaRepository<ProductChannelMapping, String> {
    /**
     * 상품코드 리스트로 채널매핑 정보 조회
     */
    // @Query("SELECT pcm FROM ProductChannelMapping pcm WHERE pcm.productCode IN :codes")
    List<ProductChannelMapping> findByProductCodeIn(@Param("codes") List<String> codes);

    /**
     * 상품코드로 채널매핑 정보 조회
     */
    Optional<ProductChannelMapping> findByProductCode(String productCode);


    List<ProductChannelMapping> findByVendorItemIdIsNotNull();

    List<ProductChannelMapping> findBySmartstoreIdIsNotNull();

    List<ProductChannelMapping> findByElevenstIdIsNotNull();
    @Query("SELECT pcm FROM ProductChannelMapping pcm WHERE pcm.productCode = :productCode")
    Optional<ProductChannelMapping> findMappingByProductCode(@Param("productCode") String productCode);

    void deleteByProductCode(String productCode);
}
