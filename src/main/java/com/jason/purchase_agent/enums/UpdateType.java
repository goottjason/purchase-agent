package com.jason.purchase_agent.enums;

// UpdateType enum 추가
public enum UpdateType {
    CRAWL_PRICE_STOCK("UPDATE_CRAWL"),
    MANUAL_PRICE_STOCK("UPDATE_MANUAL_PRICE"),
    MANUAL_ALL_FIELDS("UPDATE_MANUAL_ALL");

    private final String processType;

    UpdateType(String processType) {
        this.processType = processType;
    }

    public String getProcessType() {
        return processType;
    }
}