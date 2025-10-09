package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.dto.products.ProductUpdateRequest;
import com.jason.purchase_agent.dto.suppliers.SupplierDto;
import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.entity.Supplier;
import com.jason.purchase_agent.repository.jpa.SupplierRepository;
import com.jason.purchase_agent.service.autoupdate.AutoUpdateService;
import com.jason.purchase_agent.service.products.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AutoUpdateController {

    private final ProductService productService;


    // AJAX로 배치업데이트 실행 (서버에서 비동기 메시지 발행)
    @PostMapping("/auto-update/run")
    public ResponseEntity<?> runAutoUpdate (
            @RequestParam Integer marginRate,
            @RequestParam Integer couponRate,
            @RequestParam Integer minMarginPrice,
            @RequestParam String supplierCode
    ) {
        List<ProductUpdateRequest> requests = productService.makeUpdateRequestsBySupplier(supplierCode);

        // [비동기] 메시지 큐(RabbitMQ 등)에 배치 등록 (예시)
        productService.enqueueProductBatchUpdate(marginRate, couponRate, minMarginPrice, requests);


        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수정 요청 접수. 실제 반영 여부는 이력에서 확인"
        ));
    }
}
