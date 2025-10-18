package com.jason.purchase_agent.enums;

// 작업 유형 정의
public enum JobType {
    REGISTER_PRODUCT("상품등록"),
    CRAWL_AND_UPDATE_PRICE_STOCK("크롤링 후 가격/재고 업데이트"),
    MANUAL_UPDATE_PRICE_STOCK("수동 가격/재고 수정"),
    MANUAL_UPDATE_ALL_FIELDS("전체 상품 정보 수정");

    private final String description;

    JobType(String description) {
        this.description = description;
    }
}
