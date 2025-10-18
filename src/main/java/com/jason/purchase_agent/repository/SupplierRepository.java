package com.jason.purchase_agent.repository;

import com.jason.purchase_agent.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {
    // findById(String code) 사용 가능
}