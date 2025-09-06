package com.jason.purchase_agent.dto.channel.elevenst;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
//

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 생략시 아예 필드가 나타나지 않음
@JacksonXmlRootElement(localName = "Product")
public class ElevenstListingEnrollRequest {

  @JacksonXmlProperty(localName = "abrdBuyPlace")
  private String abrdBuyPlace = "D";                    // ◎해외상품코드, D(현지온라인쇼핑몰)

  @JacksonXmlProperty(localName = "abrdSizetableDispYn")
  private String abrdSizetableDispYn = "N";             // ◎해외사이즈 조견표 노출여부, N(노출안함)

  @JacksonXmlProperty(localName = "selMnbdNckNm")
  private String selMnbdNckNm;                          // ○닉네임

  @JacksonXmlProperty(localName = "selMthdCd")
  private String selMthdCd = "01";                      // ◎판매방식, 01(고정가 판매)

  @JacksonXmlProperty(localName = "dispCtgrNo")
  private String dispCtgrNo;                            // ★카테고리번호

  @JacksonXmlProperty(localName = "PartnerCategory")
  private PartnerCategory partnerCategory;              // ◇◇제휴사카테고리

  @JacksonXmlProperty(localName = "prdTypCd")
  private String prdTypCd = "01";                       // ◎서비스 상품코드,  01(일반상품)

  @JacksonXmlProperty(localName = "hsCode")
  private String hsCode;                                // ○H.S Code

  @JacksonXmlProperty(localName = "prdNm")
  private String prdNm;                                 // ●상품명

  @JacksonXmlProperty(localName = "prdNmEng")
  private String prdNmEng;                              // ●영문 상품명

  @JacksonXmlProperty(localName = "advrtStmt")
  private String advrtStmt;                             // ●상품홍보문구(?)

  @JacksonXmlProperty(localName = "brand")
  private String brand;                                 // ●브랜드

  @JacksonXmlProperty(localName = "apiPrdAttrBrandCd")
  private String apiPrdAttrBrandCd;                     // ○브랜드코드

  @JacksonXmlProperty(localName = "rmaterialTypCd")
  private String rmaterialTypCd = "05";                 // ◎원재료 유형 코드, 05(상세참조)

  @JacksonXmlProperty(localName = "orgnTypCd")
  private String orgnTypCd = "03";                      // ◎원산지 코드, 03(기타)

  @JacksonXmlProperty(localName = "orgnDifferentYn")
  private String orgnDifferentYn;                       // ○원산지가 다른 상품 같이 등록

  @JacksonXmlProperty(localName = "orgnTypDtlsCd")
  private String orgnTypDtlsCd;                         // ○원산지 지역 코드

  @JacksonXmlProperty(localName = "orgnNmVal")
  private String orgnNmVal = "상세설명 참조";           // ○원산지명

  @JacksonXmlProperty(localName = "ProductRmaterial")
  private ProductRmaterial productRmaterial;            // ◇◇원재료 정보

  @JacksonXmlProperty(localName = "beefTraceStat")
  private String beefTraceStat;                   // enum, 필수

  @JacksonXmlProperty(localName = "beefTraceNo")
  private String beefTraceNo;                     // string, 선택

  // 판매 관련 정보
  @JacksonXmlProperty(localName = "sellerPrdCd")
  private String sellerPrdCd;                     // string, 선택

  @JacksonXmlProperty(localName = "suplDtyfrPrdClfCd")
  private String suplDtyfrPrdClfCd;               // enum, 필수

  @JacksonXmlProperty(localName = "yearEndTaxYn")
  private String yearEndTaxYn;                    // enum, 선택

  @JacksonXmlProperty(localName = "forAbrdBuyClf")
  private String forAbrdBuyClf;                   // enum, 필수

  @JacksonXmlProperty(localName = "importFeeCd")
  private String importFeeCd;                     // enum, 선택

  @JacksonXmlProperty(localName = "prdStatCd")
  private String prdStatCd;                       // enum, 필수

  @JacksonXmlProperty(localName = "useMon")
  private String useMon;                          // string, 선택

  @JacksonXmlProperty(localName = "gradeRefur")
  private String gradeRefur;                      // enum, 선택

