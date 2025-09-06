package com.jason.purchase_agent.dto.iherb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IherbProductDto {

  private String promoBanner;
  private List<List<CanonicalPath>> canonicalPaths;
  private String brandName;
  private String brandUrl;
  private String brandCode;
  private String brandManufacturerUrl;
  private String displayName;
  private List<String> flag;
  private Boolean show360;
  private Integer primaryImageIndex;
  private String partNumber;
  private List<Integer> imageIndices;
  private List<String> campaignImages;
  private List<Integer> enhanceImageIndices;
  private List<Integer> imageIndices360;
  private List<Object> videos;
  private String retailPrice;
  private String listPrice;
  private Double listPriceAmount;
  private String discountPrice;
  private String discountAmt;
  private Double discountPriceAmount;
  private String pricePerUnit;
  private String listPricePerUnit;
  private String discountPricePerUnit;
  private Map<String, String> listPricePerUnits;
  private Map<String, String> pricePerUnits;
  private Boolean isShippingSaver;
  private Integer stockStatus;
  private Integer stockStatusV2;
  private String stockStatusMessage;
  private Boolean hasBackInStockDate;
  private String backInStockDate;
  private String backInStockDateTime;
  private String formattedComingSoonDate;
  private String backInStockDateUnavailableMessage;
  private Boolean ltoos;
  private Object volumeDiscounts;
  private Integer loyaltyCreditPercent;
  private String expirationDate;
  private String formattedExpirationDate;
  private String weight;
  private String packageQuantity;
  private Map<String, String> packageQuantities;
  private String dimensions;
  private String actualWeight;
  private List<ProductRank> productRanks;
  private String description;
  private String ingredients;
  private String specialNote;
  private String suggestedUse;
  private String supplementFacts;
  private String warnings;
  private Boolean hidePrice;
  private Double averageRating;
  private Integer totalRatingCount;
  private Boolean isAvailableToPurchase;
  private String urlName;
  private Long id;
  private String url;
  private Boolean isProductBanned;
  private List<String> warehouses;
  private List<Object> productHighlights;
  private String disclaimer;
  private String countrySpecificDisclaimer;
  private Boolean showDisclaimer;
  private Boolean showUnifiedPricingMessage;
  private String unifiedPricingMessage;
  private Integer productStatus;
  private Boolean isDiscontinued;
  private Boolean isComingSoon;
  private Object productPageTemplate;
  private Integer qtyLimit;
  private Integer quantityLimit;
  private Integer autoShipQuantityLimit;
  private Object specialDealInfo;
  private Object trialDiscountInfo;
  private Integer discountType;
  private Integer discountDisplayType;
  private Boolean isInCartDiscount;
  private Double salesDiscountPercentage;
  private QnaInfo qna;
  private List<Double> ratingStarsMap;
  private String onSaleDate;
  private Integer rootCategoryId;
  private Integer l3CategoryId;
  private String rootCategoryName;
  private String enRootCategoryName;
  private String formattedOnSaleDate;
  private String weightLb;
  private String weightKg;
  private String dimensionsIn;
  private String dimensionsCm;
  private Boolean hasExpirationDate;
  private List<Object> manufacturerAddresses;
  private Boolean enabledDiscountBanner;
  private Boolean enabledDiscountForPreviouslyPurchased;
  private Boolean isNew;
  private List<Object> restrictedCountries;
  private String legalNotice;
  private String recentActivityMessage;
  private Integer recentActivityCount;
  private Boolean prohibited;
  private Boolean customerVisible;
  private Boolean inStockInExcludedWarehouses;
  private Boolean isWeightManagementProduct;
  private Promotion promotion;
  private Object attributeValuesInSpec;
  private Boolean isITested;

  @JsonProperty("iTestedCertificateUrl")
  private String iTestedCertificateUrl;
  
  private Integer groupId;
  private Boolean isExpressDelivery;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CanonicalPath {
    private String displayName;
    private String url;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductRank {
    private String categoryDisplayName;
    private String categoryUrl;
    private String rank;
    private Integer categoryId;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class QnaInfo {
    private Integer questionCount;
    private Integer answerCount;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Promotion {
    private Boolean promotionExcluded;
  }
}

