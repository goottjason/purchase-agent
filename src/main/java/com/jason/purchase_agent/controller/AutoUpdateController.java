package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.dto.suppliers.SupplierDto;
import com.jason.purchase_agent.entity.Supplier;
import com.jason.purchase_agent.repository.jpa.SupplierRepository;
import com.jason.purchase_agent.service.autoupdate.AutoUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AutoUpdateController {

    private final SupplierRepository supplierRepo;
    private final AutoUpdateService autoUpdateService;

    // 공급업체 목록 반환 (페이지 초기 로딩용)
    @GetMapping("/auto-update/suppliers")
    public List<SupplierDto> getSuppliers() {
        List<Supplier> suppliers = supplierRepo.findAll();
        // Entity → DTO 변환
        return suppliers.stream()
                .map(supplier -> new SupplierDto(supplier, null))
                .collect(Collectors.toList());
    }

    // AJAX로 배치업데이트 실행 (서버에서 비동기 메시지 발행)
    @PostMapping("api/auto-update")
    public ResponseEntity<?> startAutoUpdate (
            @RequestParam String supplierCode,
            @RequestParam Integer marginRate,
            @RequestParam Integer couponRate,
            @RequestParam Integer minMarginPrice
    ) {
        autoUpdateService.startAutoUpdate(
                supplierCode, marginRate, couponRate, minMarginPrice,"amdin");
        return ResponseEntity.ok().build();
    }
}