  @JacksonXmlProperty(localName = "paidSelPrc")
  private String paidSelPrc;                      // string, 선택

  @JacksonXmlProperty(localName = "exteriorSpecialNote")
  private String exteriorSpecialNote;             // string, 선택

  @JacksonXmlProperty(localName = "minorSelCnYn")
  private String minorSelCnYn;                    // enum, 필수

  // 이미지 정보
  @JacksonXmlProperty(localName = "prdImage01")
  private String prdImage01;                      // string, 필수

  @JacksonXmlProperty(localName = "prdImage02")
  private String prdImage02;                      // string, 선택

  @JacksonXmlProperty(localName = "prdImage03")
  private String prdImage03;                      // string, 선택

  @JacksonXmlProperty(localName = "prdImage04")
  private String prdImage04;                      // string, 선택

  @JacksonXmlProperty(localName = "prdImage09")
  private String prdImage09;                      // string, 선택

  @JacksonXmlProperty(localName = "htmlDetail")
  private String htmlDetail;                      // string, 필수

  // 인증 관련 정보
  @JacksonXmlProperty(localName = "ProductCertGroup")
  private ProductCertGroup productCertGroup;      // object, 필수

  @JacksonXmlProperty(localName = "ProductMedical")
  private ProductMedical productMedical;          // object, 선택

  // 리뷰 관련
  @JacksonXmlProperty(localName = "reviewDispYn")
  private String reviewDispYn;                    // string, 선택

  @JacksonXmlProperty(localName = "reviewOptDispYn")
  private String reviewOptDispYn;                 // string, 선택

  // 판매 조건 관련
  @JacksonXmlProperty(localName = "selPrdClfCd")
  private String selPrdClfCd;                     // enum, 선택

  @JacksonXmlProperty(localName = "aplBgnDy")
  private String aplBgnDy;                        // string, 선택

  @JacksonXmlProperty(localName = "aplEndDy")
  private String aplEndDy;                        // string, 선택

  @JacksonXmlProperty(localName = "setFpSelTermYn")
  private String setFpSelTermYn;                  // enum, 선택

  @JacksonXmlProperty(localName = "selTermUseYn")
  private String selTermUseYn;                    // enum, 선택

  @JacksonXmlProperty(localName = "selPrdClfFpCd")
  private String selPrdClfFpCd;                   // enum, 선택

  @JacksonXmlProperty(localName = "wrhsPlnDy")
  private String wrhsPlnDy;                       // string, 선택

  @JacksonXmlProperty(localName = "contractCd")
  private String contractCd;                      // enum, 선택

  @JacksonXmlProperty(localName = "chargeCd")
  private String chargeCd;                        // string, 선택

  @JacksonXmlProperty(localName = "periodCd")
  private String periodCd;                        // enum, 선택

  // 가격 정보
  @JacksonXmlProperty(localName = "phonePrc")
  private String phonePrc;                        // string, 선택

  @JacksonXmlProperty(localName = "maktPrc")
  private String maktPrc;                         // string, 선택

  @JacksonXmlProperty(localName = "recommendRetailPrice")
  private String recommendRetailPrice;            // string, 선택

  @JacksonXmlProperty(localName = "selPrc")
  private String selPrc;                          // string, 필수

  // 쿠폰 관련
  @JacksonXmlProperty(localName = "cuponcheck")
  private String cuponcheck;                      // enum, 선택

  @JacksonXmlProperty(localName = "dscAmtPercnt")
  private String dscAmtPercnt;                    // string, 선택

  @JacksonXmlProperty(localName = "cupnDscMthdCd")
  private String cupnDscMthdCd;                   // enum, 선택

  @JacksonXmlProperty(localName = "cupnUseLmtDyYn")
  private String cupnUseLmtDyYn;                  // enum, 선택

  @JacksonXmlProperty(localName = "cupnIssEndDy")
  private String cupnIssEndDy;                    // string, 선택

  // 페이 관련
  @JacksonXmlProperty(localName = "pay11YN")
  private String pay11YN;                         // enum, 선택

  @JacksonXmlProperty(localName = "pay11Value")
  private String pay11Value;                      // string, 선택

