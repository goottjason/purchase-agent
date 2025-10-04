package com.jason.purchase_agent.service.process_status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jason.purchase_agent.entity.ProcessStatus;
import com.jason.purchase_agent.repository.jpa.ProcessStatusRepository;
import lombok.RequiredArgsConstructor;
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
        // select for update or with optimistic @Version
        ProcessStatus ps = psr.findByBatchIdAndProductCode(batchId, productCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 상태이력 없음"));

        // message JSON 파싱 (혹시 11번가 xml을 json으로 바꿔서 보내져야하는데 그 부분 체크할 것
        Map<String, Object> msgJson = null;
        try {
            msgJson = ps.getMessage() != null
                    ? objectMapper.readValue(ps.getMessage(), new TypeReference<Map<String,Object>>() {})
                    : new HashMap<>();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        msgJson.put(channel, channelResult);
        // 다시 JSON 문자열로 저장
        try {
            ps.setMessage(objectMapper.writeValueAsString(msgJson));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ps.setStep("CHANNEL_UPDATE");
        // 전체 채널 성공/실패 summary도 판단 가능(추가적 요구 시)
        ps.setStatus(msgJson.values().stream()
                .allMatch(m -> Boolean.TRUE.equals(((Map)m).get("success"))) ? "SUCCESS" : "FAIL");
        psr.save(ps);
    }

}