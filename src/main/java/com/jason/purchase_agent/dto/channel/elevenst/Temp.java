package com.jason.purchase_agent.dto.channel.elevenst;

public class Temp {
}

/*

 */
/**
 * 11번가 상품 등록 요청 DTO
 * 카테고리조회하여 특정값 기입 -> 상품등록 요청
 *//*

public class ElevenstListingEnrollRequest {

  // 기본 상품 정보
  private String abrdBuyPlace = "D";              // ◎해외상품코드, enum, 선택, D(현지 온라인 쇼핑몰)
  private String abrdSizetableDispYn;             // ○해외사이즈 조견표 노출여부, enum, 선택, 국내셀러는 생략
  private String selMnbdNckNm;                    // ○닉네임, string, 선택, 생략하면 대표닉네임 자동등록
  private String selMthdCd = "01";                // ◎판매방식, enum, 필수, 01(고정가 판매)
  private String dispCtgrNo;                      // ★카테고리번호, string, 필수, 카테고리 조회 후 기입
                                                  // 세카테고리를 입력하셔야 하며 세카테고리가 없는 경우 소카테고리를 입력
  private String PartnerCategory;                 // ○제휴사카테고리, string, 선택, 생략
  private String leafCategoryYn;                  // ○리프카테고리 여부, enum, 선택, 생략
  private String categoryName1;                   // ○대분류명, string, 선택, 생략
  private String categoryName2;                   // ○중분류명, string, 선택, 생략
  private String categoryName3;                   // ○소분류명, string, 선택, 생략
  private String categoryName4;                   // ○세분류명, string, 선택, 생략
  private String lastCategoryNo;                  // ○마지막노드 카테고리번호, string, 선택, 생략
  private String prdTypCd = "01";                 // ◎서비스 상품코드, enum, 필수, 01(일반상품)
  private String hsCode;                          // ○H.S Code, string, 선택, 생략
  private String prdNm;                           // ●상품명, string, 필수, 100Byte로 제한 (한글 50자)
  private String prdNmEng;                        // ●영문 상품명, string, 선택, 100Byte로 제한 (영문 100자)
  private String advrtStmt;                       // ○상품홍보문구, string, 선택
  private String brand;                           // ●브랜드, string, 필수
  private String apiPrdAttrBrandCd;               // ○브랜드코드, string, 선택
  private String rmaterialTypCd = "05";           // ◎원재료 유형 코드, enum, 필수, 05(상품별 원산지는 상세설명 참조)
  private String orgnTypCd = "03";                // ◎원산지 코드, enum, 필수, 03(기타)
  private String orgnDifferentYn;                 // ○원산지가 다른 상품 같이 등록, string, 선택
  private String orgnTypDtlsCd;                   // ○원산지 지역 코드, string, 선택
  private String orgnNmVal = "상세설명 참조";     // ◎원산지명, string, 선택
  private ProductRmaterial ProductRmaterial;      // ○원재료정보, object, 선택
  private String beefTraceStat;                   // ○축산물 이력번호, enum, 필수 (식품> 축산 > 수입쇠고기 카테고리 선택 시)
  private String beefTraceNo;                     // ○이력번호 표시대상 제품, string, 선택

  // 판매 관련 정보
  private String sellerPrdCd;                     // ●판매자 상품코드, string, 선택
  private String suplDtyfrPrdClfCd = "01";        // ◎부가세/면세상품코드, enum, 필수
  private String yearEndTaxYn;                    // ○연말정산 소득공제 여부, enum, 선택
  private String forAbrdBuyClf = "02";            // ◎해외구매대행상품 여부, enum, 필수, 02(해외판매대행상품)
  private String importFeeCd = "01";              // ◎관부가세 포함 여부, enum, 선택, 01(포함)
  private String prdStatCd = "01";                // ◎상품상태, enum, 필수, 01(새상품)
  private String useMon;                          // ○사용개월수, string, 선택
  private String gradeRefur;                      // ○리퍼 상품 등급, enum, 선택
  private String paidSelPrc;                      // ○구입당시 판매가, string, 선택
  private String exteriorSpecialNote;             // ○외관/기능상 특이사항, string, 선택
  private String minorSelCnYn = "Y";              // ◎미성년자 구매가능, enum, 필수, Y(가능)

  // 이미지 정보
  private String prdImage01;                      // ●대표 이미지 URL, string, 필수, 11번가 서버가 다운로드하여 600 x 600 사이즈로 리사이징
  private String prdImage02;                      // ○추가 이미지 1 URL, string, 선택
  private String prdImage03;                      // ○추가 이미지 2 URL, string, 선택
  private String prdImage04;                      // ○추가 이미지 3 URL, string, 선택
  private String prdImage09;                      // ○카드뷰 이미지2, string, 선택
  private String htmlDetail;                      // ○상세설명, string, 필수, html 을 입력하실 경우 <![CDATA[ ]]> 로 묶음

  // 인증 관련 정보
  private ProductCertGroup productCertGroup;      // ●인증정보그룹, object, 필수
  private ProductMedical productMedical;          // ○의료기기 품목허가, object, 선택

  // 리뷰 관련
  private String reviewDispYn = "Y";              // ◎reviewDispYn, string, 선택
  private String reviewOptDispYn = "Y";           // ◎상품리뷰/후기 옵션 노출여부, string, 선택

  // 판매 조건 관련 ★★★★★
  private String selPrdClfCd = "0:100";           // 판매기간코드/예약기간코드, enum, 선택, 0:100(판매기간 직접입력)
  private String aplBgnDy;                        // 판매 시작일/예약 시작일, string, 선택
  private String aplEndDy;                        // 판매 종료일/예약 종료일, string, 선택
  private String setFpSelTermYn = "N";            // 예약판매 상품의 고정가 판매기간 설정, enum, 선택, N(설정안함)
  private String selTermUseYn = "N";              // 판매기간, enum, 선택
  private String selPrdClfFpCd;                   // 판매기간코드, enum, 선택

  private String wrhsPlnDy;                       // ○입고예정일, string, 선택
  private String contractCd;                      // ○약정코드, enum, 선택
  private String chargeCd;                        // ○요금제 코드, string, 선택
  private String periodCd;                        // ○약정기간 코드, enum, 선택

  // 가격 정보
  private String phonePrc;                        // ○단말기 출고 가격, string, 선택
  private String maktPrc;                         // ○정가, string, 선택
  private String recommendRetailPrice;            // ○권장소비자가, string, 선택
  private String selPrc;                          // ●판매가, string, 필수, 판매가는 10원 단위로, 최대 10억 원 미만으로 입력 가능합니다. 판매가 정보 수정 시, 최대 50% 인상/80% 인하까지 수정

  // 쿠폰 관련
  private String cuponcheck = "Y";                // ◎기본즉시할인 설정여부, enum, 선택 (S : 기존값 유지(상품수정))
  private String dscAmtPercnt = "38";             // ◎할인수치, string, 선택
  private String cupnDscMthdCd = "02";            // ◎할인단위 코드, enum, 선택
  private String cupnUseLmtDyYn = "N";            // ◎할인 적용기간 설정여부, enum, 선택
  private String cupnIssEndDy;                    // ○할인적용기간 종료일string, 선택

  // 페이 관련
  private String pay11YN;                         // ○11pay 포인트 지급 설정여부, enum, 선택
  private String pay11Value;                      // ○적립수치, string, 선택
  private String pay11WyCd;                       // ○적립단위 코드, enum, 선택

  // 할부 관련
  private String intFreeYN;                       // ○무이자 할부 제공 설정여부, enum, 선택
  private String intfreeMonClfCd;                 // ○개월수, enum, 선택

  // 플러스 할인 관련
  private String pluYN;                           // ○복수구매할인 설정 여부, enum, 선택
  private String pluDscCd;                        // ○복수구매할인 설정 기준, enum, 선택
  private String pluDscBasis;                     // ○복수구매할인 기준 금액 및 수량, string, 선택
  private String pluDscAmtPercnt;                 // ○복수구매할인 금액/율, string, 선택
  private String pluDscMthdCd;                    // ○복수구매할인 구분코드, enum, 선택
  private String pluUseLmtDyYn;                   // ○복수구매할인 적용기간 설정, enum, 선택
  private String pluIssStartDy;                   // ○복수구매할인 적용기간 시작일, string, 선택
  private String pluIssEndDy;                     // ○복수구매할인 적용기간 종료일, string, 선택

  // 희망 배송 관련
  private String hopeShpYn;                       // ○희망후원 지급 설정 여부, enum, 선택
  private String hopeShpPnt;                      // ○적립수치, string, 선택
  private String hopeShpWyCd;                     // ○적립단위 코드, enum, 선택

  // 옵션 관련
  private String optSelectYn;                     // ○선택형 옵션 여부, string, 선택
  private String txtColCnt;                       // ○고정값, string, 선택
  private String optionAllQty;                    // ○멀티옵션 일괄재고수량 설정, string, 선택
  private String optionAllAddPrc;                 // ○멀티옵션 옵션가 0원 설정, string, 선택
  private String optionAllAddWght;                // ○멀티옵션 일괄옵션추가무게 설정, string, 필수
  private String prdExposeClfCd;                  // ○상품상세 옵션값 노출 방식 선택, enum, 선택
  private String optMixYn;                        // ○전체옵션 조합여부, enum, 선택

  private List<ProductOption> productOptions;     // ProductOption, list, 선택
  private List<ProductRootOption> productRootOptions; // ProductRootOption, list, 선택
  private List<ProductOptionExt> productOptionExts; // ProductOptionExt, list, 선택
  private List<ProductCustOption> productCustOptions; // list, 선택
  private List<ProductComponent> productComponents; // list, 선택

  // 판매 수량 제한
  private String prdSelQty;                       // string, 선택
  private String selMinLimitTypCd;                // enum, 선택
  private String selMinLimitQty;                  // string, 선택
  private String selLimitTypCd;                   // enum, 선택
  private String selLimitQty;                     // string, 선택
  private String townSelLmtDy;                    // string, 선택

  // 선물 관련
  private String useGiftYn;                       // enum, 선택
  private List<ProductGift> productGifts;         // list, 선택

  // 배송 관련 정보
  private String dlvCnAreaCd;                     // enum, 필수
  private String dlvWyCd;                         // enum, 필수
  private String dlvEtprsCd;                      // enum, 선택
  private String dlvSendCloseTmpltNo;             // string, 필수
  private String dlvCstInstBasiCd;                // enum, 필수
  private String dlvCst1;                         // string, 선택
  private String dlvCst3;                         // string, 선택
  private String dlvCst4;                         // string, 선택
  private String dlvCstInfoCd;                    // enum, 선택
  private String prdFrDlvBasiAmt;                 // string, 필수
  private String dlvCnt1;                         // string, 필수
  private String dlvCnt2;                         // string, 필수
  private String bndlDlvCnYn;                     // enum, 필수
  private String dlvCstPayTypCd;                  // enum, 필수
  private String jejuDlvCst;                      // string, 필수
  private String islandDlvCst;                    // string, 필수
  private String addrSeqOut;                      // string, 필수
  private String outsideYnOut;                    // enum, 선택
  private String visitDlvYn;                      // string, 선택
  private String visitAddrSeq;                    // string, 선택
  private String addrSeqOutMemNo;                 // string, 선택
  private String addrSeqIn;                       // string, 필수
  private String outsideYnIn;                     // enum, 선택
  private String addrSeqInMemNo;                  // string, 선택
  private String abrdCnDlvCst;                    // string, 선택
  private String rtngdDlvCst;                     // string, 필수
  private String exchDlvCst;                      // string, 필수
  private String rtngdDlvCd;                      // enum, 선택
  private String asDetail;                        // string, 필수
  private String rtngExchDetail;                  // string, 필수
  private String dlvClf;                          // enum, 필수
  private String abrdInCd;                        // enum, 필수
  private String prdWght;                         // string, 필수
  private String ntShortNm;                       // string, 필수
  private String globalOutAddrSeq;                // string, 필수
  private String mbAddrLocation05;                // enum, 필수
  private String globalInAddrSeq;                 // string, 필수
  private String mbAddrLocation06;                // enum, 필수

  // 제조/유효 일자
  private String mnfcDy;                          // string, 선택
  private String eftvDy;                          // string, 선택

  // 상품 고시 정보
  private List<ProductNotification> productNotifications; // list, 필수

  // 기타
  private String bcktExYn;                        // string, 선택
  private String prcCmpExpYn;                     // enum, 선택
  private String prcDscCmpExpYn;                  // enum, 선택
  private String martCPSAgreeYn;                  // enum, 선택
  private String stdPrdYn;                        // string, 선택

  private List<ProductTag> productTags;           // list, 선택
  */
