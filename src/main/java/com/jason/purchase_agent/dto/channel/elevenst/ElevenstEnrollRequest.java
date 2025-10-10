package com.jason.purchase_agent.dto.channel.elevenst;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
//

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 생략시 아예 필드가 나타나지 않음
@JacksonXmlRootElement(localName = "Product")
public class ElevenstEnrollRequest {
    // ◎해외상품코드, D(현지온라인쇼핑몰)
    @Builder.Default private String abrdBuyPlace = "D";
    // ◎해외사이즈 조견표 노출여부, N(노출안함)
    @Builder.Default private String abrdSizetableDispYn = "N";
    // ○닉네임
    private String selMnbdNckNm;
    // ◎판매방식, 01(고정가 판매)
    @Builder.Default private String selMthdCd = "01";
    // ●카테고리번호
    @NotNull private String dispCtgrNo;
    // □제휴사카테고리
    private PartnerCategory partnerCategory;
    // ◎서비스 상품코드,  01(일반상품)
    @Builder.Default private String prdTypCd = "01";
    // ○H.S Code (조건부 배송일때만 필수)
    private String hsCode;
    // ●상품명
    @NotNull private String prdNm;
    // ●영문 상품명
    @NotNull private String prdNmEng;
    // ◎상품홍보문구
    @Builder.Default private String advrtStmt = "100% 정품 보장";
    // ●브랜드
    @NotNull private String brand;
    // ○브랜드코드
    private String apiPrdAttrBrandCd;
    // ◎원재료 유형 코드, 05(상품별 원산지는 상세설명 참조)
    @Builder.Default private String rmaterialTypCd = "05";
    // ◎원산지 코드, 03(기타)
    @Builder.Default private String orgnTypCd = "03";
    // ○원산지가 다른 상품 같이 등록 (원산지가 한 군데 이상)
    private String orgnDifferentYn;
    // ○원산지 지역 코드 (기타는 생략 가능)
    private String orgnTypDtlsCd;
    // ◎원산지명
    @Builder.Default private String orgnNmVal = "상세설명 참조";
    // □원재료 정보
    private ProductRmaterial productRmaterial;
    // ○축산물 이력번호
    private String beefTraceStat;
    // ○이력번호 표시대상 제품
    private String beefTraceNo;
    // ●판매자 상품코드
    @NotNull private String sellerPrdCd;
    // ◎부가세/면세상품코드(01 : 과세상품)
    @Builder.Default private String suplDtyfrPrdClfCd = "01";
    // ○연말정산 소득공제 여부
    private String yearEndTaxYn;
    // ◎해외구매대행상품 여부
    @Builder.Default private String forAbrdBuyClf = "02";
    // ◎관부가세 포함 여부
    @Builder.Default private String importFeeCd = "01";
    // ◎상품상태
    @Builder.Default private String prdStatCd = "01";
    // ○사용개월수
    private String useMon;
    // ○리퍼상품 등급
    private String gradeRefur;
    // ○구입당시 판매가(중고판매의 경우)
    private String paidSelPrc;
    // ○외관/기능상 특이사항
    private String exteriorSpecialNote;
    // ◎미성년자 구매가능
    @Builder.Default private String minorSelCnYn = "Y";
    // ●대표 이미지 URL ("Content-Type" 정의 필수)
    @NotNull private String prdImage01;
    // ●추가 이미지 1 URL
    private String prdImage02;
    // ●추가 이미지 2 URL
    private String prdImage03;
    // ●추가 이미지 3 URL
    private String prdImage04;
    // ○카드뷰 이미지2
    private String prdImage09;
    // ●상세설명 (html 을 입력하실 경우 <![CDATA[ ]]> 로 묶을 것)
    @NotNull private String htmlDetail;
    // □인증 관련 정보
    private ProductCertGroup productCertGroup;
    // □의료기기 품목허가
    private ProductMedical productMedical;
    // ◎상품리뷰/후기 전시여부
    @Builder.Default private String reviewDispYn = "Y";
    // ◎상품리뷰/후기 옵션 노출여부
    @Builder.Default private String reviewOptDispYn = "Y";
    // ◎판매기간코드/예약기간코드
    @Builder.Default private String selPrdClfCd = "3y:110";
    // ○판매 시작일/예약 시작일
    private String aplBgnDy;
    // ○판매 종료일/예약 종료일
    private String aplEndDy;
    // ◎예약판매 상품의 고정가 판매기간 설정
    @Builder.Default private String setFpSelTermYn = "N";
    // ◎판매기간
    @Builder.Default private String selTermUseYn = "N";
    // ○판매기간코드 (고정가 판매기간 설정 - Y인 경우만 사용가능)
    private String selPrdClfFpCd;
    // ○입고예정일
    private String wrhsPlnDy;
    // ○약정코드(휴대폰)
    private String contractCd;
    // ○요금제 코드(휴대폰)
    private String chargeCd;
    // ○약정기간 코드(휴대폰)
    private String periodCd;
    // ○단말기 출고 가격(휴대폰)
    private String phonePrc;
    // ○정가(도서)
    private String maktPrc;
    // ○권장소비자가
    private String recommendRetailPrice;
    // ●판매가
    private String selPrc;
    // ○기본즉시할인 설정여부
    private String cuponcheck;
    // ○할인수치
    private String dscAmtPercnt;
    // ○할인단위 코드
    private String cupnDscMthdCd;
    // ○할인 적용기간 설정여부
    private String cupnUseLmtDyYn;
    // ○할인적용기간 종료일
    private String cupnIssEndDy;
    // ○11pay 포인트 지급 설정여부
    private String pay11YN;
    // ○적립수치
    private String pay11Value;
    // ○적립단위 코드
    private String pay11WyCd;
    // ○무이자 할부 제공 설정여부
    private String intFreeYN;
    // ○개월수
    private String intfreeMonClfCd;
    // ○복수구매할인 설정 여부
    private String pluYN;
    // ○복수구매할인 설정 기준
    private String pluDscCd;
    // ○복수구매할인 기준 금액 및 수량
    private String pluDscBasis;
    // ○복수구매할인 금액/율
    private String pluDscAmtPercnt;
    // ○복수구매할인 구분코드
    private String pluDscMthdCd;
    // ○복수구매할인 적용기간 설정
    private String pluUseLmtDyYn;
    // ○복수구매할인 적용기간 시작일
    private String pluIssStartDy;
    // ○복수구매할인 적용기간 종료일
    private String pluIssEndDy;
    // ○희망후원 지급 설정 여부
    private String hopeShpYn;
    // ○적립수치
    private String hopeShpPnt;
    // ○적립단위 코드
    private String hopeShpWyCd;
    // ○선택형 옵션 여부
    private String optSelectYn;
    // ○고정값
    private String txtColCnt;
    // ○멀티옵션 일괄재고수량 설정
    private String optionAllQty;
    // ○멀티옵션 옵션가 0원 설정
    private String optionAllAddPrc;
    // ○멀티옵션 일괄옵션추가무게 설정
    private String optionAllAddWght;
    // ○상품상세 옵션값 노출 방식 선택
    private String prdExposeClfCd;
    // ○전체옵션 조합여부
    private String optMixYn;
    // □옵션 객체
    private ProductOption productOption;
    // □루트옵션 객체
    private ProductRootOption productRootOption;
    // □확장옵션 객체
    private ProductOptionExt productOptionExt;
    // □커스텀옵션 객체
    private ProductCustOption productCustOption;
    // ○계산형옵션 설정여부
    private String useOptCalc;
    // ○계산형 옵션 타입설정
    private String optCalcTranType;
    // ○계산형옵션구분값
    private String optTypCd;
    // ○첫번째 계산형 옵션명
    private String optItem1Nm;
    // ○첫번째 계산형 옵션 판매최소값
    private String optItem1MinValue;
    // ○첫번째 계산형 옵션 판매최대값
    private String optItem1MaxValue;
    // ○두번째 계산형 옵션명
    private String optItem2Nm;
    // ○두번째 계산형 옵션 판매최소값
    private String optItem2MinValue;
    // ○두번째 계산형 옵션 판매최대값
    private String optItem2MaxValue;
    // ○단가기준값
    private String optUnitPrc;
    // ○기준 단위코드
    private String optUnitCd;
    // ○판매단위-숫자
    private String optSelUnit;
    // □추가구성상품
    private ProductComponent productComponent;
    // ◎재고수량
    @Builder.Default private String prdSelQty = "500";
    // ○최소구매수량 설정코드(제한안함)
    private String selMinLimitTypCd;
    // ○최소구매수량 개수
    private String selMinLimitQty;
    // ○최대구매수량 설정코드
    private String selLimitTypCd;
    // ○최대구매수량 개수
    private String selLimitQty;
    // ○최대구매수량 재구매기간
    private String townSelLmtDy;
    // ○사은품 정보 사용여부
    private String useGiftYn;
    // ○사은품 객체
    private ProductGift productGift;
    // ○선물포장 유형코드
    private String gftPackTypCd;
    // ◎배송가능지역 코드 (01 : 전국)
    @Builder.Default private String dlvCnAreaCd = "01";
    // ◎배송방법 (01 : 택배)
    @Builder.Default private String dlvWyCd = "01";
    // ◎발송택배사 (00034 : CJ대한통운)
    @Builder.Default private String dlvEtprsCd = "00034";
    // ◎발송마감 템플릿번호 (오늘 주문완료 건 7일 내 발송처리)
    @Builder.Default private String dlvSendCloseTmpltNo = "682132";
    // ◎배송비 종류
    @Builder.Default private String dlvCstInstBasiCd = "01";
    // ○배송비
    private String dlvCst1;
    // ○수량별 차등 배송비
    private String dlvCst3;
    // ○1개당 배송비
    private String dlvCst4;
    // ○고정 배송비
    private String dlvCstInfoCd;
    // ○상품조건부 무료 상품기준금액
    private String prdFrDlvBasiAmt;
    // ○수량별 차등 기준 ~이상 수량
    private String dlvCnt1;
    // ○수량별 차등 기준 ~이하 수량
    private String dlvCnt2;
    // ◎묶음배송 여부
    @Builder.Default private String bndlDlvCnYn = "Y";
    // ◎결제방법 (03 : 선결제)
    @Builder.Default private String dlvCstPayTypCd = "03";
    // ◎제주 추가 배송비
    @Builder.Default private String jejuDlvCst = "0";
    // ◎도서산간 추가 배송비
    @Builder.Default private String islandDlvCst = "0";
    // ◎출고지 주소 코드
    @Builder.Default private String addrSeqOut = "1";
    // ◎출고지 주소 해외 여부
    @Builder.Default private String outsideYnOut = "Y";
    // ○방문수령추가
    private String visitDlvYn;
    // ○방문수령 주소 코드
    private String visitAddrSeq;
    // ○통합 ID 회원 번호
    private String addrSeqOutMemNo;
    // ◎반품/교환지 주소 코드
    @Builder.Default private String addrSeqIn = "2";
    // ◎반품/교환지 주소 해외 여부
    @Builder.Default private String outsideYnIn = "Y";
    // ○통합 ID 회원 번호
    private String addrSeqInMemNo;
    // ○해외취소 배송비
    private String abrdCnDlvCst;
    // ◎반품 배송비
    @Builder.Default private String rtngdDlvCst = "7000";
    // ◎교환 배송비(왕복)
    @Builder.Default private String exchDlvCst = "14000";
    // ◎초기배송비 무료시 부과방법
    @Builder.Default private String rtngdDlvCd = "02";
    // ◎A/S 안내
    @Builder.Default private String asDetail = "상세페이지 참조";
    // ◎반품/교환 안내
    @Builder.Default private String rtngExchDetail = "상세페이지 참조";
    // ◎배송 주체 (02 : 업체배송)
    @Builder.Default private String dlvClf = "02";
    // ○11번가 해외 입고 유형
    private String abrdInCd;
    // ○상품 무게
    private String prdWght;
    // ◎생산지국가(통관용)
    @Builder.Default private String ntShortNm = "US";
    // ◎판매자 해외 출고지 주소
    @Builder.Default private String globalOutAddrSeq = "1";
    // ◎판매자 해외 출고지 지역 정보 (02 : 해외)
    @Builder.Default private String mbAddrLocation05 = "02";
    // ◎판매자 반품/교환지 주소
    @Builder.Default private String globalInAddrSeq = "2";
    // ◎판매자 반품/교환지 지역 정보 (02 : 해외)
    @Builder.Default private String mbAddrLocation06 = "02";
    // ○제조일자
    private String mnfcDy;
    // ○유효일자
    private String eftvDy;
    // ●상품정보제공고시
    @JacksonXmlProperty(localName = "ProductNotification")
    private ProductNotification productNotification;
    // ○제조사/수입사
    private String company;
    // ○모델명
    private String modelNm;
    // ○모델코드
    private String modelCd;
    // ○원제(도서)
    private String mainTitle;
    // ○아티스트/감독(배우)
    private String artist;
    // ○음반 라벨
    private String mudvdLabel;
    // ○제조사
    private String maker;
    // ○앨범명
    private String albumNm;
    // ○DVD 타이틀
    private String dvdTitle;
    // ◎장바구니 담기 제한
    @Builder.Default private String bcktExYn = "N";
    // ◎가격비교 사이트 등록 여부
    @Builder.Default private String prcCmpExpYn = "Y";
    // ◎가격비교 사이트 할인 적용
    @Builder.Default private String prcDscCmpExpYn = "Y";
    // ◎통합마트 CPS 동의 여부
    @Builder.Default private String martCPSAgreeYn = "Y";
    // ◎신규상품여부
    @Builder.Default private String stdPrdYn = "Y";
    // □태그 목록
    private ProductTag productTag;
    // □우아(OOAh)서비스 상품 항목
    private ProductLuxury productLuxury;

