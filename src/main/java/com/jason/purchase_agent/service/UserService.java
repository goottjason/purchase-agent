package com.jason.purchase_agent.service;

import com.jason.purchase_agent.entity.User;
import com.jason.purchase_agent.enums.LoginType;
import com.jason.purchase_agent.enums.UserRole;
import com.jason.purchase_agent.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 자체 회원가입 처리 (비밀번호 암호화/Entity 생성/승인 대기)
    public User createLocalUser(String name, String email, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword); // 비번 암호화
        User user = User.createLocalUser(email, name, encodedPassword); // 팩토리 생성
        return userRepository.save(user);
    }
}