  @JacksonXmlProperty(localName = "pay11WyCd")
  private String pay11WyCd;                       // enum, 선택

  // 할부 관련
  @JacksonXmlProperty(localName = "intFreeYN")
  private String intFreeYN;                       // enum, 선택

  @JacksonXmlProperty(localName = "intfreeMonClfCd")
  private String intfreeMonClfCd;                 // enum, 선택

  // 플러스 할인 관련
  @JacksonXmlProperty(localName = "pluYN")
  private String pluYN;                           // enum, 선택

  @JacksonXmlProperty(localName = "pluDscCd")
  private String pluDscCd;                        // enum, 선택

  @JacksonXmlProperty(localName = "pluDscBasis")
  private String pluDscBasis;                     // string, 선택

  @JacksonXmlProperty(localName = "pluDscAmtPercnt")
  private String pluDscAmtPercnt;                 // string, 선택

  @JacksonXmlProperty(localName = "pluDscMthdCd")
  private String pluDscMthdCd;                    // enum, 선택

  @JacksonXmlProperty(localName = "pluUseLmtDyYn")
  private String pluUseLmtDyYn;                   // enum, 선택

  @JacksonXmlProperty(localName = "pluIssStartDy")
  private String pluIssStartDy;                   // string, 선택

  @JacksonXmlProperty(localName = "pluIssEndDy")
  private String pluIssEndDy;                     // string, 선택

  // 희망 배송 관련
  @JacksonXmlProperty(localName = "hopeShpYn")
  private String hopeShpYn;                       // enum, 선택

  @JacksonXmlProperty(localName = "hopeShpPnt")
  private String hopeShpPnt;                      // string, 선택

  @JacksonXmlProperty(localName = "hopeShpWyCd")
  private String hopeShpWyCd;                     // enum, 선택

  // 옵션 관련
  @JacksonXmlProperty(localName = "optSelectYn")
  private String optSelectYn;                     // string, 선택

  @JacksonXmlProperty(localName = "txtColCnt")
  private String txtColCnt;                       // string, 선택

  @JacksonXmlProperty(localName = "optionAllQty")
  private String optionAllQty;                    // string, 선택

  @JacksonXmlProperty(localName = "optionAllAddPrc")
  private String optionAllAddPrc;                 // string, 선택

  @JacksonXmlProperty(localName = "optionAllAddWght")
  private String optionAllAddWght;                // string, 필수

  @JacksonXmlProperty(localName = "prdExposeClfCd")
  private String prdExposeClfCd;                  // enum, 선택

  @JacksonXmlProperty(localName = "optMixYn")
  private String optMixYn;                        // enum, 선택

  // 옵션 객체들
  @JacksonXmlProperty(localName = "ProductOption")
  private ProductOption productOption;            // object, 선택

  @JacksonXmlProperty(localName = "ProductRootOption")
  private ProductRootOption productRootOption;    // object, 선택

  @JacksonXmlProperty(localName = "ProductOptionExt")
  private ProductOptionExt productOptionExt;      // object, 선택

  @JacksonXmlProperty(localName = "ProductCustOption")
  private ProductCustOption productCustOption;    // object, 선택

  // 커스텀 옵션 관련 필드들 (XML에서 ProductCustOption 밖에 위치)
  @JacksonXmlProperty(localName = "useOptCalc")
  private String useOptCalc;                      // string, 선택

  @JacksonXmlProperty(localName = "optCalcTranType")
  private String optCalcTranType;                 // enum, 선택

  @JacksonXmlProperty(localName = "optTypCd")
  private String optTypCd;                        // string, 선택

  @JacksonXmlProperty(localName = "optItem1Nm")
  private String optItem1Nm;                      // string, 선택

  @JacksonXmlProperty(localName = "optItem1MinValue")
  private String optItem1MinValue;                // string, 선택

  @JacksonXmlProperty(localName = "optItem1MaxValue")
  private String optItem1MaxValue;                // string, 선택

  @JacksonXmlProperty(localName = "optItem2Nm")
  private String optItem2Nm;                      // string, 선택

  @JacksonXmlProperty(localName = "optItem2MinValue")
  private String optItem2MinValue;                // string, 선택

