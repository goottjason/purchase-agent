package com.jason.purchase_agent.service.process_status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jason.purchase_agent.entity.ProcessStatus;
import com.jason.purchase_agent.repository.jpa.ProcessStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;

/**
 * 현황 테이블(배치/상품별) 비즈니스 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessStatusService {

    private final ProcessStatusRepository psr;

    /** 배치ID + 상품코드 단위 현황 upsert
     *
     * @param batchId       배치ID (외부에서 생성해서 전달)
     * @param productCode   상품코드 (외부에서 생성해서 전달)
     * @param details       상세정보 (null 가능)
     * @param step          처리단계 (null 가능)
     * @param status        상태 (null 가능)
     * @param message       메시지 (null 가능)
     */
    public void upsertProcessStatus(
            String batchId, String productCode, String details,
            String step, String status, String message
    ) {
        // 존재하면 update, 아니면 insert
        Optional<ProcessStatus> existingOpt = psr
                .findByBatchIdAndProductCode(batchId, productCode);
        if (existingOpt.isPresent()) {
            ProcessStatus ps = existingOpt.get();
            if (details != null) ps.setDetails(details);
            if (step != null) ps.setStep(step);
            if (status != null) ps.setStatus(status);
            if (message != null) ps.setMessage(message);
            psr.save(ps);
        } else {
            // Insert 시에는 null -> ""로 초기화해도 되고, builder에서 null 체크해서 ""로 변환해도 됨
            psr.save(ProcessStatus.builder()
                    .batchId(batchId)
                    .productCode(productCode)
                    .details(details)    // null 그대로
                    .step(step)          // null 그대로
                    .status(status)      // null 그대로
                    .message(message)    // null 그대로
                    .build());
        }
    }

    @Transactional
    public void mergeChannelResult(
            String batchId, String productCode, String channel, Map<String, Object> channelResult
    ) {
        log.info("[ProcessStatus][Merge] 호출 - batchId={}, productCode={}, channel={}, step=CHANNEL_UPDATE", batchId, productCode, channel);

        ProcessStatus ps = psr.findByBatchIdAndProductCode(batchId, productCode)
                .orElseThrow(() -> {
                    log.error("[ProcessStatus][Merge] 이력 찾기 실패 - batchId={}, productCode={}", batchId, productCode);
                    return new IllegalArgumentException("해당 상태이력 없음");
                });

        Map<String, Object> msgJson = null;
        try {
            msgJson = ps.getMessage() != null
                    ? objectMapper.readValue(ps.getMessage(), new TypeReference<Map<String,Object>>() {})
                    : new HashMap<>();
            log.debug("[ProcessStatus][Merge] 기존 message 파싱 성공 - msgJson={}", msgJson);
        } catch (JsonProcessingException e) {
            log.error("[ProcessStatus][Merge] message JSON 파싱 실패 - {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        msgJson.put(channel, channelResult);
        log.debug("[ProcessStatus][Merge] 채널 결과 추가 - channel={}, channelResult={}", channel, channelResult);

        try {
            String mergedMsgStr = objectMapper.writeValueAsString(msgJson);
            ps.setMessage(mergedMsgStr);
            log.info("[ProcessStatus][Merge] message 병합 및 저장 준비 - mergedMsg={}", mergedMsgStr);
        } catch (JsonProcessingException e) {
            log.error("[ProcessStatus][Merge] message JSON serialize 실패 - {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        ps.setStep("CHANNEL_UPDATE");

        boolean allSuccess = msgJson.values().stream()
                .allMatch(m -> Boolean.TRUE.equals(((Map)m).get("success")));
        ps.setStatus(allSuccess ? "SUCCESS" : "FAIL");
        log.info("[ProcessStatus][Merge] status 계산, allSuccess={}, 최종 status={}", allSuccess, ps.getStatus());

        psr.save(ps);
        log.info("[ProcessStatus][Merge] 최종 이력 저장 완료 - batchId={}, productCode={}, step={}, status={}", batchId, productCode, ps.getStep(), ps.getStatus());
    }


}