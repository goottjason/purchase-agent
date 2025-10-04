package com.jason.purchase_agent.external.iherb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

import static org.springframework.util.StringUtils.capitalize;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IherbProductDto {

    private String userCategoryName; // 비타민
    private String userCategoryPath; //

    private String promoBanner;
    private List<List<CanonicalPath>> canonicalPaths;
    private String brandName;
    private String brandUrl;
    private String brandCode;
    private String brandManufacturerUrl;
    private String displayName;
    private String displayEngName;
    private List<FlagInfo> flag;
    private Boolean show360;
    private Integer primaryImageIndex;
    private String partNumber;
    private List<Integer> imageIndices;
    private List<String> campaignImages;
    private List<Integer> enhanceImageIndices;
    private List<Integer> imageIndices360;
    private List<String> imageLinks;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlagInfo {
        private String displayName;
        private int flag;
        private String flagColor;
        private String fontColor;
        private String url;
    }

    public void setImageLinks() {
        if (imageIndices == null || partNumber == null) {
            this.imageLinks = Collections.emptyList();
            return;
        }
        // partNumber에서 "-" 앞 알파벳(소문자)만 추출
        String[] parts = partNumber.split("-");
        String brandLike = parts[0].toLowerCase();           // 첫번째 segment 사용
        String part = partNumber.toLowerCase().replace("-", "");

        List<String> links = new ArrayList<>();
        // imageIndices에서 최대 4개까지만 처리
        int count = 0;
        for (Integer idx : imageIndices) {
            if (count >= 4) break;
            String url = String.format(
                    "https://cloudinary.images-iherb.com/image/upload/f_auto,q_auto:eco/images/%s/%s/l/%d.jpg",
                    brandLike, part, idx
            );
            links.add(url);
            count++;
        }
        this.imageLinks = links;
    }


    public void setDisplayEngNameByRule() {
        // 1. 브랜드명(한글 주석 제거)
        String engBrand = brandName;
        if (engBrand == null) engBrand = "";
        if (engBrand.contains("(")) engBrand = engBrand.substring(0, engBrand.indexOf("(")).trim();

        // 2. urlName에서 브랜드 슬러그 뒤 제거
        if (urlName == null) {
            this.displayEngName = engBrand;
            return;
        }
        String brandSlug = engBrand.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
        String url = urlName.toLowerCase();
        if (url.startsWith(brandSlug + "-")) {
            url = url.substring(brandSlug.length() + 1);
        }
        String[] tokens = url.split("-");

        // 3. 토큰 조합 및 수량/단위 분리 처리
        List<String> parts = new ArrayList<>();
        List<String> units = new ArrayList<>();
        Set<String> unitSet = Set.of("mg", "g", "ml", "oz", "iu", "softgels", "tablets", "capsules", "fl", "lb", "lbs", "servings", "drops", "vials", "pieces", "chewables", "lozenges");
        for (int i = 0; i < tokens.length; i++) {
            // 숫자 + softgels/tablets 식 조합
            if (i + 1 < tokens.length && tokens[i].matches("\\d+") && unitSet.contains(tokens[i + 1])) {
                String num = String.format("%,d", Integer.parseInt(tokens[i]));
                units.add(num + " " + capitalize(tokens[i + 1]));
                i++;
            } else if (tokens[i].matches("\\d+")) {
                units.add(String.format("%,d", Integer.parseInt(tokens[i])));
            } else if (unitSet.contains(tokens[i])) {
                units.add(capitalize(tokens[i]));
            } else if (tokens[i].equals("per") && (i > 0 && i + 1 < tokens.length)) {
                // "mg per softgel" 등 처리
                units.set(units.size() - 1, units.get(units.size() - 1) + " per " + capitalize(tokens[i + 1]));
                i++;
            } else if (!tokens[i].isBlank()) {
                parts.add(capitalizeHyphen(tokens[i]));
            }
        }
        // 결과 조합
        StringBuilder sb = new StringBuilder();
        sb.append(engBrand);
        if (!parts.isEmpty()) sb.append(", ").append(String.join(" ", parts));
        if (!units.isEmpty()) {
            if (units.size() == 1) {
                sb.append(", ").append(units.get(0));
            } else {
                sb.append(", ").append(units.get(0));
                sb.append(" (").append(String.join(", ", units.subList(1, units.size()))).append(")");
            }
        }
        this.displayEngName = sb.toString().replaceAll(" ,", ",").replaceAll("  +", " ").trim();
    }

    // 보조: 하이픈 TitleCase
    private static String capitalizeHyphen(String word) {
        String[] arr = word.split("-");
        for (int i = 0; i < arr.length; i++) {
            if (!arr[i].isBlank()) arr[i] = arr[i].substring(0, 1).toUpperCase() + arr[i].substring(1).toLowerCase();
        }
        return String.join("-", arr);
    }


    // 정적 빌더형 팩토리 메서드 추가
    public static IherbProductDto fromJsonWithLinks(
            String json
    ) {
        Gson gson = new Gson();
        IherbProductDto dto = gson.fromJson(json, IherbProductDto.class);
        dto.setImageLinks(); // imageLinks 등 파생 필드 자동 세팅
        dto.setDisplayEngNameByRule();
        return dto;
    }


}

