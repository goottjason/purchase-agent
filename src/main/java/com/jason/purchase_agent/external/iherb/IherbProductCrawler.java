package com.jason.purchase_agent.external.iherb;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.jason.purchase_agent.util.http.HttpClientUtil.client;

@Slf4j
@Service
public class IherbProductCrawler {

    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    /**
     * [로직 설명]
     * 해당 카테고리 목록에서 제품 상세링크 URL 리스트를 최대 n개까지 추출(크롤링)
     * - 실제로는 JSoup/Selenium 활용, 예시로 더미 데이터 리턴
     */
    private List<String> crawlProductLinksFromCategory(String categoryUrl, int n) throws InterruptedException {
        System.out.println("[SOUT] URL 이동 완료: " + categoryUrl);
        ChromeOptions options = new ChromeOptions();
//    options.addArguments("--headless=new");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)...");
        options.addArguments("accept-language=ko-KR,ko;q=0.9,en-US,en;q=0.8");
        options.addArguments("--window-size=1920,1080");
//    options.addArguments("--user-data-dir=C:/Users/369bu/AppData/Local/Google/Chrome/User Data");
//    options.addArguments("--profile-directory=Profile 3");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-notifications");
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);


        WebDriver driver = new ChromeDriver(options);
        Random rand = new Random();

        System.out.println("[SOUT] 드라이버 생성 완료");

        try {
            driver.get(categoryUrl);
            System.out.println("[SOUT] URL 이동 완료: " + categoryUrl);

            Thread.sleep(1000 + rand.nextInt(1000));
            System.out.println("[SOUT] 첫 sleep 후");

            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            System.out.println("[SOUT] 스크롤 완료");

            Thread.sleep(1200 + rand.nextInt(1200));
            System.out.println("[SOUT] 스크롤 후 sleep");

            Thread.sleep(2000 + rand.nextInt(800));
            System.out.println("[SOUT] 최종 렌더링 대기 후");

            String pageSource = driver.getPageSource();
            System.out.println("pageSource = " + pageSource);
            System.out.println("[SOUT] pageSource 추출 완료");

            Document doc = Jsoup.parse(pageSource);
            System.out.println("doc = " + doc);

            System.out.println("[SOUT] Jsoup 파싱 완료");

            Elements aTags = doc.select("a[href^=/pr/]");
            System.out.println("[SOUT] 추출된 a 태그 개수: " + aTags.size());

            Set<String> seen = new HashSet<>();
            List<String> productLinks = new ArrayList<>();

            for (Element a : aTags) {
                String href = a.attr("href");
                if (!seen.contains(href)) {
                    seen.add(href);
                    productLinks.add("https://kr.iherb.com" + href);
                }
                if (productLinks.size() >= n) break;
            }
            System.out.println("[SOUT] 최종 상품 링크 개수: " + productLinks.size());
            return productLinks;
        } finally {
            driver.quit();
            System.out.println("[SOUT] 드라이버 종료");
        }
    }


    /**
     * 제품 상세 크롤 JSON 크롤러 - 위에서 제공한 버전과 연동, 최대 재시도 포함
     */
    public static String crawlProductAsJson(String productId) throws IOException {
        int maxRetries = 3;
        long initialDelayMs = 3000;
        int retryCount = 0;

        while (retryCount <= maxRetries) {
            String url = "https://catalog.app.iherb.com/product/" + productId;
            // log.info("Fetching URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", generateRandomUserAgent())
                    .header("Accept", "application/json")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Referer", "https://www.iherb.com/")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                int code = response.code();
                if (response.isSuccessful()) {
                    return response.body().string();
                } else if (code == 403 && retryCount < maxRetries) {
                    System.out.printf("403 발생. %d초 후 재시도...%n", (initialDelayMs / 1000));
                    retryCount++;
                    try {
                        Thread.sleep(initialDelayMs * (long) Math.pow(2, retryCount));
                    } catch (InterruptedException e) {
                    }
                } else if (code == 404) {
                    throw new IOException("HTTP 404 error: 해당 상품 없음. (" + url + ")");
                } else {
                    throw new IOException("HTTP error " + code + ": " + response.message() + " (" + url + ")");
                }
            }
        }
        throw new IOException("최대 재시도 초과");
    }

    /**
     * JSON 파싱 (Google Gson 등)
     */
    private static JsonObject parseJson(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    /**
     * User-Agent 무작위로
     */
    private static String generateRandomUserAgent() {

        return faker.internet().userAgent();
    }

    /**
     * 크롤러 차단 방지/랜덤 딜레이
     */
    public static long getRandomDelay() {
        return TimeUnit.SECONDS.toMillis(2 + random.nextInt(7));
    }
}
