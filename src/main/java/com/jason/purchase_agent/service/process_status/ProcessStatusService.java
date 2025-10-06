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
import java.util.*;
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

    /**
     * 채널별 결과를 누적 리스트(List)로 담는 mergeChannelResult 구현.
     *
     * 예시) DB에 저장되는 message 컬럼(최종 형태)
     * {
     *   "coupang": [
     *     { "type": "price", "success": true, "message": "가격 변경 완료" },
     *     { "type": "stock", "success": true, "message": "재고 변경 완료" }
     *   ],
     *   "smartstore": [
     *     { "type": "price", "success": false, "message": "API 에러..." }
     *   ]
     * }
     */
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
            String msgString = ps.getMessage();
            // message 컬럼이 "{}"이거나 빈 값이면 빈 Map, JSON이면 파싱
            msgJson = (msgString != null && msgString.trim().startsWith("{"))
                    ? objectMapper.readValue(msgString, new TypeReference<Map<String,Object>>() {})
                    : new HashMap<>();
            log.debug("[ProcessStatus][Merge] 기존 message 파싱 성공 - msgJson={}", msgJson);
        } catch (JsonProcessingException e) {
            log.warn("[ProcessStatus][Merge] message JSON 파싱 실패, 빈 map으로 대체 - {}", e.getMessage());
            msgJson = new HashMap<>();
        }

        /*
         * 누적 구조로 각 채널은 리스트(List<Map<String,Object>>)
         * 기존에 coupang => list가 없으면 새로 만들고, 있으면 꺼내서 append
         * 반드시 channelResult에 type ("price"/"stock" 등) 필드 포함!
         */
        List<Map<String, Object>> resultList = (List<Map<String, Object>>) msgJson.get(channel);
        if (resultList == null) {
            resultList = new ArrayList<>();
            msgJson.put(channel, resultList);
        }
        resultList.add(channelResult);

        log.debug("[ProcessStatus][Merge] 채널 결과 리스트 추가 - channel={}, channelResult={}", channel, channelResult);

        try {
            String mergedMsgStr = objectMapper.writeValueAsString(msgJson);
            ps.setMessage(mergedMsgStr);
            log.info("[ProcessStatus][Merge] message 병합 및 저장 준비 - mergedMsg={}", mergedMsgStr);
        } catch (JsonProcessingException e) {
            log.error("[ProcessStatus][Merge] message JSON serialize 실패 - {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        ps.setStep("CHANNEL_UPDATE");

        // 전체 채널의 price/stock update가 모두 success일 때만 SUCCESS, 아니면 FAIL
        boolean allSuccess = msgJson.values().stream()
                .flatMap(val -> ((List<Map<String,Object>>) val).stream())
                .allMatch(item -> Boolean.TRUE.equals(item.get("success")));
        ps.setStatus(allSuccess ? "SUCCESS" : "FAIL");
        log.info("[ProcessStatus][Merge] status 계산, allSuccess={}, 최종 status={}", allSuccess, ps.getStatus());

        psr.save(ps);
        log.info("[ProcessStatus][Merge] 최종 이력 저장 완료 - batchId={}, productCode={}, step={}, status={}", batchId, productCode, ps.getStep(), ps.getStatus());
    }



}