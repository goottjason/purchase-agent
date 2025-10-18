package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.entity.ProcessStatus;
import com.jason.purchase_agent.repository.ProcessStatusRepository;
import com.jason.purchase_agent.service.process_status.ProcessStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 등록 현황 REST API (AJAX, 페이지 구성용)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ProcessStatusController {

    private final ProcessStatusRepository processStatusRepository;
    private final ProcessStatusService processStatusService;

    @GetMapping("/process-status")
    public String processStatusPage() {
        return "pages/process-status";
    }

    @GetMapping("/process-status/list")
    @ResponseBody
    public Map<String, Object> getStatus(@RequestParam(defaultValue="1") int page,
                                         @RequestParam(defaultValue="20") int size) {
        Page<ProcessStatus> result = processStatusService.getPagedStatus(page, size);
        Map<String,Object> out = new HashMap<>();
        out.put("rows", result.getContent());
        out.put("totalPages", result.getTotalPages());
        log.info("out : {}", out);
        return out;
    }
    @PostMapping("/process-status/delete")
    @ResponseBody
    public ResponseEntity<?> delete(@RequestBody Map<String,String> req) {
        processStatusService.deleteRow(req.get("batchId"), req.get("productCode"));
        return ResponseEntity.ok().build();
    }
    @PostMapping("/process-status/delete-all")
    @ResponseBody
    public ResponseEntity<?> deleteAll(){
        processStatusService.deleteAllRows();
        return ResponseEntity.ok().build();
    }
}