  @JacksonXmlProperty(localName = "optItem2MaxValue")
  private String optItem2MaxValue;                // string, 선택

  @JacksonXmlProperty(localName = "optUnitPrc")
  private String optUnitPrc;                      // string, 선택

  @JacksonXmlProperty(localName = "optUnitCd")
  private String optUnitCd;                       // enum, 선택

  @JacksonXmlProperty(localName = "optSelUnit")
  private String optSelUnit;                      // string, 선택

  // 구성품
  @JacksonXmlProperty(localName = "ProductComponent")
  private ProductComponent productComponent;      // object, 선택

  // 판매 수량 제한
  @JacksonXmlProperty(localName = "prdSelQty")
  private String prdSelQty;                       // string, 선택

  @JacksonXmlProperty(localName = "selMinLimitTypCd")
  private String selMinLimitTypCd;                // enum, 선택

  @JacksonXmlProperty(localName = "selMinLimitQty")
  private String selMinLimitQty;                  // string, 선택

  @JacksonXmlProperty(localName = "selLimitTypCd")
  private String selLimitTypCd;                   // enum, 선택

  @JacksonXmlProperty(localName = "selLimitQty")
  private String selLimitQty;                     // string, 선택

  @JacksonXmlProperty(localName = "townSelLmtDy")
  private String townSelLmtDy;                    // string, 선택

  // 선물 관련
  @JacksonXmlProperty(localName = "useGiftYn")
  private String useGiftYn;                       // enum, 선택

  @JacksonXmlProperty(localName = "ProductGift")
  private ProductGift productGift;                // object, 선택

  @JacksonXmlProperty(localName = "gftPackTypCd")
  private String gftPackTypCd;                    // enum, 선택

  // 배송 관련 정보
  @JacksonXmlProperty(localName = "dlvCnAreaCd")
  private String dlvCnAreaCd;                     // enum, 필수

  @JacksonXmlProperty(localName = "dlvWyCd")
  private String dlvWyCd;                         // enum, 필수

  @JacksonXmlProperty(localName = "dlvEtprsCd")
  private String dlvEtprsCd;                      // enum, 선택

  @JacksonXmlProperty(localName = "dlvSendCloseTmpltNo")
  private String dlvSendCloseTmpltNo;             // string, 필수

  @JacksonXmlProperty(localName = "dlvCstInstBasiCd")
  private String dlvCstInstBasiCd;                // enum, 필수

  @JacksonXmlProperty(localName = "dlvCst1")
  private String dlvCst1;                         // string, 선택

  @JacksonXmlProperty(localName = "dlvCst3")
  private String dlvCst3;                         // string, 선택

  @JacksonXmlProperty(localName = "dlvCst4")
  private String dlvCst4;                         // string, 선택

  @JacksonXmlProperty(localName = "dlvCstInfoCd")
  private String dlvCstInfoCd;                    // enum, 선택

  @JacksonXmlProperty(localName = "PrdFrDlvBasiAmt")
  private String prdFrDlvBasiAmt;                 // string, 필수

  @JacksonXmlProperty(localName = "dlvCnt1")
  private String dlvCnt1;                         // string, 필수

  @JacksonXmlProperty(localName = "dlvCnt2")
  private String dlvCnt2;                         // string, 필수

  @JacksonXmlProperty(localName = "bndlDlvCnYn")
  private String bndlDlvCnYn;                     // enum, 필수

  @JacksonXmlProperty(localName = "dlvCstPayTypCd")
  private String dlvCstPayTypCd;                  // enum, 필수

  @JacksonXmlProperty(localName = "jejuDlvCst")
  private String jejuDlvCst;                      // string, 필수

  @JacksonXmlProperty(localName = "islandDlvCst")
  private String islandDlvCst;                    // string, 필수

  @JacksonXmlProperty(localName = "addrSeqOut")
  private String addrSeqOut;                      // string, 필수

  @JacksonXmlProperty(localName = "outsideYnOut")
  private String outsideYnOut;                    // enum, 선택

  @JacksonXmlProperty(localName = "visitDlvYn")
  private String visitDlvYn;                      // string, 선택

