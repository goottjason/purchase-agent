package com.jason.purchase_agent.dto.channel.smartstore;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 네이버 커머스 상품 등록 DTO (완전 버전)
 * 공식문서 https://apicenter.commerce.naver.com/docs/commerce-api/current/create-product-product 기반
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmartstoreListingEnrollRequest {

  /**
   * 상품 공통 속성. (REQUIRED)
   */
  private OriginProduct originProduct;

  /**
   * 스마트스토어 채널 상품 정보 (REQUIRED)
   */
  private SmartstoreChannelProduct smartstoreChannelProduct;

  /**
   * 쇼핑윈도 채널 상품 정보 (윈도 노출 필요시)
   */
  private WindowChannelProduct windowChannelProduct;


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OriginProduct {
    /** 판매상태 (REQUIRED) WAIT, SALE, OUTOFSTOCK 등 ENUM */
    private String statusType;
    /** 판매유형 NEW(기본), OLD(중고) 등 ENUM */
    private String saleType;
    /** 리프 카테고리ID (REQUIRED 상품등록시 필수) */
    private String leafCategoryId;
    /** 상품명 (REQUIRED) */
    private String name;
    /** 상세 설명 (REQUIRED, 수정시만 생략 가능) */
    private String detailContent;
    /** 이미지 (객체, 필수) */
    private Images images;
    /** 판매 시작일시(00분단위) yyyy-MM-dd'T'HH:mm[:ss][.SSS]XXX */
    private String saleStartDate;
    /** 판매 종료일시(59분단위) yyyy-MM-dd'T'HH:mm[:ss][.SSS]XXX */
    private String saleEndDate;
    /** 판매 가격 (REQUIRED, 0~999999990) */
    private Integer salePrice;
    /** 재고 수량 (REQUIRED, 0~99999999) */
    private Integer stockQuantity;

    // 배송 등 부가정보
    /** 배송 정보 */
    private DeliveryInfo deliveryInfo;
    /** 물류사 정보 목록 */
    private List<ProductLogistics> productLogistics;
    /** 원상품 상세 속성 (REQUIRED) */
    private DetailAttribute detailAttribute;
    /** 상품 고객혜택 정보 */
    private CustomerBenefit customerBenefit;
  }

  // ==================== IMAGES ====================
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Images {
    /** 대표 이미지(필수) */
    private RepresentativeImage representativeImage;
    /** 추가 이미지 목록(최대 9개) */
    private List<OptionalImage> optionalImages;
  }

  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class RepresentativeImage {
    /** 이미지 url (필수) */
    private String url;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OptionalImage {
    /** 이미지 url (필수) */
    private String url;
  }

  // ==================== DELIVERY ====================
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class DeliveryInfo {
    /** 배송방법 타입(필수) [DELIVERY(택배), DIRECT(직접배송)] */
    private String deliveryType;
    /** 배송 속성 타입(필수) [NORMAL, TODAY, HOPE 등 제한 ENUM] */
    private String deliveryAttributeType;
    /** 택배사 (DELIVERY 일 때 필수) */
    private String deliveryCompany;
    /** 판매자 창고ID (SELLER_GUARANTEE, HOPE_SELLER_GUARANTEE시 필수) */
    private String outboundLocationId;
    /** 묶음배송 가능 */
    private Boolean deliveryBundleGroupUsable;
    /** 묶음배송 그룹 코드 */
    private Integer deliveryBundleGroupId;
    /** 퀵서비스 배송 지역 코드(SEOUL 등 ENUM 다수) */
    private List<String> quickServiceAreas;
    /** 방문수령 주소록ID */
    private Integer visitAddressId;
    /** 배송비 정보(필수) */
    private DeliveryFee deliveryFee;
    /** 클레임(반품/교환) 정보(필수) */
    private ClaimDeliveryInfo claimDeliveryInfo;
    /** 설치여부 (희망일배송일 때만 필수) */
    private Boolean installation;
    /** 설치비여부 */
    private Boolean installationFee;
    /** 주문제작 발송예정타입 [ETC, TWO, THREE, ..., FOURTEEN] ENUM */
    private String expectedDeliveryPeriodType;
    /** 발송예정 직접입력 값 */
    private String expectedDeliveryPeriodDirectInput;
    /** 오늘출발 상품 재고수량 */
    private Integer todayStockQuantity;
    /** 주문 후 제작상품 여부 */
    private Boolean customProductAfterOrderYn;
    /** 희망일배송 그룹번호 */
    private Integer hopeDeliveryGroupId;
    /** 사업자통관판매여부 */
    private Boolean businessCustomsClearanceSaleYn;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class DeliveryFee {
    /** 배송비 타입 [FREE, PAID 등] */
    private String deliveryFeeType;
    private Integer baseFee;
    private Integer freeConditionalAmount;
    private Integer repeatQuantity;
    private Integer secondBaseQuantity;
    private Integer secondExtraFee;
    private Integer thirdBaseQuantity;
    private Integer thirdExtraFee;
    /** COLLECT(착불) 등 */
    private String deliveryFeePayType;
    /** 지역별 배송비 정보 */
    private DeliveryFeeByArea deliveryFeeByArea;
    /** 지역별 차등배송비 */
    private String differentialFeeByArea;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class DeliveryFeeByArea {
    /** 지역타입 (예: AREA_2) */
    private String deliveryAreaType;
    private Integer area2extraFee;
    private Integer area3extraFee;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ClaimDeliveryInfo {
    /** 반품 택배사 우선순위(PRIMARY) */
    private String returnDeliveryCompanyPriorityType;
    /** 반품 배송비 */
    private Integer returnDeliveryFee;
    /** 교환 배송비 */
    private Integer exchangeDeliveryFee;
    /** 교환/반품 주소록ID */
    private Integer shippingAddressId;
    private Integer returnAddressId;
    /** 무료반품보장여부 */
    private Boolean freeReturnInsuranceYn;
  }

  // ==================== 물류회사 정보 ====================
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductLogistics {
    /** 물류사id */
    private String logisticsCompanyId;
  }

  // ==================== 상세 속성 ====================
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class DetailAttribute {
    private NaverShoppingSearchInfo naverShoppingSearchInfo;
    private String manufactureDefineNo;
    private AfterServiceInfo afterServiceInfo;
    private PurchaseQuantityInfo purchaseQuantityInfo;
    private OriginAreaInfo originAreaInfo;
    private SellerCodeInfo sellerCodeInfo;
    private Boolean skuYn;
    private OptionInfo optionInfo;
    private SupplementProductInfo supplementProductInfo;
    private PurchaseReviewInfo purchaseReviewInfo;
    private IsbnInfo isbnInfo;
    private BookInfo bookInfo;
    private String eventPhraseCont;
    private String manufactureDate;
    private String releaseDate;
    private String validDate;
    private String taxType;
    private List<ProductCertificationInfo> productCertificationInfos;
    private CertificationTargetExcludeContent certificationTargetExcludeContent;
    private String sellerCommentContent;
    private Boolean sellerCommentUsable;
    private Boolean minorPurchasable;
    private Ecoupon ecoupon;
//    private ProductInfoProvidedNotice productInfoProvidedNotice;
    private List<ProductAttribute> productAttributes;
    private Boolean cultureCostIncomeDeductionYn;
    private Boolean customProductYn;
    private Boolean itselfProductionProductYn;
    private Boolean brandCertificationYn;
    private SeoInfo seoInfo;
    private ProductSize productSize;
  }

  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class NaverShoppingSearchInfo {
    private Integer modelId;
    private String modelName;
    private String manufacturerName;
    private Integer brandId;
    private String brandName;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class AfterServiceInfo {
    private String afterServiceTelephoneNumber;
    private String afterServiceGuideContent;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class PurchaseQuantityInfo {
    private Integer minPurchaseQuantity;
    private Integer maxPurchaseQuantityPerId;
    private Integer maxPurchaseQuantityPerOrder;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OriginAreaInfo {
    private String originAreaCode;
    private String importer;
    private String content;
    private Boolean plural;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SellerCodeInfo {
    private String sellerManagementCode;
    private String sellerBarcode;
    private String sellerCustomCode1;
    private String sellerCustomCode2;
  }
  // 옵션
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OptionInfo {
    private String simpleOptionSortType;
    private List<OptionSimple> optionSimple;
    private List<OptionCustom> optionCustom;
    private String optionCombinationSortType;
    private OptionCombinationGroupNames optionCombinationGroupNames;
    private List<OptionCombination> optionCombinations;
    private List<StandardOptionGroup> standardOptionGroups;
    private List<OptionStandard> optionStandards;
    private Boolean useStockManagement;
    private List<String> optionDeliveryAttributes;
  }
  // 옵션 하위 항목들
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OptionSimple {
    private Integer id;
    private String groupName;
    private String name;
    private Boolean usable;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OptionCustom {
    private Integer id;
    private String groupName;
    private String name;
    private Boolean usable;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OptionCombinationGroupNames {
    private String optionGroupName1;
    private String optionGroupName2;
    private String optionGroupName3;
    private String optionGroupName4;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OptionCombination {
    private Integer id;
    private Integer stockQuantity;
    private Integer price;
    private Boolean usable;
    private String optionName1;
    private String optionName2;
    private String optionName3;
    private String optionName4;
    private String sellerManagerCode;
    private Boolean skuYn;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class StandardOptionGroup {
    private String groupName;
    private List<StandardOptionAttribute> standardOptionAttributes;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class StandardOptionAttribute {
    private Integer attributeId;
    private Integer attributeValueId;
    private String attributeValueName;
    private List<String> imageUrls;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OptionStandard {
    private Integer id;
    private Integer stockQuantity;
    private Boolean usable;
    private String optionName1;
    private String optionName2;
    private String sellerManagerCode;
    private Boolean skuYn;
  }

  // 보조상품
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SupplementProductInfo {
    private String sortType;
    private List<SupplementProduct> supplementProducts;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SupplementProduct {
    private Integer id;
    private String groupName;
    private String name;
    private Integer price;
    private Integer stockQuantity;
    private String sellerManagementCode;
    private Boolean usable;
  }

  // 리뷰 노출관련
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class PurchaseReviewInfo {
    private Boolean purchaseReviewExposure;
    private String reviewUnExposeReason;
  }
  // ISBN
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class IsbnInfo {
    private String isbn13;
    private String issn;
    private Boolean independentPublicationYn;
  }
  // 도서정보
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class BookInfo {
    private String publishDay;
    private Publisher publisher;
    private List<Person> authors;
    private List<Person> illustrators;
    private List<Person> translators;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Publisher {
    private String code;
    private String text;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Person {
    private String code;
    private String text;
  }

  // 인증
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductCertificationInfo {
    private Integer certificationInfoId;
    private String certificationKindType;
    private String name;
    private String certificationNumber;
    private Boolean certificationMark;
    private String companyName;
    private String certificationDate;
  }
  // 인증 예외
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CertificationTargetExcludeContent {
    private Boolean childCertifiedProductExclusionYn;
    private String kcExemptionType;
    private String kcCertifiedProductExclusionYn;
    private Boolean greenCertifiedProductExclusionYn;
  }
  // 에디터 판매자 코멘트
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Ecoupon {
    private String periodType;
    private String validStartDate;
    private String validEndDate;
    private Integer periodDays;
    private String publicInformationContents;
    private String contactInformationContents;
    private String usePlaceType;
    private String usePlaceContents;
    private Boolean restrictCart;
    private String siteName;
  }
  // 상품 정보 제공 고시
  /*@Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductInfoProvidedNotice {
    *//*private String productInfoProvidedNoticeType;
    private Wear wear;
    private Shoes shoes;
    private Bag bag;
    private FashionItems fashionItems;
    private SleepingGear sleepingGear;
    private Furniture furniture;
    private ImageAppliances imageAppliances;
    private HomeAppliances homeAppliances;
    private SeasonAppliances seasonAppliances;
    private OfficeAppliances officeAppliances;
    private OpticsAppliances opticsAppliances;
    private MicroElectronics microElectronics;
    private Navigation navigation;
    private CarArticles carArticles;
    private MedicalAppliances medicalAppliances;
    private KitchenUtensils kitchenUtensils;
    private Cosmetic cosmetic;
    private Jewellery jewellery;
    private Food food;
    private GeneralFood generalFood;
    private DietFood dietFood;
    private Kids kids;
    private MusicalInstrument musicalInstrument;
    private SportsEquipment sportsEquipment;
    private Books books;
    private RentalEtc rentalEtc;
    private RentalHa rentalHa;
    private DigitalContents digitalContents;
    private GiftCard giftCard;
    private MobileCoupon mobileCoupon;
    private MovieShow movieShow;
    private EtcService etcService;
    private Biochemistry biochemistry;
    private Biocidal biocidal;
    private CellPhone cellPhone;
    private Etc etc;*//*
  }*/

  /*@Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Wear { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Shoes { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Bag { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class FashionItems { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SleepingGear { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Furniture { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ImageAppliances { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class HomeAppliances { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SeasonAppliances { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OfficeAppliances { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OpticsAppliances { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MicroElectronics { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Navigation { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CarArticles { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MedicalAppliances { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class KitchenUtensils { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Cosmetic { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Jewellery { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Food { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class GeneralFood { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class DietFood { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Kids { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MusicalInstrument { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SportsEquipment { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Books { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class RentalEtc { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class RentalHa { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class DigitalContents { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class GiftCard { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MobileCoupon { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MovieShow { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class EtcService { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Biochemistry { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Biocidal { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CellPhone { *//* 구현 필요 *//* }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Etc { *//* 구현 필요 *//* }*/

  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductAttribute {
    private Integer attributeSeq;
    private Integer attributeValueSeq;
    private String attributeRealValue;
    private String attributeRealValueUnitCode;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SeoInfo {
    private String pageTitle;
    private String metaDescription;
    private List<SellerTag> sellerTags;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SellerTag {
    private Integer code;
    private String text;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductSize {
    private Integer sizeTypeNo;
    private List<SizeAttribute> sizeAttributes;
    private List<Model> models;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SizeAttribute {
    private String name;
    private List<SizeValue> sizeValues;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SizeValue {
    private Integer sizeValueTypeNo;
    private Integer value;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Model {
    private Integer modelId;
  }

  // ==================== 혜택 ====================
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CustomerBenefit {
    private ImmediateDiscountPolicy immediateDiscountPolicy;
    private PurchasePointPolicy purchasePointPolicy;
    private ReviewPointPolicy reviewPointPolicy;
    private FreeInterestPolicy freeInterestPolicy;
    private GiftPolicy giftPolicy;
    private MultiPurchaseDiscountPolicy multiPurchaseDiscountPolicy;
    private ReservedDiscountPolicy reservedDiscountPolicy;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ImmediateDiscountPolicy {
    private DiscountMethod discountMethod;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class PurchasePointPolicy {
    private Integer value;
    private String unitType;
    private String startDate;
    private String endDate;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ReviewPointPolicy {
    private Integer textReviewPoint;
    private Integer photoVideoReviewPoint;
    private Integer afterUseTextReviewPoint;
    private Integer afterUsePhotoVideoReviewPoint;
    private Integer storeMemberReviewPoint;
    private String startDate;
    private String endDate;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class FreeInterestPolicy {
    private Integer value;
    private String startDate;
    private String endDate;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class GiftPolicy {
    private String presentContent;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MultiPurchaseDiscountPolicy {
    private DiscountMethod discountMethod;
    private Integer orderValue;
    private String orderValueUnitType;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ReservedDiscountPolicy {
    private DiscountMethod discountMethod;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class DiscountMethod {
    private Integer value;
    private String unitType;
    private String startDate;
    private String endDate;
  }

  // SMARTSTORE/WINDOW
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SmartstoreChannelProduct {
    /** 채널 상품 전용 상품명 */
    private String channelProductName;
    /** 콘텐츠 게시글 일련번호 */
    private Integer bbsSeq;
    /** 알림받기 동의 회원 전용 상품여부 */
    private Boolean storeKeepExclusiveProduct;
    /** 네이버 쇼핑 등록여부 (필수) */
    private Boolean naverShoppingRegistration;
    /** 전시상태 코드 (필수/WAIT,ON,SUSPENSION) */
    private String channelProductDisplayStatusType;
  }
  @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class WindowChannelProduct {
    private String channelProductName;
    private Integer bbsSeq;
    private Boolean storeKeepExclusiveProduct;
    private Boolean naverShoppingRegistration;
    /** 윈도노출시 필수 */
    private Integer channelNo;
    /** 베스트 여부(기본 false) */
    private Boolean best;
  }
}
