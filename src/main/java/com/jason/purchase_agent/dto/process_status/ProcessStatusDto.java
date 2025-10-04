package com.jason.purchase_agent.dto.process_status;

import com.jason.purchase_agent.entity.ProcessStatus;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 등록 처리 현황 DTO (엔티티 구조에 맞춰 수정)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStatusDto {
    private Long id;
    private String batchId;
    private String productCode;
    private String step;
    private String status;
    private String message;
    private String details;           // 상세 json (option)
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;

    public static ProcessStatusDto fromEntity(ProcessStatus e) {
        return new ProcessStatusDto(
                e.getId(),
                e.getBatchId(),
                e.getProductCode(),
                e.getStep(),
                e.getStatus(),
                e.getMessage(),
                e.getDetails(),
                e.getUpdatedAt(),
                e.getStartedAt()
        );
    }
}