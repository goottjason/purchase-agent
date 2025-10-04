package com.jason.purchase_agent.security;

import com.jason.purchase_agent.entity.User;
import com.jason.purchase_agent.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 로그인 성공 후 사용자 상태에 따른 리다이렉트 처리 핸들러
 *
 * 처리 로직:
 * 1. 로그인 방식 구분 (OAuth2 vs 폼 로그인)
 * 2. 사용자 승인 상태 확인
 * 3. 권한에 따른 적절한 페이지로 리다이렉트
 *
 * 이 핸들러가 필요한 이유:
 * - Spring Security 기본 동작은 모든 사용자를 메인 페이지로 보냄
 * - 승인 대기 사용자는 별도 페이지에서 대기해야 함
 * - 관리자는 관리 페이지로, 일반 사용자는 메인 페이지로 분기 필요
 */
@Slf4j
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 로그인 성공 시 호출되는 메서드
     * - Spring Security가 인증 완료 후 자동 호출
     * - OAuth2 로그인과 폼 로그인 모두에서 사용
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체 (리다이렉트에 사용)
     * @param authentication 인증된 사용자 정보
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        log.info("로그인 성공 - 사용자: {}", authentication.getName());

        // 1. 인증 객체에서 사용자 정보 추출
        User user = extractUserFromAuthentication(authentication);

        if (user == null) {
            log.error("인증 객체에서 사용자 정보를 추출할 수 없음");
            response.sendRedirect("/login?error=true");
            return;
        }

        // 2. 사용자 상태에 따른 리다이렉트 처리
        String redirectUrl = determineRedirectUrl(user);

        log.info("사용자 {} -> 리다이렉트: {}", user.getEmail(), redirectUrl);

        // 3. 리다이렉트 실행
        response.sendRedirect(redirectUrl);
    }

    /**
     * 인증 객체에서 User 엔티티 추출
     * - OAuth2 로그인과 폼 로그인의 Principal 타입이 다름
     * - 각각에 맞는 방식으로 User 객체 추출
     *
     * @param authentication Spring Security 인증 객체
     * @return User 추출된 사용자 엔티티
     */
    private User extractUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // OAuth2 로그인인 경우
        if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUser();
        }

        // 폼 로그인인 경우
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }

        log.error("알 수 없는 Principal 타입: {}", principal.getClass());
        return null;
    }

    /**
     * 사용자 상태에 따른 리다이렉트 URL 결정
     * - 승인 대기: 대기 페이지
     * - 관리자: 관리 페이지
     * - 일반 사용자: 메인 페이지
     *
     * @param user 로그인한 사용자
     * @return String 리다이렉트할 URL
     */
    private String determineRedirectUrl(User user) {

        /*// 승인되지 않은 사용자 (PENDING 상태)
        if (!user.isApproved() || user.getRole() == UserRole.PENDING) {
            log.info("승인 대기 사용자 로그인: {}", user.getEmail());
            return "/pending-approval";
        }*/

        // 관리자 사용자
        if (user.getRole() == UserRole.ADMIN) {
            log.info("관리자 로그인: {}", user.getEmail());
            return "/";  // 메인페이지
        }

        // 승인된 일반 사용자
        if (user.getRole() == UserRole.USER) {
            log.info("일반 사용자 로그인: {}", user.getEmail());
            return "/";  // 메인 페이지
        }

        // 예외 상황 (정상적이지 않은 사용자 상태)
        log.warn("비정상적인 사용자 상태 - 이메일: {}, 역할: {}, 승인: {}",
                user.getEmail(), user.getRole(), user.isApproved());
        return "/login";
    }
}