  @JacksonXmlProperty(localName = "visitAddrSeq")
  private String visitAddrSeq;                    // string, 선택

  @JacksonXmlProperty(localName = "addrSeqOutMemNo")
  private String addrSeqOutMemNo;                 // string, 선택

  @JacksonXmlProperty(localName = "addrSeqIn")
  private String addrSeqIn;                       // string, 필수

  @JacksonXmlProperty(localName = "outsideYnIn")
  private String outsideYnIn;                     // enum, 선택

  @JacksonXmlProperty(localName = "addrSeqInMemNo")
  private String addrSeqInMemNo;                  // string, 선택

  @JacksonXmlProperty(localName = "abrdCnDlvCst")
  private String abrdCnDlvCst;                    // string, 선택

  @JacksonXmlProperty(localName = "rtngdDlvCst")
  private String rtngdDlvCst;                     // string, 필수

  @JacksonXmlProperty(localName = "exchDlvCst")
  private String exchDlvCst;                      // string, 필수

  @JacksonXmlProperty(localName = "rtngdDlvCd")
  private String rtngdDlvCd;                      // enum, 선택

  @JacksonXmlProperty(localName = "asDetail")
  private String asDetail;                        // string, 필수

  @JacksonXmlProperty(localName = "rtngExchDetail")
  private String rtngExchDetail;                  // string, 필수

  @JacksonXmlProperty(localName = "dlvClf")
  private String dlvClf;                          // enum, 필수

  @JacksonXmlProperty(localName = "abrdInCd")
  private String abrdInCd;                        // enum, 필수

  @JacksonXmlProperty(localName = "prdWght")
  private String prdWght;                         // string, 필수

  @JacksonXmlProperty(localName = "ntShortNm")
  private String ntShortNm;                       // string, 필수

  @JacksonXmlProperty(localName = "globalOutAddrSeq")
  private String globalOutAddrSeq;                // string, 필수

  @JacksonXmlProperty(localName = "mbAddrLocation05")
  private String mbAddrLocation05;                // enum, 필수

  @JacksonXmlProperty(localName = "globalInAddrSeq")
  private String globalInAddrSeq;                 // string, 필수

  @JacksonXmlProperty(localName = "mbAddrLocation06")
  private String mbAddrLocation06;                // enum, 필수

  // 제조/유효 일자
  @JacksonXmlProperty(localName = "mnfcDy")
  private String mnfcDy;                          // string, 선택

  @JacksonXmlProperty(localName = "eftvDy")
  private String eftvDy;                          // string, 선택

  // 상품 고시 정보
  @JacksonXmlProperty(localName = "ProductNotification")
  private ProductNotification productNotification; // object, 필수

  // 상품고시 관련 추가 필드들
  @JacksonXmlProperty(localName = "company")
  private String company;                         // string, 선택

  @JacksonXmlProperty(localName = "modelNm")
  private String modelNm;                         // string, 선택

  @JacksonXmlProperty(localName = "modelCd")
  private String modelCd;                         // string, 선택

  @JacksonXmlProperty(localName = "mainTitle")
  private String mainTitle;                       // string, 선택

  @JacksonXmlProperty(localName = "artist")
  private String artist;                          // string, 선택

  @JacksonXmlProperty(localName = "mudvdLabel")
  private String mudvdLabel;                      // string, 선택

  @JacksonXmlProperty(localName = "maker")
  private String maker;                           // string, 선택

  @JacksonXmlProperty(localName = "albumNm")
  private String albumNm;                         // string, 선택

  @JacksonXmlProperty(localName = "dvdTitle")
  private String dvdTitle;                        // string, 선택

  // 기타
  @JacksonXmlProperty(localName = "bcktExYn")
  private String bcktExYn;                        // string, 선택

  @JacksonXmlProperty(localName = "prcCmpExpYn")
  private String prcCmpExpYn;                     // enum, 선택

  @JacksonXmlProperty(localName = "prcDscCmpExpYn")
  private String prcDscCmpExpYn;                  // enum, 선택

  @JacksonXmlProperty(localName = "martCPSAgreeYn")
  private String martCPSAgreeYn;                  // enum, 선택

  @JacksonXmlProperty(localName = "stdPrdYn")
  private String stdPrdYn;                        // string, 선택

