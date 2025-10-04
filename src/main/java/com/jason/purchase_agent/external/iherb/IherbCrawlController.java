package com.jason.purchase_agent.external.iherb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.external.iherb.dto.IherbProductDto;
import com.jason.purchase_agent.external.iherb.dto.IherbProductSimpleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/crawl/iherb")
public class IherbCrawlController {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Random random = new Random();

    @GetMapping("/{productId}")
    public ResponseEntity<IherbProductDto> getIherbProduct(@PathVariable String productId) throws Exception {
        String url = "https://catalog.app.iherb.com/product/" + productId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();
        IherbProductDto dto = objectMapper.readValue(response.body(), IherbProductDto.class);
        System.out.println("dto = " + dto);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/list")
    public ResponseEntity<List<IherbProductDto>> getIherbProducts(@RequestBody List<String> productIds) throws Exception {
        List<IherbProductDto> products = new ArrayList<>();
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        for (String id : productIds) {
            String url = "https://catalog.app.iherb.com/product/" + id;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            IherbProductDto dto = objectMapper.readValue(response.body(), IherbProductDto.class);
            products.add(dto);
        }
        return ResponseEntity.ok(products);
    }

    // 실제 상품 정보 조회/렌더링 (상품ID 리스트를 파라미터로)
    @PostMapping("/list/simple")
    @ResponseBody
    public List<IherbProductSimpleDto> getIherbSimpleProducts(@RequestBody List<String> productIds) throws Exception {
        List<IherbProductSimpleDto> result = new ArrayList<>();
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        for (String id : productIds) {
            String url = "https://catalog.app.iherb.com/product/" + id;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            IherbProductDto dto = objectMapper.readValue(response.body(), IherbProductDto.class);

            System.out.println("dto = " + dto);

            String campaignImage = (dto.getCampaignImages() != null && !dto.getCampaignImages().isEmpty())
                    ? dto.getCampaignImages().get(0) : null;

            IherbProductSimpleDto simpleDto = IherbProductSimpleDto.builder()
                    .campaignImage(campaignImage)
                    .id(dto.getId())
                    .displayName(dto.getDisplayName())
                    .discountPriceAmount(dto.getDiscountPriceAmount())
                    .build();
            System.out.println("simpleDto = " + simpleDto);
            result.add(simpleDto);
            Thread.sleep(TimeUnit.SECONDS.toMillis(2 + random.nextInt(7)));
        }
        return result; // 자동 JSON 배열
    }
}

