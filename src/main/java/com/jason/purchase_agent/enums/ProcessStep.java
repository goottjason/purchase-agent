package com.jason.purchase_agent.enums;

// 처리 단계 정의
public enum ProcessStep {
    // 공통 단계
    INIT("초기화"),
    DATA_PREPARATION("데이터 준비"),  // 크롤링 또는 사용자 입력
    VALIDATION("검증"),
    DB_SAVE("DB 저장"),
    CHANNEL_UPDATE("채널 업데이트"),  // 조건부
    COMPLETED("완료");

    private final String description;

    ProcessStep(String description) {
        this.description = description;
    }
}
