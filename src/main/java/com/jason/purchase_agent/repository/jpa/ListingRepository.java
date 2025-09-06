package com.jason.purchase_agent.repository.jpa;

import com.jason.purchase_agent.entity.Currency;
import com.jason.purchase_agent.entity.Listing;
import com.jason.purchase_agent.enums.SaleChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
  Optional<Listing> findByChannelProductCodeAndSaleChannel(String channelProductCode, SaleChannel channel);
}
