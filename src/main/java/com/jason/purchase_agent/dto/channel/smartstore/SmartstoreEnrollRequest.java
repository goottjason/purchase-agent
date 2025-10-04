package com.jason.purchase_agent.dto.channel.smartstore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// 네이버 상품등록 Full DTO (대표, 옵션, 인증, 공시, 혜택, 채널 등 포함)
@Data @Builder(toBuilder = true) @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmartstoreEnrollRequest {

    @NotNull private OriginProduct originProduct;
    @NotNull @Builder.Default private SmartstoreChannelProduct smartstoreChannelProduct = new SmartstoreChannelProduct();
    private WindowChannelProduct windowChannelProduct;

    // ---------------- OriginProduct(원상품) ----------------
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OriginProduct {
        @NotNull @Builder.Default private String statusType = "SALE";
        private String saleType;
        @NotNull private String leafCategoryId; // ● 리프 카테고리 ID
        @NotNull private String name; // ● 상품명
        @NotNull private String detailContent; // ● 상품 상세 정보(HTML)
        @NotNull private Images images; // 이미지 Object
        private String saleStartDate;
        private String saleEndDate;
        @NotNull private Integer salePrice; // ● 상품 판매 가격
        @NotNull private Integer stockQuantity; // ● 재고 수량
        @NotNull private DeliveryInfo deliveryInfo; // ● 배송 정보 Object
        private List<ProductLogistics> productLogistics;
        @NotNull private DetailAttribute detailAttribute; // ● 원상품 상세속성 Object
        private CustomerBenefit customerBenefit;
    }

    // ---------------- Images
    //                      > Image
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Images {
        @NotNull private Image representativeImage; // ● 대표이미지
        @NotNull private List<Image> optionalImages; // ● 상세이미지
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Image {
        @NotNull private String url;
    }

    // ---------------- DeliveryInfo
    //                      > DeliveryFee
    //                          > DeliveryFeeByArea
    //                      > ClaimDeliveryInfo
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryInfo {
        @NotNull @Builder.Default private String deliveryType = "DELIVERY";
        @NotNull @Builder.Default private String deliveryAttributeType ="NORMAL";
        @NotNull @Builder.Default private String deliveryCompany = "CJGLS";
        private String outboundLocationId;
        private Boolean deliveryBundleGroupUsable;
        private Integer deliveryBundleGroupId;
        private List<String> quickServiceAreas;
        private Integer visitAddressId;
        @NotNull @Builder.Default private DeliveryFee deliveryFee = new DeliveryFee();
        @NotNull @Builder.Default private ClaimDeliveryInfo claimDeliveryInfo = new ClaimDeliveryInfo();
        private Boolean installation;
        private Boolean installationFee;
        private String expectedDeliveryPeriodType;
        private String expectedDeliveryPeriodDirectInput;
        @NotNull @Builder.Default private Integer todayStockQuantity = 0;
        @NotNull @Builder.Default private Boolean customProductAfterOrderYn = false;
        private Integer hopeDeliveryGroupId;
        @NotNull @Builder.Default private Boolean businessCustomsClearanceSaleYn = true;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryFee {
        @NotNull @Builder.Default private String deliveryFeeType = "FREE";
        private Integer baseFee;
        private Integer freeConditionalAmount;
        private Integer repeatQuantity;
        private Integer secondBaseQuantity;
        private Integer secondExtraFee;
        private Integer thirdBaseQuantity;
        private Integer thirdExtraFee;
        private String deliveryFeePayType;
        private DeliveryFeeByArea deliveryFeeByArea;
        private String differentialFeeByArea;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryFeeByArea {
        private String deliveryAreaType;
        private Integer area2extraFee;
        private Integer area3extraFee;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ClaimDeliveryInfo {
        @NotNull @Builder.Default private String returnDeliveryCompanyPriorityType = "PRIMARY";
        @NotNull @Builder.Default private Integer returnDeliveryFee = 7000;
        @NotNull @Builder.Default private Integer exchangeDeliveryFee = 14000;
        @NotNull @Builder.Default private Integer shippingAddressId = 102265746;
        @NotNull @Builder.Default private Integer returnAddressId = 101123637;
        private Boolean freeReturnInsuranceYn = false;
    }

    // ---------------- ProductLogistics
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductLogistics {
        private String logisticsCompanyId;
    }

    // ---------------- DetailAttribute
    //                      > NaverShoppingSearchInfo
    //                      > AfterServiceInfo
    //                      > PurchaseQuantityInfo
    //                      > OriginAreaInfo
    //                      > SellerCodeInfo
    //                      > OptionInfo
    //                          > List<OptionSimple>
    //                          > List<OptionCustom>
    //                          > OptionCombinationGroupNames
    //                          > List<OptionCombination>
    //                          > List<StandardOptionGroup>
    //                              > List<StandardOptionAttribute>
    //                          > List<OptionStandard>
    //                      > SupplementProductInfo
    //                      > PurchaseReviewInfo
    //                      > IsbnInfo
    //                      > BookInfo
    //                          > Publisher
    //                          > List<Person>
    //                      > List<ProductCertificationInfo>
    //                      > CertificationTargetExcludeContent
    //                      > Ecoupon
    //                      > ProductInfoProvidedNotice
    //                          > Wear
    //                          > Shoes
    //                          > Food
    //                          > Cosmetic
    //                      > List<ProductAttribute>
    //                      > SeoInfo
    //                          > List<SellerTag>
    //                      > ProductSize
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DetailAttribute {
        private NaverShoppingSearchInfo naverShoppingSearchInfo;
        private String manufactureDefineNo;
        @NotNull @Builder.Default private AfterServiceInfo afterServiceInfo = new AfterServiceInfo();
        @NotNull @Builder.Default private PurchaseQuantityInfo purchaseQuantityInfo = new PurchaseQuantityInfo();
        @NotNull @Builder.Default private OriginAreaInfo originAreaInfo = new OriginAreaInfo();
        @NotNull private SellerCodeInfo sellerCodeInfo;
        private Boolean skuYn;
        @NotNull @Builder.Default private OptionInfo optionInfo = new OptionInfo();
        private SupplementProductInfo supplementProductInfo;
        private PurchaseReviewInfo purchaseReviewInfo;
        private IsbnInfo isbnInfo;
        private BookInfo bookInfo;
        @NotNull @Builder.Default private String eventPhraseCont = "100% 정품 보장";
        private String manufactureDate;
        private String releaseDate;
        private String validDate;
        private String taxType;
        // @NotNull @Builder.Default private CertificationTargetExcludeContent certificationTargetExcludeContent = new CertificationTargetExcludeContent();
        @NotNull @Builder.Default private String sellerCommentContent = "해외구매대행 특성상 배송 7~14일 소요";
        @NotNull @Builder.Default private Boolean sellerCommentUsable = true;
        @NotNull @Builder.Default private Boolean minorPurchasable = true;
        private Ecoupon ecoupon;
        @NotNull private ProductInfoProvidedNotice productInfoProvidedNotice;
        // 값ID/속성ID가 카테고리별로 완전히 다름 → 반드시 사전 조회 필요
        // @NotNull private List<ProductAttribute> productAttributes; // ● attribute 조회
        private Boolean cultureCostIncomeDeductionYn;
        private Boolean customProductYn;
        private Boolean itselfProductionProductYn;
        private Boolean brandCertificationYn;
        private SeoInfo seoInfo;
        private ProductSize productSize;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NaverShoppingSearchInfo {
        public Integer modelId;
        public String modelName;
        public String manufacturerName;
        public Integer brandId;
        public String brandName;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AfterServiceInfo {
        @NotNull @Builder.Default public String afterServiceTelephoneNumber = "010-2597-2480";
        @NotNull @Builder.Default public String afterServiceGuideContent = "상세페이지 참조";
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PurchaseQuantityInfo {
        public Integer minPurchaseQuantity;
        @NotNull @Builder.Default public Integer maxPurchaseQuantityPerId = 100;
        @NotNull @Builder.Default public Integer maxPurchaseQuantityPerOrder = 100;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OriginAreaInfo {
        @NotNull @Builder.Default public String originAreaCode = "03";
        public String importer;
        public String content;
        public Boolean plural;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SellerCodeInfo {
        @NotNull public String sellerManagementCode; // ● 판매자 관리코드
        public String sellerBarcode;
        public String sellerCustomCode1;
        public String sellerCustomCode2;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionInfo {
        private String simpleOptionSortType;
        @NotNull @Builder.Default private List<OptionSimple> optionSimple = new ArrayList<>();
        private List<OptionCustom> optionCustom;
        private String optionCombinationSortType;
        private OptionCombinationGroupNames optionCombinationGroupNames;
        private List<OptionCombination> optionCombinations;
        private List<StandardOptionGroup> standardOptionGroups;
        private List<OptionStandard> optionStandards;
        private Boolean useStockManagement;
        private List<String> optionDeliveryAttributes;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionSimple {
        private Integer id;
        private String groupName;
        private String name;
        private Boolean usable;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionCustom {
        private Integer id;
        private String groupName;
        private String name;
        private Boolean usable;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionCombinationGroupNames {
        private String optionGroupName1;
        private String optionGroupName2;
        private String optionGroupName3;
        private String optionGroupName4;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StandardOptionGroup {
        private String groupName;
        private List<StandardOptionAttribute> standardOptionAttributes;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StandardOptionAttribute {
        private Integer attributeId;
        private Integer attributeValueId;
        private String attributeValueName;
        private List<String> imageUrls;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionStandard {
        private Integer id;
        private Integer stockQuantity;
        private Boolean usable;
        private String optionName1;
        private String optionName2;
        private String sellerManagerCode;
        private Boolean skuYn;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SupplementProductInfo {
        private String sortType;
        private List<SupplementProduct> supplementProducts;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SupplementProduct {
        private Integer id;
        private String groupName;
        private String name;
        private Integer price;
        private Integer stockQuantity;
        private String sellerManagementCode;
        private Boolean usable;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PurchaseReviewInfo {
        private Boolean purchaseReviewExposure;
        private String reviewUnExposeReason;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IsbnInfo {
        private String isbn13;
        private String issn;
        private Boolean independentPublicationYn;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BookInfo {
        private String publishDay;
        private Publisher publisher;
        private List<Person> authors;
        private List<Person> illustrators;
        private List<Person> translators;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Publisher {
        private String code;
        private String text;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Person {
        private String code;
        private String text;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CertificationTargetExcludeContent {
        @NotNull @Builder.Default private Boolean childCertifiedProductExclusionYn = false;
        @NotNull @Builder.Default private String kcExemptionType = "OVERSEAS";
        @NotNull @Builder.Default private String kcCertifiedProductExclusionYn = "KC_EXEMPTION_OBJECT";
        @NotNull @Builder.Default private Boolean greenCertifiedProductExclusionYn = false;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductInfoProvidedNotice {
        @NotNull private String productInfoProvidedNoticeType; // ● "GeneralFood" or "DietFood" 세팅
        // private Food food;
        private GeneralFood generalFood;
        private DietFood dietFood;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Food {
        @NotNull @Builder.Default private String returnCostReason = "1";
        @NotNull @Builder.Default private String noRefundReason = "1";
        @NotNull @Builder.Default private String qualityAssuranceStandard = "1";
        @NotNull @Builder.Default private String compensationProcedure = "1";
        @NotNull @Builder.Default private String troubleShootingContents = "1";
        @NotNull @Builder.Default private String foodItem = "상세페이지 참조";
        @NotNull @Builder.Default private String weight = "상세페이지 참조";
        @NotNull @Builder.Default private String amount = "상세페이지 참조";
        @NotNull @Builder.Default private String size = "상세페이지 참조";
        // 제조일자·소비기한(직접입력), 생성/유통일자는 개별 가이드 따른다(필요시 null)
        private String packDate;
        @NotNull @Builder.Default private String packDateText = "상세페이지 참조";
        private String consumptionDate;
        @NotNull @Builder.Default private String consumptionDateText = "상세페이지 참조";
        @NotNull @Builder.Default private String producer = "상세페이지 참조";
        private String relevantLawContent;
        @NotNull @Builder.Default private String productComposition = "상세페이지 참조";
        @NotNull @Builder.Default private String keep = "상세페이지 참조";
        @NotNull @Builder.Default private String adCaution = "상세페이지 참조";
        @NotNull @Builder.Default private String customerServicePhoneNumber = "상세페이지 참조";
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GeneralFood {
        @NotNull @Builder.Default private String returnCostReason = "1"; // 상품상세 참조
        @NotNull @Builder.Default private String noRefundReason = "1"; // 상품상세 참조
        @NotNull @Builder.Default private String qualityAssuranceStandard = "1"; // 상품상세 참조
        @NotNull @Builder.Default private String compensationProcedure = "1"; // 상품상세 참조
        @NotNull @Builder.Default private String troubleShootingContents = "1"; // 상품상세 참조
        // 아래는 필수지만 상세참조 불가 (직접 실값 넣어야 함, 빈값/1 넣으면 안됨)
        @NotNull @Builder.Default private String productName = "상세페이지 참조";    // API에서는 실제 상품명을 넣어야 옳음, 완전 대체는 비권장
        @NotNull @Builder.Default private String foodType = "상세페이지 참조";
        @NotNull @Builder.Default private String producer = "상세페이지 참조";
        @NotNull @Builder.Default private String location = "상세페이지 참조";
        private String packDate;
        @NotNull @Builder.Default private String packDateText = "상세페이지 참조";
        private String consumptionDate;
        @NotNull @Builder.Default private String consumptionDateText = "상세페이지 참조";
        @NotNull @Builder.Default private String weight = "상세페이지 참조";
        @NotNull @Builder.Default private String amount = "상세페이지 참조";
        @NotNull @Builder.Default private String ingredients = "상세페이지 참조";
        @NotNull @Builder.Default private String nutritionFacts = "상세페이지 참조";

        @NotNull @Builder.Default private Boolean geneticallyModified = false;
        @NotNull @Builder.Default private String consumerSafetyCaution = "상세페이지 참조";
        @NotNull @Builder.Default private Boolean importDeclarationCheck = true;
        @NotNull @Builder.Default private String customerServicePhoneNumber = "상세페이지 참조";
        // 날짜/상세 등 기타 항목은 필요시 null/직접입력
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DietFood {
        @NotNull @Builder.Default private String returnCostReason = "1";
        @NotNull @Builder.Default private String noRefundReason = "1";
        @NotNull @Builder.Default private String qualityAssuranceStandard = "1";
        @NotNull @Builder.Default private String compensationProcedure = "1";
        @NotNull @Builder.Default private String troubleShootingContents = "1";
        @NotNull @Builder.Default private String productName = "상세페이지 참조";
        @NotNull @Builder.Default private String producer = "상세페이지 참조";
        @NotNull @Builder.Default private String location = "상세페이지 참조";
        private String consumptionDate;
        @NotNull @Builder.Default private String consumptionDateText = "상세페이지 참조";
        @NotNull @Builder.Default private String storageMethod = "상세페이지 참조";
        @NotNull @Builder.Default private String weight = "상세페이지 참조";
        @NotNull @Builder.Default private String amount = "상세페이지 참조";
        @NotNull @Builder.Default private String ingredients = "상세페이지 참조";
        @NotNull @Builder.Default private String nutritionFacts = "상세페이지 참조";
        @NotNull @Builder.Default private String specification = "상세페이지 참조";
        @NotNull @Builder.Default private String cautionAndSideEffect = "상세페이지 참조";
        @NotNull @Builder.Default private String nonMedicinalUsesMessage = "상세페이지 참조";
        @NotNull @Builder.Default private Boolean geneticallyModified = false;
        @NotNull @Builder.Default private Boolean importDeclarationCheck = true;
        @NotNull @Builder.Default private String consumerSafetyCaution = "상세페이지 참조";
        @NotNull @Builder.Default private String customerServicePhoneNumber = "상세페이지 참조";
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductAttribute {
        private Integer attributeSeq;
        private Integer attributeValueSeq;
        private String attributeRealValue;
        private String attributeRealValueUnitCode;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SeoInfo {
        private String pageTitle;
        private String metaDescription;
        private List<SellerTag> sellerTags;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SellerTag {
        private Integer code;
        private String text;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductSize {
        private Integer sizeTypeNo;
        private List<SmartstoreProductRequest.SizeAttribute> sizeAttributes;
        private List<SmartstoreProductRequest.Model> models;
    }

    // =================================================================================== /

    // ---------------- CustomerBenefit
    //                      > ImmediateDiscountPolicy
    //                          > DiscountMethod
    //                      > PurchasePointPolicy
    //                      > ReviewPointPolicy
    //                      > FreeInterestPolicy
    //                      > GiftPolicy
    //                      > MultiPurchaseDiscountPolicy
    //                          > DiscountMethod
    //                      > ReservedDiscountPolicy
    //                          > DiscountMethod
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomerBenefit {
        private ImmediateDiscountPolicy immediateDiscountPolicy;
        private PurchasePointPolicy purchasePointPolicy;
        private ReviewPointPolicy reviewPointPolicy;
        private FreeInterestPolicy freeInterestPolicy;
        private GiftPolicy giftPolicy;
        private MultiPurchaseDiscountPolicy multiPurchaseDiscountPolicy;
        private ReservedDiscountPolicy reservedDiscountPolicy;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImmediateDiscountPolicy {
        private DiscountMethod discountMethod;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DiscountMethod {
        private Integer value;
        private String unitType; // 예: "PERCENT"
        private String startDate;
        private String endDate;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PurchasePointPolicy {
        private Integer value;
        private String unitType;
        private String startDate;
        private String endDate;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReviewPointPolicy {
        private Integer textReviewPoint;
        private Integer photoVideoReviewPoint;
        private Integer afterUseTextReviewPoint;
        private Integer afterUsePhotoVideoReviewPoint;
        private Integer storeMemberReviewPoint;
        private String startDate;
        private String endDate;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FreeInterestPolicy {
        private Integer value;
        private String startDate;
        private String endDate;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GiftPolicy {
        private String presentContent;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MultiPurchaseDiscountPolicy {
        private DiscountMethod discountMethod;
        private Integer orderValue;
        private String orderValueUnitType;
    }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReservedDiscountPolicy {
        private DiscountMethod discountMethod;
    }

    // ---------------- SmartstoreChannelProduct ----------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SmartstoreChannelProduct {
        private String channelProductName;
        private Integer bbsSeq;
        private Boolean storeKeepExclusiveProduct;
        @NotNull private Boolean naverShoppingRegistration = false;
        @NotNull private String channelProductDisplayStatusType = "ON";
    }

    // ---------------- WindowChannelProduct ----------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WindowChannelProduct {
        private String channelProductName;
        private Integer bbsSeq;
        private Boolean storeKeepExclusiveProduct;
        @NotNull private Boolean naverShoppingRegistration;
        @NotNull private Integer channelNo;
        private Boolean best;
    }

    // ---------------------------
    // == 빌더형 팩토리 메서드 ==
    // ---------------------------
    public static SmartstoreEnrollRequest from(ProductRegistrationDto dto) {

        List<String> imageLinks = dto.getSmartstoreImageLinks();
        Images images = Images.builder()
                .representativeImage(
                        Image.builder().url(imageLinks.getFirst()).build()
                )
                .optionalImages(
                        imageLinks.size() > 1
                                ? imageLinks.subList(1, imageLinks.size()).stream()
                                // 이미지 URL을 하나씩 Image 객체로 변환(mapping)
                                .map(url -> Image.builder().url(url).build())
                                // 리스트(List)로 다시 모으기
                                .collect(Collectors.toList())
                                : new ArrayList<>() // 상세이미지 없을 때 빈 리스트
                )
                .build();

        String productType = dto.getProductType();
        String noticeType = null;

        if ("FOOD".equals(productType)) {
            noticeType = "GENERAL_FOOD";
        } else if ("HEALTH".equals(productType)) {
            noticeType = "DIET_FOOD";
        }

        ProductInfoProvidedNotice.ProductInfoProvidedNoticeBuilder noticeBuilder =
                ProductInfoProvidedNotice.builder()
                        .productInfoProvidedNoticeType(noticeType);

        // 필드 분기 처리
        if ("DIET_FOOD".equals(noticeType)) {
            noticeBuilder.dietFood(
                    DietFood.builder().build()
            );
        } else if ("GENERAL_FOOD".equals(noticeType)) {
            noticeBuilder.generalFood(
                    GeneralFood.builder().build()
            );
        }

        // builder 완성
        ProductInfoProvidedNotice productInfoProvidedNotice = noticeBuilder.build();


        DetailAttribute detailAttribute = DetailAttribute.builder()
                .sellerCodeInfo(
                        SellerCodeInfo.builder()
                                .sellerManagementCode(dto.getCode())
                                .build()
                )
                .productInfoProvidedNotice(productInfoProvidedNotice)
                // .productAttributes(null) // ● 골치 아픈 느낌... (일단 스킵)
                .build();

        String detailsHtml = dto.getDetailsHtml();
        detailsHtml = detailsHtml.replaceAll("\\\\\"", "\"")
                .replaceAll("\n", "")
                .trim();
        OriginProduct originProduct =
                OriginProduct.builder()
                        .leafCategoryId(String.valueOf(dto.getSmartstoreCategoryId()))
                        .name(dto.getTitle())
                        .detailContent(detailsHtml)
                        .images(images)
                        .salePrice(dto.getSalePrice())
                        .stockQuantity(dto.getStock())
                        .deliveryInfo(DeliveryInfo.builder().build())
                        .detailAttribute(detailAttribute)
                        .build();

        SmartstoreEnrollRequest request = SmartstoreEnrollRequest.builder()
                .originProduct(originProduct)
                .build();

        return request;
    }
}












