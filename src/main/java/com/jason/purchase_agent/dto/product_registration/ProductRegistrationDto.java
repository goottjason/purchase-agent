package com.jason.purchase_agent.dto.product_registration;

import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.external.ChannelResultDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRegistrationDto {
    private String code; // 자체상품코드
    private String link; // 제품링크
    private String brandName; // 브랜드명
    private String title; // '(패킹수량) 브랜드 한글상품명'
    private String korName;
    private String engName;
    private int unitValue; // ex. 30
    private String unit; // ex. ml
    private int packQty; // 패킹수량
    private Double buyPrice; // 구매가
    private int salePrice; // 판매가
    private Double marginRate; // 마진율 (%, 공통 속성, 프론트에서 입력)

    private int stock; // 재고 (0 or 500)
    private String detailsHtml; // 상세설명 (쿠팡 등록하면서 세팅)
    private List<String> imageLinks; // 이미지링크 (아이허브 크롤링하면서 세팅)
    private List<String> imageFiles; // 이미지 로컬파일 경로 (임시저장된 파일경로, S3 업로드 후에는 null로 세팅)
    private List<String> uploadedImageLinks; // 업로드된 이미지링크 (쿠팡 등록하면서 세팅)

    private String ingredients; // XXX 성분 (아이허브 크롤링하면서 세팅)
    private String supplementFacts; // XXX 상세설명 (아이허브 크롤링하면서 세팅)

    private String productType; // 고시정보구분 ("FOOD" 또는 "HEALTH")
    private List<String> categoryNames;
    private Integer smartstoreCategoryId;
    private Integer elevenstCategoryId;
    private List<String> smartstoreImageLinks;

    ChannelResultDto coupangResult;
    ChannelResultDto smartstoreResult;
    ChannelResultDto elevenstResult;

}
