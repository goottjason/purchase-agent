package com.jason.purchase_agent.repository.jpa;

import com.jason.purchase_agent.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
  // currency_code(PK)가 기본키이기 때문에 String
}