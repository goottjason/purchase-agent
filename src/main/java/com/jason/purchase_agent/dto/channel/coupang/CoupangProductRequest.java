package com.jason.purchase_agent.dto.channel.coupang;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import com.jason.purchase_agent.dto.products.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoupangProductRequest {
    /**
     * 노출카테고리코드 (카테고리 목록 조회 API로 확인, 미입력시 자동매칭)
     * Number
     */
    private Long displayCategoryCode; // 미입력(자동매칭)
    /**
     * 등록상품명 (발주서에 사용, 100자 이내)
     * Required
     */
    private String sellerProductName; // brand + korName
    /**
     * 판매자ID(업체코드, 쿠팡에서 발급)
     * Required
     */
    @Builder.Default
    private String vendorId = "A00213055";
    /**
     * 판매시작일시 ("yyyy-MM-dd'T'HH:mm:ss" 형식)
     * Required
     */
    private String saleStartedAt; // 현재시각
    /**
     * 판매종료일시 ("yyyy-MM-dd'T'HH:mm:ss" 형식, 2099년까지 가능)
     * Required
     */
    @Builder.Default
    private String saleEndedAt = "2099-12-31T23:59:59";
    /**
     * 노출상품명 (쿠팡 판매페이지에 실제 노출, 100자 이내.
     * 미입력시 [brand]+[generalProductName] 또는 [sellerProductName])
     * Optional
     */
    private String displayProductName; // brand + korName
    /**
     * 브랜드명 (한글/영어 표준, 특수문자, 띄어쓰기 없음)
     * Optional
     */
    private String brand; // brand
    /**
     * 제품명 (구매옵션명, 모델명 포함 가능)
     * Optional
     */
    private String generalProductName; // korName
    /**
     * 최하위 카테고리 기준 상품종류명
     * Optional, 제품명과 중복시 생략 가능
     */
    private String productGroup; // 미입력 (추천카테고리명?)
    /**
     * 배송방법
     * Required
     * SEQUENCIAL(일반), COLD_FRESH(신선냉동), MAKE_ORDER(주문제작), AGENT_BUY(구매대행), VENDOR_DIRECT(설치/직접전달)
     */
    @Builder.Default
    private String deliveryMethod = "AGENT_BUY";
    /**
     * 택배사 코드 (택배사 코드 표 참고)
     * Required
     */
    @Builder.Default
    private String deliveryCompanyCode = "CJGLS";
    /**
     * 배송비종류 [FREE, NOT_FREE, CHARGE_RECEIVED, CONDITIONAL_FREE]
     * Required
     */
    @Builder.Default
    private String deliveryChargeType = "FREE";
    /**
     * 기본배송비 (편도, 유료배송 또는 조건부 무료 시 필수)
     * Required
     */
    @Builder.Default
    private Integer deliveryCharge = 0; // 미입력
    /**
     * 무료배송 조건 금액 (조건부 무료사용 시 해당, 0은 무조건 무료)
     * Required
     */
    @Builder.Default
    private Integer freeShipOverAmount = 0;
    /**
     * 초도반품배송비 (무료배송시 반품시 비용)
     * Required
     */
    @Builder.Default
    private Integer deliveryChargeOnReturn = 5000;
    /**
     * 도서산간 배송여부 'Y','N'
     * Required
     */
    @Builder.Default
    private String remoteAreaDeliverable = "N";
    /**
     * 묶음배송여부 [UNION_DELIVERY, NOT_UNION_DELIVERY]
     * Required
     */
    @Builder.Default
    private String unionDeliveryType = "UNION_DELIVERY";
    /**
     * 반품지센터코드 (Wing/반품지 생성API로 조회, 해외배송은 국내반품지 계약 필수)
     * Required
     */
    @Builder.Default
    private String returnCenterCode = "1000519746";
    /**
     * 반품지명 (반품지 조회시 ShippingPlaceName)
     * Required
     */
    @Builder.Default
    private String returnChargeName = "서울 금천구";
    /**
     * 반품지 연락처
     * Required
     */
    @Builder.Default
    private String companyContactNumber = "010-2597-2480";
    /**
     * 반품지 우편번호
     * Required
     */
    @Builder.Default
    private String returnZipCode = "08529";
    /**
     * 반품지 주소
     * Required
     */
    @Builder.Default
    private String returnAddress = "서울특별시 금천구 시흥대로153길 90-4 (가산동)";
    /**
     * 반품지 주소상세
     * Required
     */
    @Builder.Default
    private String returnAddressDetail = "103호";
    /**
     * 반품배송비 (편도, 초도반품과 제약)
     * Required
     */
    @Builder.Default
    private Integer returnCharge = 5000;
    /**
     * 출고지 주소 코드 (묶음배송 선택시 필수)
     * Required
     */
    @Builder.Default
    private Integer outboundShippingPlaceCode = 1206157;
    /**
     * 실사용자ID (Wing 로그인 ID, 업체 소속)
     * Required
     */
    @Builder.Default
    private String vendorUserId = "shouldbeshop";
    /**
     * 자동승인요청여부 (true시 저장+자동 승인요청)
     * Required
     */
    @Builder.Default
    private Boolean requested = true;
    /**
     * 업체상품 옵션목록 (아이템 최대 200개까지)
     * Required
     */
    private List<Item> items;
    /**
     * 구비서류 목록 (필수 서류가 있을 때 입력, 5MB 이하 PDF/HWP/DOC/DOCX/TXT/PNG/JPG/JPEG)
     */
    private List<RequiredDocument> requiredDocuments;
    /**
     * 주문제작 안내 메시지 (배송방법 주문제작시)
     */
    private String extraInfoMessage;
    /**
     * 제조사 (정확히, 모를 경우 brand와 동일)
     */
    private String manufacture;
    /**
     * 번들상품 정보 (번들구성시, 옵션불가)
     */
    private BundleInfo bundleInfo;

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item {
        /* 업체상품옵션명. 각각의 아이템에 중복X, 최대 150자. */
        private String itemName; //
        /* 정가. (할인율 %)표기 기준, salePrice와 동일시 '쿠팡가'로 노출됨 */
        private Integer originalPrice; // ceil salePrice / 0.8 / 100 * 100
        /* 판매가격(옵션단위), 최초등록시 판매요청 전에만 변경 가능 */
        private Integer salePrice; // salePrice
        /* 판매가능수량, 999이하 */
        @Builder.Default
        private Integer maximumBuyCount = 999; //
        /* 인당 최대 구매수량, '0' 제한없음 */
        @Builder.Default
        private Integer maximumBuyForPerson = 0; //
        /* 최대 구매수량 기간(일), 제한없을경우 1 */
        @Builder.Default
        private Integer maximumBuyForPersonPeriod = 1; //
        /* 기준출고일(D+N일) */
        @Builder.Default
        private Integer outboundShippingTimeDay = 5; //
        /* 단위수량(개당가격 노출용), 불필요시 0 */
        private Integer unitCount; // packQty
        /* 19세이상 구입여부 [ADULT_ONLY, EVERYONE] */
        @Builder.Default
        private String adultOnly = "EVERYONE"; //
        /* 과세여부 [TAX, FREE] */
        @Builder.Default
        private String taxType = "TAX"; //
        /* 병행수입여부 [PARALLEL_IMPORTED, NOT_PARALLEL_IMPORTED] */
        @Builder.Default
        private String parallelImported = "NOT_PARALLEL_IMPORTED"; //
        /* 구매대행여부 [OVERSEAS_PURCHASED, NOT_OVERSEAS_PURCHASED] */
        @Builder.Default
        private String overseasPurchased = "OVERSEAS_PURCHASED"; // Required
        /* PCC(개인통관고유부호) 필수여부(해외구매대행 시 true 필수) */
        @Builder.Default
        private Boolean pccNeeded = true; // Required
        /* 판매자상품코드(임의) */
        private String externalVendorSku; // code
        /* 바코드(유효한 표준) */
        private String barcode; // 미입력
        /* 바코드 없음 여부(true시 이유 필수) */
        private Boolean emptyBarcode; // 미입력
        /* 바코드 없음 사유(최대100자, emptyBarcode true시 필수) */
        private String emptyBarcodeReason; // 미입력
        /* 모델번호 */
        private String modelNo; // 미입력
        /* 옵션별 추가 속성(key-value Map, 필요만큼 입력 가능) */
        private Map<String, Object> extraProperties; // 미입력
        /* 상품인증정보 */
        private List<Certification> certifications; // 미입력
        /* 검색어(최대 20개, 20자 이내, 일부 특수문자 불가) */
        private List<String> searchTags; // 미입력
        /* 옵션이미지 목록(0,1,2 ...) */
        private List<Image> images; //
        /* 상품고시정보 목록(카테고리별) */
        private List<Notice> notices;
        /* 옵션 속성목록(구매옵션, 필수/카테고리별 제한) */
        private List<Attribute> attributes; //  (최소1)
        /* 컨텐츠(상세/요약이미지, 텍스트 등) */
        private List<Content> contents; //
        /* 상품상태 [NEW, REFURBISHED, USED_BEST, USED_GOOD, USED_NORMAL], offerCondition 지정 없으면 NEW로 취급 */
        private String offerCondition;
        /* 중고상세설명 (중고입력시 700자 이내) */
        private String offerDescription;
    }

    // -- 하위 객체 구조체들 --

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Certification {
        /* 상품인증정보타입 (카테고리별 지정, NOT_REQUIRED 등) */
        private String certificationType; // 미입력
        /* 상품인증코드(인증기관발급) */
        private String certificationCode; // 미입력
        /* 인증정보 첨부파일 (쿠팡CDN/vendorPath) */
        private List<CertificationAttachment> certificationAttachments; // 미입력
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CertificationAttachment {
        /* 업체이미지경로, http:// 시작시 CDN 자동연동 */
        private String vendorPath; // 미입력
        /* 쿠팡CDN 경로 */
        private String cdnPath; // 미입력
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Image {
        /* 이미지 표시순서 0,1,2, ... */
        private Integer imageOrder; //
        /* 이미지타입: REPRESENTATION(대표), DETAIL(상세: 9개), USED_PRODUCT(중고: 4개) */
        private String imageType; //
        /* 쿠팡CDN 경로(최대 200자) vendorPath/cdnPath 중 1개 필수 */
        private String cdnPath; // 미입력
        /* 업체이미지경로(최대 200자) http:// 시작 필수 */
        private String vendorPath; //
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Notice {
        /* 상품고시정보카테고리명(카테고리별 선택) */
        private String noticeCategoryName;
        /* 상품고시정보카테고리 상세명 */
        private String noticeCategoryDetailName;
        /* 내용 */
        private String content;
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Attribute {
        /* 옵션타입명 (최대25자, 카테고리별 한정) */
        private String attributeTypeName; // Required
        /* 옵션값(단위포함, 최대30자) */
        private String attributeValueName; // Required
        /* 옵션/필터구분(none값시 필터로 등록, 해당없으면 옵션) */
        private String exposed;
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Content {
        /* 컨텐츠타입 (IMAGE, TEXT, IMAGE_TEXT, HTML 등) */
        private String contentsType; // Required
        /* 상세컨텐츠 목록 */
        private List<ContentDetail> contentDetails; // Required
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentDetail {
        /* 내용 (이미지, 텍스트, HTML 등) */
        private String content; // Required
        /* 세부타입 (IMAGE, TEXT) */
        private String detailType; // Required
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequiredDocument {
        /* 구비서류템플릿명 (카테고리 메타 조회필요) */
        private String templateName;
        /* 구비서류 쿠팡 CDN 경로(최대150자) */
        private String documentPath;
        /* 업체경로(vendorDocumentPath) http:// 시작시 CDN 자동업로드 */
        private String vendorDocumentPath;
    }

    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BundleInfo {
        /* 번들구성구분: SINGLE(기본), AB(혼합, 옵션불가) */
        private String bundleType;
    }


    // ---------------------------
    // == 빌더형 팩토리 메서드 ==
    // ---------------------------
    public static CoupangProductRequest from(ProductDto dto) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        String korMainName = cleanKorName(dto.getKorName());

        CoupangProductRequest request = CoupangProductRequest.builder()
                .sellerProductName(dto.getBrandName() + " " + korMainName)
                .saleStartedAt(LocalDateTime.now().format(fmt))
                .displayProductName(dto.getBrandName() + " " + korMainName)
                .brand(dto.getBrandName())
                .generalProductName(dto.getKorName())
                .build();

        return request;
    }

    private static String cleanKorName(String korName) {
        if (korName == null) return "";
        int commaIdx = korName.indexOf(",");
        if (commaIdx != -1) {
            return korName.substring(0, commaIdx).trim();
        }
        return korName.trim();
    }

    public static final List<Notice> HEALTH_FUNCTIONAL_NOTICES = List.of(
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("제품명").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("제조업소의 명칭과 소재지").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("소비기한 및 보관방법").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("포장단위별 내용물의 용량(중량), 수량").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("원료명 및 함량").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("영양정보").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("기능정보").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("섭취량, 섭취방법, 섭취 시 주의사항 및 부작용 발생 가능성").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("의약품 여부").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("유전자변형건강식품에 해당하는 경우의 표시").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("수입 건강기능식품 문구").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("소비자안전을 위한 주의사항").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("건강기능식품").noticeCategoryDetailName("소비자상담관련 전화번호").content("상세페이지 참조").build()
    );
    public static final List<Notice> PROCESSED_FOOD_NOTICES = List.of(
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("제품명").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("식품의 유형").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("생산자 및 소재지").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("제조연월일, 소비기한 또는 품질유지기한").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("포장단위별 내용물의 용량(중량), 수량").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("원재료명 및 함량").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("영양성분").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("유전자변형식품에 해당하는 경우의 표시").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("소비자안전을 위한 주의사항").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("수입식품 문구").content("상세페이지 참조").build(),
            Notice.builder().noticeCategoryName("가공식품").noticeCategoryDetailName("소비자상담관련 전화번호").content("상세페이지 참조").build()
    );
}