/*private ProductLuxury productLuxury;            // 우아(OOAh)서비스 상품 항목, object, 선택*//*




  // Getters and Setters
  // ... (각 필드에 대한 getter/setter 메소드들)


  public static class ProductRmaterial {
    private String rmaterialNm;                     // ○원재료 상품명, string, 필수
    private String ingredNm;                        // ○원료명, string, 필수
    private String orgnCountry;                     // ○원산지, string, 필수
    private String content;                         // ○함량, string, 필수
  }

  */
/**
 * 인증 그룹 정보
 *//*

  public static class ProductCertGroup {
    private String crtfGrpTypCd;                // ○인증정보그룹번호, enum, 필수
    private String crtfGrpObjClfCd;             // ○KC인증대상여부, enum, 필수
    private String crtfGrpExptTypCd;            // ○KC면제유형, enum, 필수
    private List<ProductCert> productCert;      // ●인증정보, list, 필수

    // Getters and Setters
    // ...
  }

  */
/**
 * 인증 정보
 *//*

  public static class ProductCert {
    private String certTypeCd = "131";                  // ◎인증유형, enum, 필수
    private String certKey;                             // ○인증번호, string, 필수

    // Getters and Setters
    // ...
  }

  */
/**
 * 의료기기 정보
 *//*

  public static class ProductMedical {
    private String medNum1;                     // ○의료기기 품목허가번호, string, 필수
    private String medNum2;                     // ○의료기기 판매업신고 기관 및 번호, string, 필수
    private String medNum3;                     // ○의료기기사전광고심의번호, string, 필수

    // Getters and Setters
    // ...
  }

  */
