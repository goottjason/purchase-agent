package com.jason.purchase_agent.enums;

/**
 * 로그인 방식을 구분하는 열거형
 * - 사용자가 어떤 방식으로 인증하는지 명확히 구분
 * - Spring Security 설정에서 인증 로직 분기에 활용
 */
public enum LoginType {
    /**
     * KAKAO: 카카오 OAuth2 로그인 사용자
     * - 카카오 서버에서 인증 처리
     * - 비밀번호 불필요 (카카오가 인증 담당)
     * - kakaoId 필드 사용
     */
    KAKAO,

    /**
     * LOCAL: 자체 사이트 회원가입 사용자  
     * - 자체 서버에서 인증 처리
     * - 이메일 + 비밀번호로 로그인
     * - password 필드 사용 (BCrypt 암호화)
     */
    LOCAL
}
