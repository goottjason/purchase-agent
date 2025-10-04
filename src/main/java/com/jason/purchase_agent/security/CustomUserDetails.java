package com.jason.purchase_agent.security;

import com.jason.purchase_agent.entity.User;
import com.jason.purchase_agent.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 자체 로그인 사용자를 Spring Security 인증 시스템에 맞게 래핑하는 클래스
 *
 * 역할:
 * 1. UserDetails 인터페이스 구현으로 Spring Security와 호환
 * 2. 우리 시스템의 User 엔티티를 Spring Security 인증 객체로 변환
 * 3. 계정 상태 관리 (활성화, 만료, 잠금 등)
 * 4. 권한 정보 제공
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    /**
     * 사용자의 권한 목록 반환
     * - Spring Security 접근 제어에 사용
     * - 승인 상태에 따라 적절한 권한 부여
     *
     * @return Collection<? extends GrantedAuthority> 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        /*// 승인되지 않은 사용자는 PENDING 권한만
        if (!user.isApproved() || user.getRole() == UserRole.PENDING) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_PENDING"));
        }*/

        // 승인된 사용자는 실제 역할 권한 부여
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    /**
     * 인증에 사용할 비밀번호 반환
     * - Spring Security가 사용자 입력 비밀번호와 비교할 값
     * - 데이터베이스에 BCrypt로 암호화되어 저장된 비밀번호
     *
     * @return String 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자 식별자 반환 (로그인 ID)
     * - 사용자가 로그인 시 입력하는 username
     * - 우리 시스템에서는 이메일을 username으로 사용
     *
     * @return String 사용자 이메일
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * 계정 만료 여부 확인
     * - true: 계정이 만료되지 않음 (정상 상태)
     * - false: 계정 만료됨 (로그인 불가)
     *
     * 현재는 모든 계정을 만료되지 않은 상태로 처리
     * 향후 계정 만료 기능 추가 시 user.getExpiredAt() 등으로 변경 가능
     *
     * @return boolean 계정 만료되지 않았으면 true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부 확인
     * - true: 계정이 잠금되지 않음 (정상 상태)
     * - false: 계정 잠금됨 (로그인 불가)
     *
     * 현재는 모든 계정을 잠금되지 않은 상태로 처리
     * 향후 로그인 시도 횟수 제한 등의 기능 추가 시 활용
     *
     * @return boolean 계정이 잠금되지 않았으면 true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격증명(비밀번호) 만료 여부 확인
     * - true: 비밀번호가 만료되지 않음 (정상 상태)
     * - false: 비밀번호 만료됨 (비밀번호 변경 필요)
     *
     * 현재는 모든 비밀번호를 만료되지 않은 상태로 처리
     * 향후 비밀번호 정기 변경 정책 추가 시 활용
     *
     * @return boolean 비밀번호가 만료되지 않았으면 true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부 확인
     * - true: 활성화된 계정 (로그인 가능)
     * - false: 비활성화된 계정 (로그인 불가)
     *
     * 관리자 승인과는 별개로 계정 자체의 활성화 상태
     * 승인되지 않아도 계정은 활성화 상태 (승인 대기 페이지 접근을 위해)
     *
     * @return boolean 항상 true (모든 계정 활성화)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 사용자 이름 반환 (편의 메서드)
     * - 화면 표시용 사용자 이름
     *
     * @return String 사용자 이름
     */
    public String getDisplayName() {
        return user.getName();
    }
}
