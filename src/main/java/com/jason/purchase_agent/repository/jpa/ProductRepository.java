package com.jason.purchase_agent.repository.jpa;

import com.jason.purchase_agent.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
  Page<Product> findByIsAvailableTrue(Pageable pageable);
  Page<Product> findBySupplierSupplierCodeAndIsAvailableTrue(String supplierCode, Pageable pageable);
  List<Product> findByEngNameContainingIgnoreCase(String engName);

  Page<Product> findByKorNameContainingOrEngNameContainingOrProductCodeContaining(
    String korName, String engName, String productCode, Pageable pageable
  );
}