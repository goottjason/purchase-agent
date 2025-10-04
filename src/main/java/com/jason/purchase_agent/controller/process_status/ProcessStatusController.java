package com.jason.purchase_agent.controller.process_status;

import com.jason.purchase_agent.entity.ProcessStatus;
import com.jason.purchase_agent.repository.jpa.ProcessStatusRepository;
import com.jason.purchase_agent.service.process_status.ProcessStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 등록 현황 REST API (AJAX, 페이지 구성용)
 */
@Controller
@RequiredArgsConstructor
public class ProcessStatusController {

    private final ProcessStatusRepository processStatusRepository;

    @GetMapping("/process-status")
    public String processStatusPage() {
        return "pages/process-status";
    }


}