package com.jason.purchase_agent.service;

import com.jason.purchase_agent.entity.User;
import com.jason.purchase_agent.enums.LoginType;
import com.jason.purchase_agent.repository.UserRepository;
import com.jason.purchase_agent.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 자체 로그인(폼 로그인) 시 사용자 정보를 처리하는 서비스
 *
 * 처리 흐름:
 * 1. 사용자가 로그인 폼에 이메일/비밀번호 입력
 * 2. Spring Security가 이 서비스의 loadUserByUsername 호출
 * 3. 데이터베이스에서 이메일로 사용자 검색
 * 4. LOCAL 타입 사용자인지 확인
 * 5. CustomUserDetails로 래핑하여 반환
 * 6. Spring Security가 비밀번호 검증 수행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security 폼 로그인 시 호출되는 메서드
     * - 사용자가 입력한 username(이메일)으로 사용자 정보 조회
     * - 자체 회원가입한 사용자(LOCAL 타입)만 처리
     * - 카카오 로그인 사용자는 이 방식으로 로그인 불가
     *
     * @param username 사용자가 입력한 이메일 (로그인 폼의 username 필드)
     * @return UserDetails Spring Security가 비밀번호 검증에 사용할 사용자 정보
     * @throws UsernameNotFoundException 사용자를 찾을 수 없거나 부적절한 로그인 방식인 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("폼 로그인 시도 - 이메일: {}", username);

        // 1. 이메일로 사용자 검색
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 이메일로 로그인 시도: {}", username);
                    return new UsernameNotFoundException("존재하지 않는 이메일입니다: " + username);
                });

        // 2. 로그인 타입 확인 - LOCAL 사용자만 폼 로그인 허용
        if (user.getLoginType() != LoginType.LOCAL) {
            log.error("카카오 로그인 사용자가 폼 로그인 시도: {}", username);
            throw new UsernameNotFoundException(
                    "카카오 로그인으로 가입된 계정입니다. 카카오 로그인을 이용해주세요."
            );
        }

        // 3. 비밀번호 필드 확인
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.error("비밀번호가 없는 사용자 계정: {}", username);
            throw new UsernameNotFoundException("비밀번호에 문제가 있습니다. 관리자에게 문의해주세요.");
        }

        log.info("폼 로그인 사용자 찾음 - 이메일: {}, 승인여부: {}", username, user.isApproved());

        // 4. CustomUserDetails로 래핑하여 반환
        return new CustomUserDetails(user);
    }
}
