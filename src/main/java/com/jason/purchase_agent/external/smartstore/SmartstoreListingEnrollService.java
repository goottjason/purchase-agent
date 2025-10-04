package com.jason.purchase_agent.external.smartstore;

import com.jason.purchase_agent.dto.channel.smartstore.SmartstoreProductRequest;

public class SmartstoreListingEnrollService {
    public SmartstoreProductRequest createProductRequest(String productName, String categoryId, Integer price) {
        return SmartstoreProductRequest.builder()
                .originProduct(SmartstoreProductRequest.OriginProduct.builder()
                        .name(productName)
                        .leafCategoryId(categoryId)
                        .salePrice(price)
                        .stockQuantity(100)
                        .detailAttribute(SmartstoreProductRequest.DetailAttribute.builder()
                                .taxType("TAX")
                                .build())
                        .build())
                .smartstoreChannelProduct(SmartstoreProductRequest.SmartstoreChannelProduct.builder()
                        .channelProductName(productName)
                        .build())
                .build();
    }
}