/**
 * 상품 옵션 정보
 *//*

  public static class ProductOption {
    private String useYn;                       // enum, 필수
    private String colOptPrice;                 // string, 필수
    private String colValue0;                   // string, 필수
    private String colCount;                    // string, 필수
    private String colSellerStockCd;            // string, 필수
    private String optionImage;                 // string, 선택

    // Getters and Setters
    // ...
  }

  */
/**
 * 루트 옵션 정보
 *//*

  public static class ProductRootOption {
    private String colTitle;                    // string, 필수
    private List<ProductOption> productOptions; // list, 필수

    // Getters and Setters
    // ...
  }

  */
/**
 * 확장 옵션 정보
 *//*

  public static class ProductOptionExt {
    private String useYn;                       // enum, 필수
    private String colOptPrice;                 // string, 필수
    private String colOptCount;                 // string, 필수
    private String colCount;                    // string, 필수
    private String optWght;                     // string, 필수
    private String colSellerStockCd;            // string, 필수
    private String optionMappingKey;            // string, 필수
    private String optionImage;                 // string, 선택

    // Getters and Setters
    // ...
  }

  */
/**
 * 커스텀 옵션 정보
 *//*

  public static class ProductCustOption {
    private String colOptName;                  // string, 필수
    private String colOptUseYn;                 // enum, 필수
    private String useOptCalc;                  // string, 선택
    private String optCalcTranType;             // enum, 선택
    private String optTypCd;                    // string, 선택
    private String optItem1Nm;                  // string, 선택
    private String optItem1MinValue;            // string, 선택
    private String optItem1MaxValue;            // string, 선택
    private String optItem2Nm;                  // string, 선택
    private String optItem2MinValue;            // string, 선택
    private String optItem2MaxValue;            // string, 선택
    private String optUnitPrc;                  // string, 선택
    private String optUnitCd;                   // enum, 선택
    private String optSelUnit;                  // string, 선택

    // Getters and Setters
    // ...
  }

  */
