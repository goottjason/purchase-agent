package com.jason.purchase_agent.security;

import com.jason.purchase_agent.entity.User;
import com.jason.purchase_agent.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * OAuth2 로그인 사용자를 Spring Security 인증 시스템에 맞게 래핑하는 클래스
 *
 * 역할:
 * 1. OAuth2User 인터페이스 구현으로 Spring Security와 호환
 * 2. 우리 시스템의 User 엔티티 정보를 OAuth2 인증 객체에 포함
 * 3. 사용자 권한(Role)을 Spring Security GrantedAuthority로 변환
 * 4. 승인 상태에 따른 권한 제어
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final User user;                    // 우리 시스템의 사용자 엔티티
    private final Map<String, Object> attributes; // 카카오에서 받은 원본 사용자 정보

    /**
     * CustomOAuth2User 생성자
     *
     * @param user 우리 시스템의 User 엔티티
     * @param attributes 카카오에서 받은 사용자 속성 정보
     */
    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    /**
     * 카카오에서 받은 원본 사용자 속성 반환
     * - OAuth2 표준에 따라 제공되는 메서드
     * - 카카오 사용자 정보 전체에 접근 가능
     *
     * @return Map<String, Object> 카카오 사용자 속성
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Spring Security 권한 시스템에 사용될 권한 목록 반환
     * - 사용자의 승인 상태와 역할에 따라 적절한 권한 부여
     * - PENDING 사용자는 승인 대기 페이지만 접근 가능
     * - 승인된 사용자는 role에 따른 전체 권한 부여
     *
     * @return Collection<? extends GrantedAuthority> Spring Security 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        /*// 승인되지 않은 사용자는 PENDING 권한만 부여
        if (!user.isApproved() || user.getRole() == UserRole.PENDING) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_PENDING"));
        }*/

        // 승인된 사용자는 실제 역할에 따른 권한 부여
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    /**
     * OAuth2 표준에서 요구하는 사용자 이름 반환
     * - 일반적으로 사용자의 고유 식별자를 반환
     * - 카카오의 경우 사용자 ID를 반환
     *
     * @return String 사용자 고유 이름 (카카오 ID)
     */
    @Override
    public String getName() {
        return user.getKakaoId();
    }

    /**
     * 사용자 이메일 반환 (편의 메서드)
     * - 컨트롤러나 서비스에서 쉽게 이메일에 접근하기 위한 메서드
     *
     * @return String 사용자 이메일
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * 사용자 표시 이름 반환 (편의 메서드)
     * - 화면에 "안녕하세요, {displayName}님" 형태로 표시할 때 사용
     *
     * @return String 사용자 표시 이름 (카카오 닉네임)
     */
    public String getDisplayName() {
        return user.getName();
    }
}
