package com.jason.purchase_agent.repository.jpa;

import com.jason.purchase_agent.entity.SaleChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleChannelRepository extends JpaRepository<SaleChannel, String> {
  // channel_id(PK)가 기본키이기 때문에 Integer
}