/**
 * 구성품 정보
 *//*

  public static class ProductComponent {
    private String addPrdGrpNm;                 // string, 필수
    private String compPrdNm;                   // string, 필수
    private String sellerAddPrdCd;              // string, 필수
    private String addCompPrc;                  // string, 필수
    private String compPrdQty;                  // string, 필수
    private String compPrdVatCd;                // enum, 필수
    private String addUseYn;                    // enum, 필수
    private String addPrdWght;                  // string, 필수

    // Getters and Setters
    // ...
  }

  */
/**
 * 선물 정보
 *//*

  public static class ProductGift {
    private String giftInfo;                    // string, 필수
    private String giftNm;                      // string, 필수
    private String aplBgnDt;                    // string, 필수
    private String aplEndDt;                    // string, 필수
    private String gftPackTypCd;                // enum, 선택

    // Getters and Setters
    // ...
  }

  */
/**
 * 상품 고시 정보
 *//*

  public static class ProductNotification {
    private String type;                        // string, 필수
    private List<Item> items;                   // list, 필수
    private String company;                     // string, 선택
    private String modelNm;                     // string, 선택
    private String modelCd;                     // string, 선택
    private String mnfcDy;                      // string, 선택
    private String mainTitle;                   // string, 선택
    private String artist;                      // string, 선택
    private String mudvdLabel;                  // string, 선택
    private String maker;                       // string, 선택
    private String albumNm;                     // string, 선택
    private String dvdTitle;                    // string, 선택

    // Getters and Setters
    // ...
  }

  */
/**
 * 아이템 정보
 *//*

  public static class Item {
    private String code;                        // string, 필수
    private String name;                        // string, 필수

    // Getters and Setters
    // ...
  }

  */
/**
 * 태그 정보
 *//*

  public static class ProductTag {
    private String tagName;                     // string, 선택

    // Getters and Setters
    // ...
  }

  */
/*public static class ProductLuxury {
    private String grade;                       // enum, 선택
    private String Item;                     // string, 선택
  }*//*


}
*/