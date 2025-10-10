package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.dto.products.ProductUpdateRequest;
import com.jason.purchase_agent.service.products.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AutoUpdateController {
    private final ProductService productService;

    @PostMapping("/auto-update/run")
    @ResponseBody
    public ResponseEntity<?> crawlAndUpdateBySupplier (
            @RequestParam Integer marginRate,
            @RequestParam Integer couponRate,
            @RequestParam Integer minMarginPrice,
            @RequestParam String supplierCode
    ) {
        // IHB 상품만 모아서 ProductUpdateRequest 리스트로 반환
        // Product, Mapping 테이블 필드값 전부 productDto에 세팅완료
        List<ProductUpdateRequest> requests = productService.makeRequestsBySupplier(supplierCode);

        // 원하는 상품만 code로 필터
        /*List<ProductUpdateRequest> filteredRequests = requests.stream()
                .filter(req -> "201203IHB006".equals(req.getProductDto().getCode()))
                .collect(Collectors.toList());*/

        // 각 상품별 메세지 발행
        productService.crawlAndUpdateBySupplier(marginRate, couponRate, minMarginPrice, requests);
        // productService.crawlAndUpdateBySupplier(marginRate, couponRate, minMarginPrice, filteredRequests);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수정 요청 접수. 실제 반영 여부는 이력에서 확인"
        ));
    }
}
