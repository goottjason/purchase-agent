package com.jason.purchase_agent.external.iherb;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IherbCategoryCrawler {

    /**
     * iHerb 카테고리 상위 상품 ID 리스트 크롤링
     *
     * @param categoryUrl 카테고리 URL (ex: https://kr.iherb.com/c/vitamins)
     * @param count       상위 추출 상품수 (ex: 10)
     * @param sortOrder   정렬 파라미터 (ex: discount_desc)
     *                    (best_sellers, ratings, reviews, discount_desc 등)
     * @return 상품ID(숫자문자열) 리스트
     */
    public List<String> getTopProductIds(String categoryUrl, int count, int sortOrder) {
        List<String> productIds = new ArrayList<>();
        String url = categoryUrl + "?sr=" + sortOrder + "&page=1";
        // 여기서 모바일 UserAgent/헤더/Proxy 등 봇우회 옵션 추가 가능
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );
            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            );
            Page page = context.newPage();
            page.navigate(url);

            // ***iHerb 상품 리스트의 상품 링크 a[data-ga-label=product-name] 기준***
            // DOM구조 바뀌면 크롬 개발자도구로 selector확인 (2025년 9월 기준)
            List<ElementHandle> products = page.querySelectorAll("a[data-ga-event-action=productClick]");
            for (ElementHandle handle : products) {
                String href = handle.getAttribute("href");
                // 상품ID는 항상 마지막 path segment (예: /pr/상품명/12345)
                if (href != null) {
                    String[] tokens = href.split("/");
                    String prodId = tokens[tokens.length - 1];
                    productIds.add(prodId);
                    if (productIds.size() >= count) break;
                }
            }
            // 자원정리
            page.close();
            browser.close();
        }
        return productIds;
    }
}
