package com.jason.purchase_agent.enums;

/*
 * 사용자 권한 수준을 정의하는 열거형(Enum)
 *
 * Enum을 사용하는 이유:
 * 1. 타입 안전성: 정의된 값들만 사용 가능 (오타나 잘못된 값 입력 방지)
 * 2. 코드 가독성: PENDING, USER, ADMIN이 각각 무엇을 의미하는지 명확
 * 3. 유지보수: 권한 종류 추가/변경 시 한 곳에서만 수정하면 됨
 */

/**
 * UserRole: 사용자의 권한 수준을 나타내는 열거형
 * Spring Security와 연동되어 접근 제어에 사용됨
 */

/**
 * 사용자 권한 등급을 나타내는 열거형
 * - Spring Security의 Role과 매핑되어 접근 제어에 사용
 * - 관리자 승인 워크플로우의 상태 관리
 */
public enum UserRole {
    /**
     * USER: 승인 완료된 일반 사용자
     * - 관리자 승인 후 부여되는 권한
     * - 일반 서비스 기능 모두 이용 가능
     * - Spring Security: ROLE_USER
     */
    USER,

    /**
     * ADMIN: 시스템 관리자
     * - 최고 권한 (일반 기능 + 관리자 기능)
     * - 다른 사용자 승인/거부 권한
     * - Spring Security: ROLE_ADMIN
     */
    ADMIN
}