package com.jason.purchase_agent.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartstoreProductDto {
  private long originProductNo;
  private List<ChannelProduct> channelProducts;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ChannelProduct {
    private String statusType;
    private String manufacturerName;
    private int exchangeFee;
    private int managerPurchasePoint;
    private String saleEndDate;
    private int stockQuantity;
    private String saleStartDate;
    private String regDate;
    private String wholeCategoryName;
    private String deliveryAttributeType;
    private RepresentativeImage representativeImage;
    private String sellerManagementCode;
    private long channelProductNo;
    private String brandName;
    private boolean knowledgeShoppingProductRegistration;
    private int salePrice;
    private int mobileDiscountedPrice;
    private String channelProductDisplayStatusType;
    private String channelServiceType;
    private long originProductNo;
    private int deliveryFee;
    private int discountedPrice;
    private int returnFee;
    private String name;
    private String modifiedDate;
    private String wholeCategoryId;
    private String categoryId;
  }


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RepresentativeImage {
    private String url;
  }
}
