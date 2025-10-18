package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.repository.UserRepository;
import com.jason.purchase_agent.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;   // 비즈니스 로직 담당
    private final UserRepository userRepository;

    // 메인 대시보드
    @GetMapping("/")
    public String home() {
        return "pages/index";
    }

    // 로그인 폼 GET
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "로그인 정보를 확인하세요");
        if (logout != null) model.addAttribute("message", "정상적으로 로그아웃되었습니다");
        return "pages/login";
    }

    // 회원가입 폼 GET
    @GetMapping("/register")
    public String registerPage(Model model) {
        return "pages/register";
    }

    // 회원가입 처리 POST
    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String repeatPassword,
                               Model model) {

        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "이미 사용중인 이메일입니다.");
            return "pages/register";
        }

        // 2. 비밀번호 일치 체크
        if (!password.equals(repeatPassword)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "pages/register";
        }

        // 3. 회원 생성 및 저장 (비밀번호 암호화 포함)
        try {
            userService.createLocalUser(name, email, password);
            model.addAttribute("message", "회원가입 완료! 관리자의 승인 후 이용 가능합니다.");
            return "pages/login";
        } catch (Exception e) {
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "pages/register";
        }
    }

    // 로그인 > Forgot Your Password?
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "pages/forgot-password";
    }
}
