package com.jason.purchase_agent.dto.channel.cafe;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import com.jason.purchase_agent.dto.products.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

// 카페24 상품등록 DTO (필드별 한글 주석/필수/Default 표시)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CafeProductRequest {

    @NotNull @Builder.Default
    private Integer shopNo = 1; // ◎쇼핑몰번호 (Required)
    @NotNull
    private RequestBody request; // ●상품등록 본문 (Required)

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequestBody {
        @NotNull @Builder.Default
        private String display = "T"; // ◎상품 진열 [진열여부] (Required, Default:T)
        @NotNull @Builder.Default
        private String selling = "T"; // ◎상품 판매상태 [판매여부] (Required, Default:T)
        @NotNull @Builder.Default
        private String productCondition = "N"; // ◎[상품상태] (새상품:N, 중고:U 등)
        private Integer productUsedMonth; // ○[중고사용개월]
        @NotNull @Builder.Default
        private List<AddCategoryNo> addCategoryNo = new ArrayList<>();; // ◎[추가카테고리]
        @NotNull
        private String customProductCode; // ●[사용자상품코드]
        @NotNull
        private String productName; // ●[상품명] (Required)
        private String engProductName; // ●[영문상품명]
        private String supplyProductName; // ○[공급사상품명]
        @NotNull
        private String internalProductName; // ●[내부상품명] = 상품명
        private String modelName; // ○[모델명]
        @NotNull
        private String price; // ●[판매가] (Required)
        private String retailPrice; // ○[소비자가]
        @NotNull
        private String supplyPrice; // ●[공급가] salePrice의 0.8로 참조만 할 뿐 (100원단위)
        @NotNull @Builder.Default
        private String hasOption = "F"; // ◎[옵션사용여부] (Required)
        private String soldoutMessage; // ○[품절시메세지]
        private List<Option> options; // ○[옵션정보]
        private String useNaverpay; // ○[네이버페이 사용여부] (T)
        private String naverpayType; // ○[네이버페이타입] (C)
        private String useKakaopay; // ○[카카오페이 사용여부] (T)
        @NotNull @Builder.Default
        private String imageUploadType = "A";             // [이미지업로드타입] (A)
        private String detailImage;                 // ●[상세이미지(대표)]
        private String manufacturerCode; // ○[제조사코드]
        private String supplierCode; // ○[공급사코드]
        private String brandCode; // ○[브랜드코드]
        private String trendCode; // ○[트렌드코드]
        private String productWeight; // ○[상품무게]
        private ExpirationDate expirationDate; // ○[유효기간]
        private List<String> icon; // ○[아이콘리스트]
        private String priceContent; // ○[가격정책설명]
        private String buyLimitByProduct; // ○[상품별구매제한 지정여부]
        private String buyLimitType; // ○[구매제한유형]
        private List<Integer> buyGroupList; // ○[구매가능회원등급리스트]
        private List<String> buyMemberIdList; // ○[구매가능회원ID리스트]
        private String repurchaseRestriction; // ○[재구매제한여부]
        private String singlePurchaseRestriction; // ○[1회구매제한여부]
        private String singlePurchase; // ○[1회구매여부]
        private String buyUnitType; // ○[구매단위유형]
        private Integer buyUnit; // ○[구매단위]
        private String orderQuantityLimitType; // ○[주문수량제한 유형]
        private Integer minimumQuantity; // ○[최소구매수량]
        private Integer maximumQuantity; // ○[최대구매수량]
        private String pointsByProduct; // ○[상품별 적립금 설정여부]
        private String pointsSettingByPayment; // ○[결제별 적립금설정]
        private List<PointsAmount> pointsAmount; // ○[적립금설정리스트]
        private String exceptMemberPoints; // ○[적립금제외회원 설정]
        private ProductVolume productVolume; // ○[상품부피정보]
        @NotNull
        private String description; // ●[상세설명]
        private String mobileDescription; // ○[모바일설명]
        private String translatedDescription; // ○[영문설명]
        private String summaryDescription; // ○[요약설명]
        private String simpleDescription; // ○[간단설명]
        private String paymentInfo; // ○[결제안내]
        private String shippingInfo; // ○[배송안내]
        private String exchangeInfo; // ○[교환안내]
        private String serviceInfo; // ○[서비스안내]
        private String hscode; // ○[HS코드]
        private CountryHscode countryHscode; // ○[국가별HS코드]
        private List<RelationalProduct> relationalProduct; // ○[연관상품리스트]
        @NotNull @Builder.Default
        private String shippingScope = "A"; // ◎[배송범위]
        @NotNull @Builder.Default
        private String shippingFeeByProduct = "T"; // ◎[상품별배송비적용여부]
        @NotNull @Builder.Default
        private String shippingMethod = "01"; // ◎[배송방법]
        private ShippingPeriod shippingPeriod; // ○[배송기간]
        private String shippingArea; // ○[배송지역]
        @NotNull @Builder.Default
        private String shippingFeeType = "T"; // ◎[배송비유형]
        private String clearanceCategoryCode; // ○[통관분류코드]
        @NotNull @Builder.Default
        private String productShippingType = "C"; // ○[상품배송구분]
        private List<ShippingRates> shippingRates; // ○[상품별배송비리스트]
        private String productMaterial; // ○[재질]
        private String translateProductMaterial; // ○[재질영문번역]
        private String englishProductMaterial; // ○[영문재질]
        private String clothFabric; // ○[원단정보]
        private String classificationCode; // ○[상품분류코드]
        private String additionalPrice; // ○[추가금액]
        private String marginRate; // ○[마진율]
        @NotNull @Builder.Default
        private String taxType = "A"; // ◎[과세/면세 유형]
        private Integer taxRate; // ○[세율]
        @NotNull @Builder.Default
        private String prepaidShippingFee = "P"; // ◎[선결제배송비]
        @NotNull @Builder.Default
        private String originClassification = "E"; // ◎[원산지구분]
        private Integer originPlaceNo; // ○[원산지노출번호]
        @NotNull @Builder.Default
        private String originPlaceValue = "기타"; // ◎[원산지기타정보]
        private String madeInCode; // ○[제조국코드]


        @NotNull
        private List<String> additionalImage;       // ●[추가이미지리스트]
        // private String exposureLimitType;           // [노출제한유형]
        // private List<Integer> exposureGroupList;    // [노출회원등급리스트]
        @NotNull @Builder.Default
        private String culturalTaxDeduction = "F";        // [문화세공제여부]
        // private SizeGuide sizeGuide;                // [사이즈가이드]
    }

    // 이하 하위 구조체도 한글 주석 및 (Required/Default) 표시해서 작성
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddCategoryNo {
        @NotNull @Builder.Default
        private Integer categoryNo = 27; // ◎[카테고리번호] (Required)
        private String recommend;      // [추천여부] (Default:F)
        private String newItem;        // [신상품여부] (Default:T)
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Option {
        private String name;           // [옵션명] (Required)
        private List<String> value;    // [옵션값리스트] (Required)
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExpirationDate {
        private String startDate;      // [시작일]
        private String endDate;        // [종료일]
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PointsAmount {
        private String paymentMethod;         // [결제방법] (Required)
        private String pointsRate;            // [적립률]
        private String pointsUnitByPayment;   // [적립단위]
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductVolume {
        private String useProductVolume;      // [부피사용여부] (Default:T)
        private Double productWidth;          // [가로]
        private Double productHeight;         // [세로]
        private Double productLength;         // [세로]
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CountryHscode {
        private String JPN;                   // [일본HS코드]
        private String CHN;                   // [중국HS코드]
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RelationalProduct {
        private Integer productNo;            // [상품번호]
        private String interrelated;          // [연관여부]
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ShippingPeriod {
        private Integer minimum;              // [최소배송기간]
        private Integer maximum;              // [최대배송기간]
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ShippingRates {
        private String shippingRatesMin;      // [요금적용최소]
        private String shippingRatesMax;      // [요금적용최대]
        private String shippingFee;           // [배송비]
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SizeGuide {
        private String use;           // [사이즈가이드사용] (Default:T)
        private String type;          // [가이드유형] (Default:default)
        private String _default;      // [기본치] (예:Male)
    }

    // ---------------------------
    // == 빌더형 팩토리 메서드 ==
    // ---------------------------

    public static CafeProductRequest from(
            ProductRegistrationRequest request,
            List<String> uploadedImageLinks
    ) {
        ProductDto productDto = request.getProductDto();
        // 대표이미지는 무조건 첫 번째, 추가이미지는 2번째 이상 리스트로 처리
        String detailImg = uploadedImageLinks.size() > 0 ? uploadedImageLinks.get(0) : null;
        List<String> additionalImgs = uploadedImageLinks.size() > 1
                ? uploadedImageLinks.subList(1, uploadedImageLinks.size()) : new ArrayList<>();

        RequestBody requestBody = RequestBody.builder()
                .detailImage(detailImg)
                .additionalImage(additionalImgs)
                .customProductCode(productDto.getCode())
                .productName(productDto.getTitle())
                .engProductName(productDto.getEngName())
                .internalProductName(productDto.getTitle())
                .price(String.valueOf(productDto.getSalePrice()))
                .supplyPrice(String.valueOf(Math.ceil(productDto.getSalePrice()*0.8/100.0)*100))
                .description(productDto.getDetailsHtml())
                // 필요한 필드는 모두 req에서 꺼내서 세팅! (옵션, 카테고리 등도 추가로 연결 가능)
                .build();

        return CafeProductRequest.builder()
                .shopNo(1) // 또는 request.getShopNo() 등
                .request(requestBody)
                .build();
    }

}
