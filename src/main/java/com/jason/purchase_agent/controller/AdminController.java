package com.jason.purchase_agent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

  // 메인 대시보드
  @GetMapping("/")
  public String home() {
    return "pages/index";
  }

  // 인증 관련 페이지
  @GetMapping("/login")
  public String login(@RequestParam(value = "error", required = false) String error,
                      @RequestParam(value = "logout", required = false) String logout,
                      Model model) {
    if (error != null) {
      model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    if (logout != null) {
      model.addAttribute("message", "성공적으로 로그아웃되었습니다.");
    }
    return "pages/login"; // login.html 템플릿 반환
  }

  @GetMapping("/register")
  public String register() {
    return "pages/register";
  }

  @GetMapping("/forgot-password")
  public String forgotPassword() {
    return "pages/forgot-password";
  }

  // 에러 페이지
  @GetMapping("/404")
  public String error404() {
    return "pages/404";
  }

  // UI 컴포넌트 페이지들
  @GetMapping("/buttons")
  public String buttons() {
    return "pages/buttons";
  }

  @GetMapping("/cards")
  public String cards() {
    return "pages/cards";
  }

  @GetMapping("/blank")
  public String blank() {
    return "pages/blank";
  }

  // 차트와 테이블
  @GetMapping("/charts")
  public String charts() {
    return "pages/charts";
  }

  @GetMapping("/tables")
  public String tables() {
    return "pages/tables";
  }

  // 유틸리티 페이지들
  @GetMapping("/utilities/color")
  public String utilitiesColor() {
    return "pages/utilities-color";
  }

  @GetMapping("/utilities/border")
  public String utilitiesBorder() {
    return "pages/utilities-border";
  }

  @GetMapping("/utilities/animation")
  public String utilitiesAnimation() {
    return "pages/utilities-animation";
  }

  @GetMapping("/utilities/other")
  public String utilitiesOther() {
    return "pages/utilities-other";
  }
}
