package com.jason.purchase_agent.controller.currencies;

import com.jason.purchase_agent.dto.currencies.CurrencyDto;
import com.jason.purchase_agent.service.currencies.CurrencyService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CurrenciesController {

    private final CurrencyService currencyService;

    // 데이터관리 > 환율
    @GetMapping("/currencies")
    public String currenciesPage() {
        return "pages/currencies";
    }

    @GetMapping("/currencies/list")
    @ResponseBody
    public List<CurrencyDto> getAllCurrencies() {
        return currencyService.getAllCurrencies();
    }

    @PostMapping("/admin/currencies")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCurrency(@RequestBody CurrencyDto dto) {
        Map<String, Object> res = new HashMap<>();
        try {
            currencyService.createCurrency(dto);
            res.put("success", true);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PutMapping("/admin/currencies/{code}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCurrency(
            @PathVariable String code, @RequestBody CurrencyDto dto
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            currencyService.updateCurrency(code, dto);
            res.put("success", true);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @DeleteMapping("/admin/currencies/{code}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCurrency(@PathVariable String code) {
        Map<String, Object> res = new HashMap<>();
        try {
            currencyService.deleteCurrency(code);
            res.put("success", true);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}