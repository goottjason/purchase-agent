package com.jason.purchase_agent.repository;

import com.jason.purchase_agent.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    // currencyCode (PK) 기반
}