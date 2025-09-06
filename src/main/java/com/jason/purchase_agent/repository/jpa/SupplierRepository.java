package com.jason.purchase_agent.repository.jpa;

import com.jason.purchase_agent.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {
  Optional<Supplier> findBySupplierCode(String supplierCode);
}