    // 제휴사 카테고리
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PartnerCategory {
        private String leafCategoryYn;
        private String categoryName1;
        private String categoryName2;
        private String categoryName3;
        private String categoryName4;
        private String lastCategoryNo;
    }

    // 원재료 정보
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductRmaterial {
        private String rmaterialNm;
        private String ingredNm;
        private String orgnCountry;
        private String content;
    }

    // 인증정보그룹
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductCertGroup {
        private String crtfGrpTypCd;
        private String crtfGrpObjClfCd;
        private String crtfGrpExptTypCd;
        private ProductCert productCert;

        // 인증정보
        @Data @Builder
        @NoArgsConstructor @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class ProductCert {
            private String certTypeCd;
            private String certKey;
        }
    }

    // 의료기기 품목허가
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductMedical {
        private String medNum1;
        private String medNum2;
        private String medNum3;
    }

    // 옵션 정보
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductOption {
        private String useYn;
        private String colOptPrice;
        private String colValue0;
        private String colCount;
        private String colSellerStockCd;
        private String optionImage;
    }

    // 루트옵션 정보
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductRootOption {
        private String colTitle;
        private ProductOption productOption;

        // 루트 옵션의 ProductOption
        @Data @Builder
        @NoArgsConstructor @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class ProductOption {
            private String colOptPrice;
            private String colValue0;
            private String optionImage;
        }
    }

