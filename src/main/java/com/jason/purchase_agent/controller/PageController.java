package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.service.categories.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {
    private final CategoryService categoryService;

    // 메인 대시보드
    @GetMapping("/")
    public String home() {
        return "pages/index";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() { return "pages/index"; }



    @GetMapping("/auto-update")
    public String autoUpdatePage() { return "pages/auto-update"; }


    // 데이터관리 > 카테고리



    // 상품등록 > 자동 상품등록


    // 사이드바 > Buttons
    @GetMapping("/buttons")
    public String buttons() {
        return "pages/buttons";
    }
    // 사이드바 > Cards
    @GetMapping("/cards")
    public String cards() {
        return "pages/cards";
    }
    // 사이드바 > Blank
    @GetMapping("/blank")
    public String blank() {
        return "pages/blank";
    }
    // 사이드바 > 404
    @GetMapping("/404")
    public String error404() {
        return "pages/404";
    }
    // 사이드바 > Charts
    @GetMapping("/charts")
    public String charts() {
        return "pages/charts";
    }
    // 사이드바 > Tables
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
