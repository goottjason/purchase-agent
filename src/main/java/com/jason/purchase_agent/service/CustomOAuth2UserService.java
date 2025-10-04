package com.jason.purchase_agent.service;

import com.jason.purchase_agent.entity.User;
import com.jason.purchase_agent.enums.LoginType;
import com.jason.purchase_agent.repository.jpa.UserRepository;
import com.jason.purchase_agent.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * 카카오 OAuth2 로그인 시 사용자 정보를 처리하는 커스텀 서비스
 *
 * 처리 흐름:
 * 1. 카카오에서 사용자 정보 수신
 * 2. 기존 사용자인지 확인 (kakaoId 기준)
 * 3. 신규 사용자면 회원가입 처리 (PENDING 상태)
 * 4. 기존 사용자면 정보 업데이트
 * 5. CustomOAuth2User 객체로 래핑하여 Spring Security에 전달
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 로그인 성공 후 호출되는 메서드
     * - Spring Security가 자동으로 호출
     * - 카카오에서 받은 사용자 정보를 처리하여 우리 시스템의 User 엔티티와 매핑
     *
     * @param userRequest OAuth2 제공자(카카오)에서 전달받은 요청 정보
     * @return OAuth2User Spring Security가 인증 객체에 저장할 사용자 정보
     * @throws OAuth2AuthenticationException OAuth2 인증 과정에서 발생하는 예외
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("카카오 OAuth2 로그인 처리 시작");

        // 1. 카카오로부터 사용자 정보 가져오기
        OAuth2User oauth2User = super.loadUser(userRequest);

        // 2. 제공자 정보 확인 (현재는 kakao만 지원)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 제공자: {}", registrationId);

        // 3. 카카오 응답에서 사용자 정보 추출
        Map<String, Object> attributes = oauth2User.getAttributes();

        // 카카오 사용자 고유 ID (숫자)
        String kakaoId = String.valueOf(attributes.get("id"));

        // 카카오 계정 정보 (이메일 포함)
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        // 카카오 프로필 정보 (닉네임, 프로필 사진 포함)
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String name = (String) properties.get("nickname");
        String picture = (String) properties.get("profile_image");

        log.info("카카오 사용자 정보 - ID: {}, 이메일: {}, 닉네임: {}", kakaoId, email, name);

        // 4. 기존 사용자 확인 및 처리
        User user = processOAuth2User(kakaoId, email, name, picture);

        // 5. Spring Security용 OAuth2User 객체 생성
        return new CustomOAuth2User(user, attributes);
    }

    /**
     * 카카오 로그인 사용자 정보 처리 로직
     * - 신규 사용자: 회원가입 처리
     * - 기존 사용자: 정보 업데이트
     *
     * @param kakaoId 카카오 사용자 ID
     * @param email 카카오 계정 이메일
     * @param name 카카오 닉네임
     * @param picture 카카오 프로필 사진 URL
     * @return User 처리된 사용자 엔티티
     */
    private User processOAuth2User(String kakaoId, String email, String name, String picture) {

        // 1차 확인: kakaoId로 기존 사용자 검색
        Optional<User> existingUserByKakaoId = userRepository.findByKakaoId(kakaoId);

        if (existingUserByKakaoId.isPresent()) {
            // 기존 카카오 사용자 - 정보 업데이트
            User user = existingUserByKakaoId.get();
            log.info("기존 카카오 사용자 로그인: {}", email);

            // 카카오에서 정보가 변경되었을 수 있으므로 업데이트
            user.setName(name);
            user.setPicture(picture);
            // 이메일은 카카오에서 변경될 수 있지만, 이미 우리 시스템에 다른 계정으로 존재할 수 있음
            // 따라서 이메일 업데이트는 신중하게 처리해야 함

            return userRepository.save(user);
        }

        // 2차 확인: 같은 이메일로 자체 회원가입된 사용자가 있는지 검색
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);

        if (existingUserByEmail.isPresent()) {
            User existingUser = existingUserByEmail.get();

            // 같은 이메일이지만 다른 로그인 방식인 경우
            if (existingUser.getLoginType() == LoginType.LOCAL) {
                log.error("이미 자체 회원가입된 이메일로 카카오 로그인 시도: {}", email);
                throw new OAuth2AuthenticationException(
                        "이미 일반 회원가입으로 등록된 이메일입니다. 일반 로그인을 이용해주세요.");
            }
        }

        // 신규 카카오 사용자 - 회원가입 처리
        log.info("신규 카카오 사용자 회원가입: {}", email);

        User newUser = User.createKakaoUser(email, name, picture, kakaoId);
        return userRepository.save(newUser);
    }
}