    // 확장 옵션 정보
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductOptionExt {
        private ProductOption productOption;

        // 확장 옵션의 ProductOption
        @Data @Builder
        @NoArgsConstructor @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class ProductOption {
            private String useYn;
            private String colOptPrice;
            private String colOptCount;
            private String colCount;
            private String optWght;
            private String colSellerStockCd;
            private String optionMappingKey;
            private String optionImage;
        }
    }

    // 커스텀 옵션 정보
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductCustOption {
        private String colOptName;
        private String colOptUseYn;
    }

    // 추가구성상품
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductComponent {
        private String addPrdGrpNm;
        private String compPrdNm;
        private String sellerAddPrdCd;
        private String addCompPrc;
        private String compPrdQty;
        private String compPrdVatCd;
        private String addUseYn;
        private String addPrdWght;
    }

    // 사은품 객체
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductGift {
        private String giftInfo;
        private String giftNm;
        private String aplBgnDt;
        private String aplEndDt;
    }

    // 상품 고시 정보
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductNotification {
        private String type;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        private List<Item> item;

        // 아이템 정보
        @Data @Builder
        @NoArgsConstructor @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Item {
            private String code;
            private String name;
        }
    }

    public static ProductNotification createFoodNotification() {
        List<ProductNotification.Item> item = Arrays.asList(
                new ProductNotification.Item("176317774", "제품명은 디테일페이지에 기재"),
                new ProductNotification.Item("176312674", "주의사항은 디테일페이지에 기재"),
                new ProductNotification.Item("176400445", "생산자 및 소재지는 디테일페이지에 기재"),
                new ProductNotification.Item("176398001", "기한은 디테일페이지에 기재"),
                new ProductNotification.Item("23756754", "상담번호는 디테일페이지에 기재"),
                new ProductNotification.Item("23757095", "성분표는 디테일페이지에 기재"),
                new ProductNotification.Item("42155152", "내용물은 디테일페이지에 기재"),
                new ProductNotification.Item("42154823", "수입식품안전관리특별법에 따른 수입신고를 필함"),
                new ProductNotification.Item("23757245", "원료명 및 함량은 디테일페이지에 기재"),
                new ProductNotification.Item("23757260", "유전자변형식품정보는 디테일페이지에 기재"),
                new ProductNotification.Item("23757000", "식품의 유형은 디테일페이지에 기재")
        );
        return ProductNotification.builder()
                .type("891031")
                .item(item)
                .build();
    }
    public static ProductNotification createHealthNotification() {
        List<ProductNotification.Item> items = Arrays.asList(
                new ProductNotification.Item("176312674", "주의사항은 디테일페이지에 기재"),
                new ProductNotification.Item("176317774", "제품명은 디테일페이지에 기재"),
                new ProductNotification.Item("42155152", "내용물은 디테일페이지에 기재"),
                new ProductNotification.Item("23756446", "주의사항은 디테일페이지에 기재"),
                new ProductNotification.Item("23755783", "기능정보는 디테일페이지에 기재"),
                new ProductNotification.Item("11906", "제조업소는 디테일페이지에 기재"),
                new ProductNotification.Item("23756963", "수입식품안전관리특별법에 따른 수입신고를 필함"),
                new ProductNotification.Item("23756754", "상담번호는 디테일페이지에 기재"),
                new ProductNotification.Item("23759747", "질병의 예방 및 치료를 위한 의약품이 아님"),
                new ProductNotification.Item("23759354", "기한은 디테일페이지에 기재"),
                new ProductNotification.Item("23757103", "영양정보는 디테일페이지에 기재"),
                new ProductNotification.Item("23757245", "원료명 및 합량은 디테일페이지에 기재"),
                new ProductNotification.Item("23757304", "유전자변형건강기능식품정보는 디테일페이지에 기재")
        );
        return ProductNotification.builder()
                .type("891032")
                .item(items)
                .build();
    }

