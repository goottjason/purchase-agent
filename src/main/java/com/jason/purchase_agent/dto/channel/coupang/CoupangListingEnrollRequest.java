package com.jason.purchase_agent.dto.channel.coupang;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoupangListingEnrollRequest {
  /**
   * 노출카테고리코드 (카테고리 목록 조회 API로 확인, 미입력시 자동매칭)
   * Number
   */
  private Long displayCategoryCode;

  /**
   * 등록상품명 (발주서에 사용, 100자 이내)
   * Required
   */
  private String sellerProductName;

  /**
   * 판매자ID(업체코드, 쿠팡에서 발급)
   * Required
   */
  private String vendorId;

  /**
   * 판매시작일시 ("yyyy-MM-dd'T'HH:mm:ss" 형식)
   * Required
   */
  private String saleStartedAt;

  /**
   * 판매종료일시 ("yyyy-MM-dd'T'HH:mm:ss" 형식, 2099년까지 가능)
   * Required
   */
  private String saleEndedAt;

  /**
   * 노출상품명 (쿠팡 판매페이지에 실제 노출, 100자 이내. 미입력시 [brand]+[generalProductName] 또는 [sellerProductName])
   * Optional
   */
  private String displayProductName;

  /**
   * 브랜드명 (한글/영어 표준, 특수문자, 띄어쓰기 없음)
   * Optional
   */
  private String brand;

  /**
   * 제품명 (구매옵션명, 모델명 포함 가능)
   * Optional
   */
  private String generalProductName;

  /**
   * 최하위 카테고리 기준 상품종류명
   * Optional, 제품명과 중복시 생략 가능
   */
  private String productGroup;

  /**
   * 배송방법
   * Required
   * SEQUENCIAL(일반), COLD_FRESH(신선냉동), MAKE_ORDER(주문제작), AGENT_BUY(구매대행), VENDOR_DIRECT(설치/직접전달)
   */
  private String deliveryMethod;

  /**
   * 택배사 코드 (택배사 코드 표 참고)
   * Required
   */
  private String deliveryCompanyCode;

  /**
   * 배송비종류 [FREE, NOT_FREE, CHARGE_RECEIVED, CONDITIONAL_FREE]
   * Required
   */
  private String deliveryChargeType;

  /**
   * 기본배송비 (편도, 유료배송 또는 조건부 무료 시 필수)
   * Required
   */
  private Integer deliveryCharge;

  /**
   * 무료배송 조건 금액 (조건부 무료사용 시 해당, 0은 무조건 무료)
   * Required
   */
  private Integer freeShipOverAmount;

  /**
   * 초도반품배송비 (무료배송시 반품시 비용)
   * Required
   */
  private Integer deliveryChargeOnReturn;

  /**
   * 도서산간 배송여부 'Y','N'
   * Required
   */
  private String remoteAreaDeliverable;

  /**
   * 묶음배송여부 [UNION_DELIVERY, NOT_UNION_DELIVERY]
   * Required
   */
  private String unionDeliveryType;

  /**
   * 반품지센터코드 (Wing/반품지 생성API로 조회, 해외배송은 국내반품지 계약 필수)
   * Required
   */
  private String returnCenterCode;

  /**
   * 반품지명 (반품지 조회시 ShippingPlaceName)
   * Required
   */
  private String returnChargeName;

  /**
   * 반품지 연락처
   * Required
   */
  private String companyContactNumber;

  /**
   * 반품지 우편번호
   * Required
   */
  private String returnZipCode;

  /**
   * 반품지 주소
   * Required
   */
  private String returnAddress;

  /**
   * 반품지 주소상세
   * Required
   */
  private String returnAddressDetail;

  /**
   * 반품배송비 (편도, 초도반품과 제약)
   * Required
   */
  private Integer returnCharge;

  /**
   * 출고지 주소 코드 (묶음배송 선택시 필수)
   * Required
   */
  private String outboundShippingPlaceCode;

  /**
   * 실사용자ID (Wing 로그인 ID, 업체 소속)
   * Required
   */
  private String vendorUserId;

  /**
   * 자동승인요청여부 (true시 저장+자동 승인요청)
   * Required
   */
  private Boolean requested;

  /**
   * 업체상품 옵션목록 (아이템 최대 200개까지)
   * Required
   */
  private List<Item> items;

  /**
   * 구비서류 목록 (필수 서류가 있을 때 입력, 5MB 이하 PDF/HWP/DOC/DOCX/TXT/PNG/JPG/JPEG)
   */
  private List<RequiredDocument> requiredDocuments;

  /** 주문제작 안내 메시지 (배송방법 주문제작시) */
  private String extraInfoMessage;

  /** 제조사 (정확히, 모를 경우 brand와 동일) */
  private String manufacture;

  /** 번들상품 정보 (번들구성시, 옵션불가) */
  private BundleInfo bundleInfo;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Item {
    /* 업체상품옵션명. 각각의 아이템에 중복X, 최대 150자. */
    private String itemName; // Required
    /* 정가. (할인율 %)표기 기준, salePrice와 동일시 '쿠팡가'로 노출됨 */
    private Integer originalPrice; // Required
    /* 판매가격(옵션단위), 최초등록시 판매요청 전에만 변경 가능 */
    private Integer salePrice; // Required
    /* 판매가능수량, 999이하 */
    private Integer maximumBuyCount; // Required
    /* 인당 최대 구매수량, '0' 제한없음 */
    private Integer maximumBuyForPerson; // Required
    /* 최대 구매수량 기간(일), 제한없을경우 1 */
    private Integer maximumBuyForPersonPeriod; // Required
    /* 기준출고일(D+N일) */
    private Integer outboundShippingTimeDay; // Required
    /* 단위수량(개당가격 노출용), 불필요시 0 */
    private Integer unitCount; // Required
    /* 19세이상 구입여부 [ADULT_ONLY, EVERYONE] */
    private String adultOnly; // Required
    /* 과세여부 [TAX, FREE] */
    private String taxType; // Required
    /* 병행수입여부 [PARALLEL_IMPORTED, NOT_PARALLEL_IMPORTED] */
    private String parallelImported; // Required
    /* 구매대행여부 [OVERSEAS_PURCHASED, NOT_OVERSEAS_PURCHASED] */
    private String overseasPurchased; // Required
    /* PCC(개인통관고유부호) 필수여부(해외구매대행 시 true 필수) */
    private Boolean pccNeeded; // Required
    /* 판매자상품코드(임의) */
    private String externalVendorSku;
    /* 바코드(유효한 표준) */
    private String barcode;
    /* 바코드 없음 여부(true시 이유 필수) */
    private Boolean emptyBarcode;
    /* 바코드 없음 사유(최대100자, emptyBarcode true시 필수) */
    private String emptyBarcodeReason;
    /* 모델번호 */
    private String modelNo;
    /* 옵션별 추가 속성(key-value Map, 필요만큼 입력 가능) */
    private Map<String, Object> extraProperties;
    /* 상품인증정보 */
    private List<Certification> certifications;
    /* 검색어(최대 20개, 20자 이내, 일부 특수문자 불가) */
    private List<String> searchTags;
    /* 옵션이미지 목록(0,1,2 ...) */
    private List<Image> images; // Required
    /* 상품고시정보 목록(카테고리별) */
    private List<Notice> notices;
    /* 옵션 속성목록(구매옵션, 필수/카테고리별 제한) */
    private List<Attribute> attributes; // Required (최소1)
    /* 컨텐츠(상세/요약이미지, 텍스트 등) */
    private List<Content> contents; // Required
    /* 상품상태 [NEW, REFURBISHED, USED_BEST, USED_GOOD, USED_NORMAL], offerCondition 지정 없으면 NEW로 취급 */
    private String offerCondition;
    /* 중고상세설명 (중고입력시 700자 이내) */
    private String offerDescription;
  }

  // -- 하위 객체 구조체들 --

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Certification {
    /* 상품인증정보타입 (카테고리별 지정, NOT_REQUIRED 등) */
    private String certificationType;
    /* 상품인증코드(인증기관발급) */
    private String certificationCode;
    /* 인증정보 첨부파일 (쿠팡CDN/vendorPath) */
    private List<CertificationAttachment> certificationAttachments;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CertificationAttachment {
    /* 업체이미지경로, http:// 시작시 CDN 자동연동 */
    private String vendorPath;
    /* 쿠팡CDN 경로 */
    private String cdnPath;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Image {
    /* 이미지 표시순서 0,1,2, ... */
    private Integer imageOrder; // Required
    /* 이미지타입: REPRESENTATION(대표), DETAIL(상세: 9개), USED_PRODUCT(중고: 4개) */
    private String imageType; // Required
    /* 쿠팡CDN 경로(최대 200자) vendorPath/cdnPath 중 1개 필수 */
    private String cdnPath;
    /* 업체이미지경로(최대 200자) http:// 시작 필수 */
    private String vendorPath;
  }
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Notice {
    /* 상품고시정보카테고리명(카테고리별 선택) */
    private String noticeCategoryName;
    /* 상품고시정보카테고리 상세명 */
    private String noticeCategoryDetailName;
    /* 내용 */
    private String content;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Attribute {
    /* 옵션타입명 (최대25자, 카테고리별 한정) */
    private String attributeTypeName; // Required
    /* 옵션값(단위포함, 최대30자) */
    private String attributeValueName; // Required
    /* 옵션/필터구분(none값시 필터로 등록, 해당없으면 옵션) */
    private String exposed;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Content {
    /* 컨텐츠타입 (IMAGE, TEXT, IMAGE_TEXT, HTML 등) */
    private String contentsType; // Required
    /* 상세컨텐츠 목록 */
    private List<ContentDetail> contentDetails; // Required
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ContentDetail {
    /* 내용 (이미지, 텍스트, HTML 등) */
    private String content; // Required
    /* 세부타입 (IMAGE, TEXT) */
    private String detailType; // Required
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class RequiredDocument {
    /* 구비서류템플릿명 (카테고리 메타 조회필요) */
    private String templateName;
    /* 구비서류 쿠팡 CDN 경로(최대150자) */
    private String documentPath;
    /* 업체경로(vendorDocumentPath) http:// 시작시 CDN 자동업로드 */
    private String vendorDocumentPath;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class BundleInfo {
    /* 번들구성구분: SINGLE(기본), AB(혼합, 옵션불가) */
    private String bundleType;
  }
}