  @JacksonXmlProperty(localName = "ProductTag")
  private ProductTag productTag;                  // object, 선택

  @JacksonXmlProperty(localName = "ProductLuxury")
  private ProductLuxury productLuxury;            // object, 선택





  /**
   * 파트너 카테고리 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class PartnerCategory {
    @JacksonXmlProperty(localName = "leafCategoryYn")
    private String leafCategoryYn;              // 리프카테고리 여부

    @JacksonXmlProperty(localName = "categoryName1")
    private String categoryName1;               // 대분류명

    @JacksonXmlProperty(localName = "categoryName2")
    private String categoryName2;               // 중분류명

    @JacksonXmlProperty(localName = "categoryName3")
    private String categoryName3;               // 소분류명

    @JacksonXmlProperty(localName = "categoryName4")
    private String categoryName4;               // 세분류명

    @JacksonXmlProperty(localName = "lastCategoryNo")
    private String lastCategoryNo;              // 마지막노드 카테고리번호
  }

  /**
   * 원재료 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductRmaterial {
    @JacksonXmlProperty(localName = "rmaterialNm")
    private String rmaterialNm;                 // 원재료 상품명

    @JacksonXmlProperty(localName = "ingredNm")
    private String ingredNm;                    // 원료명

    @JacksonXmlProperty(localName = "orgnCountry")
    private String orgnCountry;                 // 원산지

    @JacksonXmlProperty(localName = "content")
    private String content;                     // 함량

    // Getters and Setters...
  }

  /**
   * 인증 그룹 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductCertGroup {
    @JacksonXmlProperty(localName = "crtfGrpTypCd")
    private String crtfGrpTypCd;                // enum, 필수

    @JacksonXmlProperty(localName = "crtfGrpObjClfCd")
    private String crtfGrpObjClfCd;             // enum, 필수

    @JacksonXmlProperty(localName = "crtfGrpExptTypCd")
    private String crtfGrpExptTypCd;            // enum, 필수

    @JacksonXmlProperty(localName = "ProductCert")
    private ProductCert productCert;            // object, 필수

    /**
     * 인증 정보
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductCert {
      @JacksonXmlProperty(localName = "certTypeCd")
      private String certTypeCd;              // enum, 필수

      @JacksonXmlProperty(localName = "certKey")
      private String certKey;                 // string, 필수

      // Getters and Setters...
    }

    // Getters and Setters...
  }

  /**
   * 의료기기 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductMedical {
    @JacksonXmlProperty(localName = "medNum1")
    private String medNum1;                     // string, 필수

    @JacksonXmlProperty(localName = "medNum2")
    private String medNum2;                     // string, 필수

    @JacksonXmlProperty(localName = "medNum3")
    private String medNum3;                     // string, 필수

    // Getters and Setters...
  }

  /**
   * 상품 옵션 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductOption {
    @JacksonXmlProperty(localName = "useYn")
    private String useYn;                       // enum, 필수

    @JacksonXmlProperty(localName = "colOptPrice")
    private String colOptPrice;                 // string, 필수

    @JacksonXmlProperty(localName = "colValue0")
    private String colValue0;                   // string, 필수

    @JacksonXmlProperty(localName = "colCount")
    private String colCount;                    // string, 필수

    @JacksonXmlProperty(localName = "colSellerStockCd")
    private String colSellerStockCd;            // string, 필수

    @JacksonXmlProperty(localName = "optionImage")
    private String optionImage;                 // string, 선택

    // Getters and Setters...
  }

  /**
   * 루트 옵션 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductRootOption {
    @JacksonXmlProperty(localName = "colTitle")
    private String colTitle;                    // string, 필수

    @JacksonXmlProperty(localName = "ProductOption")
    private ProductOption productOption;        // object, 필수

    /**
     * 루트 옵션의 ProductOption (구조가 약간 다름)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductOption {
      @JacksonXmlProperty(localName = "colOptPrice")
      private String colOptPrice;             // string, 필수

      @JacksonXmlProperty(localName = "colValue0")
      private String colValue0;               // string, 필수

      @JacksonXmlProperty(localName = "optionImage")
      private String optionImage;             // string, 선택

      // Getters and Setters...
    }

    // Getters and Setters...
  }

  /**
   * 확장 옵션 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductOptionExt {
    @JacksonXmlProperty(localName = "ProductOption")
    private ProductOption productOption;        // object, 필수

    /**
     * 확장 옵션의 ProductOption
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductOption {
      @JacksonXmlProperty(localName = "useYn")
      private String useYn;                   // enum, 필수

      @JacksonXmlProperty(localName = "colOptPrice")
      private String colOptPrice;             // string, 필수

      @JacksonXmlProperty(localName = "colOptCount")
      private String colOptCount;             // string, 필수

      @JacksonXmlProperty(localName = "colCount")
      private String colCount;                // string, 필수

      @JacksonXmlProperty(localName = "optWght")
      private String optWght;                 // string, 필수

      @JacksonXmlProperty(localName = "colSellerStockCd")
      private String colSellerStockCd;        // string, 필수

      @JacksonXmlProperty(localName = "optionMappingKey")
      private String optionMappingKey;        // string, 필수

      @JacksonXmlProperty(localName = "optionImage")
      private String optionImage;             // string, 선택

      // Getters and Setters...
    }

    // Getters and Setters...
  }

  /**
   * 커스텀 옵션 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductCustOption {
    @JacksonXmlProperty(localName = "colOptName")
    private String colOptName;                  // string, 필수

    @JacksonXmlProperty(localName = "colOptUseYn")
    private String colOptUseYn;                 // enum, 필수

    // Getters and Setters...
  }

  /**
   * 구성품 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductComponent {
    @JacksonXmlProperty(localName = "addPrdGrpNm")
    private String addPrdGrpNm;                 // string, 필수

    @JacksonXmlProperty(localName = "compPrdNm")
    private String compPrdNm;                   // string, 필수

    @JacksonXmlProperty(localName = "sellerAddPrdCd")
    private String sellerAddPrdCd;              // string, 필수

    @JacksonXmlProperty(localName = "addCompPrc")
    private String addCompPrc;                  // string, 필수

    @JacksonXmlProperty(localName = "compPrdQty")
    private String compPrdQty;                  // string, 필수

    @JacksonXmlProperty(localName = "compPrdVatCd")
    private String compPrdVatCd;                // enum, 필수

    @JacksonXmlProperty(localName = "addUseYn")
    private String addUseYn;                    // enum, 필수

    @JacksonXmlProperty(localName = "addPrdWght")
    private String addPrdWght;                  // string, 필수

    // Getters and Setters...
  }

  /**
   * 선물 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductGift {
    @JacksonXmlProperty(localName = "giftInfo")
    private String giftInfo;                    // string, 필수

    @JacksonXmlProperty(localName = "giftNm")
    private String giftNm;                      // string, 필수

    @JacksonXmlProperty(localName = "aplBgnDt")
    private String aplBgnDt;                    // string, 필수

    @JacksonXmlProperty(localName = "aplEndDt")
    private String aplEndDt;                    // string, 필수

    // Getters and Setters...
  }

  /**
   * 상품 고시 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductNotification {
    @JacksonXmlProperty(localName = "type")
    private String type;                        // string, 필수

    @JacksonXmlProperty(localName = "item")
    private Item item;                          // object, 필수

    /**
     * 아이템 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item {
      @JacksonXmlProperty(localName = "code")
      private String code;                    // string, 필수

      @JacksonXmlProperty(localName = "name")
      private String name;                    // string, 필수

      // Getters and Setters...
    }

    // Getters and Setters...
  }

  /**
   * 태그 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductTag {
    @JacksonXmlProperty(localName = "tagName")
    private String tagName;                     // string, 선택

    // Getters and Setters...
  }

  /**
   * 럭셔리 상품 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductLuxury {

    @JacksonXmlProperty(localName = "grade")
    private String grade;                       // enum, 선택

    @JacksonXmlProperty(localName = "Item")
    private Item item;                          // object, 선택
    /**
     * 럭셔리 아이템 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item {
      @JacksonXmlProperty(localName = "code")
      private String code;                    // enum, 선택
    }
  }
}