    // 태그 정보
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductTag {
        private String tagName;
    }

    // 럭셔리 상품 정보
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductLuxury {
        private String grade;
        private Item item;

        // 럭셔리 아이템 정보
        @Data @Builder
        @NoArgsConstructor @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Item {
            private String code;
        }
    }

    public static ElevenstEnrollRequest fromForm(ProductRegistrationRequest form) {
        List<String> images = form.getUploadedImageLinks();
        String prdImage01 = images.size() > 0 ? images.get(0) : null;
        String prdImage02 = images.size() > 1 ? images.get(1) : null;
        String prdImage03 = images.size() > 2 ? images.get(2) : null;
        String prdImage04 = images.size() > 3 ? images.get(3) : null;

        // 상품 고시 정보 분기
        ElevenstEnrollRequest.ProductNotification notification = null;
        if ("FOOD".equals(form.getProductType())) {
            notification = ElevenstEnrollRequest.createFoodNotification();
        } else if ("HEALTH".equals(form.getProductType())) {
            notification = ElevenstEnrollRequest.createHealthNotification();
        }

        return ElevenstEnrollRequest.builder()
                // ● 표시된 필드만 직접 세팅
                .dispCtgrNo(String.valueOf(form.getElevenstCategoryId()))
                .prdNm(form.getTitle())
                .prdNmEng(form.getEngName())
                .brand(form.getBrandName())
                .sellerPrdCd(form.getCode())
                .prdImage01(prdImage01)
                .prdImage02(prdImage02)
                .prdImage03(prdImage03)
                .prdImage04(prdImage04)
                .htmlDetail(form.getDetailsHtml())
                .selPrc(String.valueOf(form.getSalePrice()))
                .productNotification(notification)
                // 나머지 필드는 필요시 기본값 그대로
                .build();
    }
}
