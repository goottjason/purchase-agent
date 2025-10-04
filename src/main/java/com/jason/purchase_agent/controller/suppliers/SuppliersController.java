package com.jason.purchase_agent.controller.suppliers;

import com.jason.purchase_agent.dto.suppliers.SupplierDto;
import com.jason.purchase_agent.service.currencies.CurrencyService;
import com.jason.purchase_agent.service.suppliers.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SuppliersController {

    private final SupplierService supplierService;
    private final CurrencyService currencyService;

    @GetMapping("/suppliers")
    public String suppliersPage(Model model) {
        // 통화 리스트 화면에 추가(셀렉트용)
        model.addAttribute("currencies", currencyService.getAllCurrencies());
        return "pages/suppliers";
    }

    @GetMapping("/suppliers/list")
    @ResponseBody
    public List<SupplierDto> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @PostMapping("/admin/suppliers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createSupplier(@RequestBody SupplierDto dto) {
        Map<String, Object> res = new HashMap<>();
        try {
            supplierService.createSupplier(dto);
            res.put("success", true);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
    @PutMapping("/admin/suppliers/{code}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSupplier(@PathVariable String code, @RequestBody SupplierDto dto) {
        Map<String, Object> res = new HashMap<>();
        try {
            supplierService.updateSupplier(code, dto);
            res.put("success", true);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
    @DeleteMapping("/admin/suppliers/{code}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSupplier(@PathVariable String code) {
        Map<String, Object> res = new HashMap<>();
        try {
            supplierService.deleteSupplier(code);
            res.put("success", true);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}