package com.jason.purchase_agent.repository.mybatis;

import com.jason.purchase_agent.dto.ProductDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
  List<ProductDetailDto> findProductsWithDetails(
    @Param("offset") int offset,
    @Param("limit") int limit,
    @Param("searchKeyword") String searchKeyword,
    @Param("supplierCode") String supplierCode
  );

  Long countProductsWithDetails(
    @Param("searchKeyword") String searchKeyword,
    @Param("supplierCode") String supplierCode
  );
}