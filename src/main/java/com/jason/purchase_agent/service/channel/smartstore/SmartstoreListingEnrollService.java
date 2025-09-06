package com.jason.purchase_agent.service.channel.smartstore;

import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreListingEnrollRequest;

public class SmartstoreListingEnrollService {
  public SmartstoreListingEnrollRequest createProductRequest(String productName, String categoryId, Integer price) {
    return SmartstoreListingEnrollRequest.builder()
      .originProduct(SmartstoreListingEnrollRequest.OriginProduct.builder()
        .name(productName)
        .leafCategoryId(categoryId)
        .salePrice(price)
        .stockQuantity(100)
        .detailAttribute(SmartstoreListingEnrollRequest.DetailAttribute.builder()
          .taxType("TAX")
          .build())
        .build())
      .smartstoreChannelProduct(SmartstoreListingEnrollRequest.SmartstoreChannelProduct.builder()
        .channelProductName(productName)
        .build())
      .build();
  }
}

