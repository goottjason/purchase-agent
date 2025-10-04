package com.jason.purchase_agent.config;

import com.jason.purchase_agent.security.OAuth2LoginSuccessHandler;
import com.jason.purchase_agent.service.CustomOAuth2UserService;
import com.jason.purchase_agent.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 종합 설정 클래스
 *
 * 이 설정의 핵심:
 * 1. 카카오 OAuth2 로그인과 자체 폼 로그인을 동시에 지원
 * 2. 사용자 승인 상태에 따른 세밀한 접근 제어
 * 3. 각 로그인 방식에 맞는 커스텀 서비스 연결
 * 4. 통합 로그인 성공 처리 및 리다이렉트
 */
@Configuration // Spring 설정 클래스임을 명시
@EnableWebSecurity // Spring Security 웹 보안 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;      // 자체 로그인 처리
    private final CustomOAuth2UserService customOAuth2UserService;        // 카카오 로그인 처리
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;     // 로그인 성공 핸들러


    // 정적 리소스 패턴을 상수로 분리
    private static final String[] STATIC_RESOURCES = {
            "/css/**", "/files/**", "/img/**", "/js/**", "/scss/**", "/vendor/**", "/favicon.ico"
    };

    // 공개 API 패턴을 상수로 분리
    private static final String[] PUBLIC_ENDPOINTS = {
            "/login", "/register", "/forgot-password", "/error"
    };


    /**
     * Spring Security의 보안 필터 체인을 구성하는 메서드
     *
     * 이 메서드는 웹 애플리케이션의 '보안 정책'을 정의하고,
     * HTTP 요청에 대한 인증(Authentication)과 인가(Authorization) 규칙을 설정합니다.
     *
     * 주요 기능:
     * 1. URL 패턴별 접근 권한 설정
     * 2. 로그인/로그아웃 처리 방식 설정
     * 3. 세션 관리 정책 설정
     * 4. CSRF, CORS 등 보안 기능 설정
     * 5. 예외 처리 방식 설정
     *
     * 필터 체인의 동작 흐름:
     * 1. 요청 접수: HTTP 요청이 들어오면 FilterChainProxy가 요청을 받습니다
     * 2. 체인 선택: 요청 URL과 매칭되는 SecurityFilterChain을 선택합니다
     * 3. 필터 실행: 선택된 SecurityFilterChain의 필터들이 순차적으로 실행됩니다
     * 4. 보안 처리: 각 필터가 인증, 인가, 세션 관리 등의 보안 작업을 수행합니다
     * 5. 요청 전달: 모든 보안 검증을 통과하면 요청이 컨트롤러로 전달됩니다
     *
     * @param httpSecurity HttpSecurity 객체
     *        - Spring Security의 HTTP 보안 설정을 위한 빌더
     *        - 이 객체를 통해 다양한 보안 설정을 체이닝 방식으로 구성할 수 있습니다.
     *
     * @return SecurityFilterChain 보안 필터 체인 객체
     *         - 설정된 보안 정책에 따라 HTTP 요청을 '필터링'하는 필터들의 체인
     *         - FilterChainProxy가 이 체인을 사용하여 요청별로 적절한 보안 처리를 수행
     *         - 각 요청은 이 필터 체인을 순차적으로 통과하며 보안 검증을 받습니다
     *
     * @throws Exception 보안 설정 구성 중 발생할 수 있는 예외
     *
     * @since Spring Security 5.7+
     *        (이전 버전의 WebSecurityConfigurerAdapter.configure() 메서드를 대체)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // URL 패턴별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 비회원도 모든 정적파일 접근 가능
                        .requestMatchers(STATIC_RESOURCES).permitAll()
                        // 공개 엔드포인트는 누구든 접근 가능
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // "나머지 모든 GET 요청"은 인증된 유저라면 누구나 접근 가능
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("USER", "ADMIN")
                        // 관리자 API는 관리자만 접근 가능
                        .requestMatchers(HttpMethod.POST, "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/admin/**").hasRole("ADMIN")
                        // 혹시 기타 경로는 인증만 요구(더 세밀하게 컨트롤 가능)
                        .anyRequest().authenticated()
                )

                // ============ 폼 로그인 설정 (자체 회원가입 사용자용) ============
                .formLogin(form -> form
                        .loginPage("/login")                    // 커스텀 로그인 페이지
                        .loginProcessingUrl("/login")           // 폼 제출 시 처리 URL (POST /login)
                        .usernameParameter("email")             // username 파라미터명을 email로 설정
                        .passwordParameter("password")          // password 파라미터명을 password로 설정
                        .successHandler(oauth2LoginSuccessHandler) // 성공 시 OAuth2와 동일한 핸들러 사용
                        .failureUrl("/login?error=true")        // 실패 시 리다이렉트 URL
                        .permitAll()
                )
                // UserDetailsService 연결 (폼 로그인용)
                .userDetailsService(customUserDetailsService)

                // ============ OAuth2 로그인 설정 (카카오 로그인용) ============
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")                        // 폼 로그인과 동일한 페이지 사용
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))  // 카카오 사용자 정보 처리 서비스
                        .successHandler(oauth2LoginSuccessHandler)  // 로그인 성공 후 처리
                        .failureUrl("/login?error=oauth")           // OAuth2 로그인 실패 시
                )

                // ============ 로그아웃 설정 ============
                .logout(logout -> logout
                        .logoutUrl("/logout")                       // 로그아웃 처리 URL
                        .logoutSuccessUrl("/login?logout=true")     // 로그아웃 성공 후 이동 URL
                        .invalidateHttpSession(true)                // 세션 무효화
                        .deleteCookies("JSESSIONID")                // 쿠키 삭제
                        .permitAll()
                )

                // CSRF 보호 기능
                .csrf(csrf -> csrf.disable()) // 개발 초기 단계에서만 비활성화 권장

                // 세션 관리 정책
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // 필요 시 세션 생성
                        .maximumSessions(1)                                         // 동시 세션 1개로 제한
                        .maxSessionsPreventsLogin(false)                            // 새 로그인 시 이전 세션 만료
                )
                // 예외 처리 설정
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json; charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"관리자만 접근 가능합니다.\"}");
                    })
                );



        // 설정이 완료된 SecurityFilterChain 객체 반환
        // 이 객체는 Spring이 자동으로 빈으로 등록하여 보안 처리에 사용됩니다
        return httpSecurity.build();
    }

    // 비밀번호 암호화를 위한 인코더 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt 해싱 알고리즘 사용
    }

    /**
     * 사용자 인증을 처리하기 위한 사용자 정보 서비스를 정의하는 메서드
     *
     * @return UserDetailsService
     *         - Spring Security가 로그인 시 사용자 정보를 조회하기 위해 사용하는 서비스
     */
    @Bean
    public UserDetailsService userDetailsService() {
        /*
         * UserDetails 객체 생성
         * - UserDetails는 Spring Security에서 사용자 정보를 표현하는 표준 인터페이스
         * - 이 인터페이스를 통해 Spring Security는 사용자의 아이디, 비밀번호, 권한 등을 파악
         * - User.builder()는 Spring Security가 제공하는 기본 UserDetails 구현체를 생성하는 빌더 패턴
         */
        UserDetails admin = User.builder()
                /*
                 * username: 사용자를 고유하게 식별하는 값 (로그인 ID)
                 * - 로그인 폼에서 입력한 사용자명과 비교되는 값
                 * - 데이터베이스 환경에서는 보통 이메일이나 사용자ID가 됨
                 */
                .username("admin") // 사용자명

                /*
                 * password: 암호화된 비밀번호
                 * - passwordEncoder().encode("admin123"): 평문 "admin123"을 BCrypt 알고리즘으로 암호화
                 * - Spring Security는 로그인 시 사용자가 입력한 평문 비밀번호를 같은 방식으로 암호화하여 비교
                 * - 평문 비밀번호를 그대로 저장하면 보안상 매우 위험하므로 반드시 암호화해야 함
                 */
                .password(passwordEncoder().encode("admin123")) // 암호화된 비밀번호

                /*
                 * roles: 사용자의 권한 역할 정의
                 * - "ADMIN" 문자열은 내부적으로 "ROLE_ADMIN"으로 변환됨 (Spring Security 규칙)
                 * - 이 권한은 @PreAuthorize나 .hasRole() 등에서 접근 제어에 사용됨
                 * - 예: .requestMatchers("/admin/**").hasRole("ADMIN") 에서 이 권한을 확인
                 */
                .roles("ADMIN") // 사용자 권한

                /*
                 * build(): 설정된 값들로 UserDetails 객체를 최종 생성
                 * - 내부적으로 Spring Security의 User 클래스 인스턴스가 생성됨
                 * - 이 User 클래스는 UserDetails 인터페이스의 기본 구현체
                 */
                .build();

        /*
         * InMemoryUserDetailsManager 반환
         *
         * InMemoryUserDetailsManager란?
         * - UserDetailsService 인터페이스의 구현체 중 하나
         * - 사용자 정보를 메모리(RAM)에 저장하고 관리하는 방식
         * - 애플리케이션이 종료되면 사용자 정보도 함께 사라짐 (휘발성)
         *
         * 동작 원리:
         * 1. 사용자가 로그인 폼에서 아이디/비밀번호 입력
         * 2. Spring Security가 이 UserDetailsService의 loadUserByUsername() 메서드 호출
         * 3. InMemoryUserDetailsManager가 메모리에 저장된 사용자 목록에서 해당 username 검색
         * 4. 찾은 UserDetails 객체를 Spring Security에 반환
         * 5. Spring Security가 비밀번호 검증 및 권한 확인 수행
         *
         * 장점:
         * - 설정이 간단하고 테스트 용도로 적합
         * - 별도 데이터베이스 연결 불필요
         *
         * 단점:
         * - 실제 운영환경에는 부적합 (재시작 시 데이터 소실)
         * - 사용자 추가/수정/삭제가 런타임에 제한적
         * - 확장성 부족
         */
        return new InMemoryUserDetailsManager(admin); // 메모리 기반 사용자 관리
    }


}
