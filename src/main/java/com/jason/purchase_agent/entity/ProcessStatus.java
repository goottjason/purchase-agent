package com.jason.purchase_agent.entity;

import com.jason.purchase_agent.enums.JobType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "process_status",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"batchId", "productCode"})
        }
)
@Builder @Data @NoArgsConstructor @AllArgsConstructor
@Accessors(chain = true)
public class ProcessStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String batchId;

    @Column()
    private String productCode;

    // === 추가 필드 ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = true) // 나중에 false로 바꿔서 수정해야돼
    private JobType jobType;  // 작업 유형

    @Column(nullable = false)
    private String step; // INIT, DATA_PREPARATION, VALIDATION, DB_SAVE, CHANNEL_UPDATE, COMPLETED

    @Column(nullable = false)
    private String status; // PENDING, IN_PROGRESS, SUCCESS, FAILED

    @Column(length = 1000)
    private String message;  // 상태 메시지 (에러포함)

    @Column(columnDefinition = "TEXT")
    private String details;  // option: 상세 json

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime startedAt;   // 이 상품에 대한 전체처리 시작시각

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;   // 최근 작업 시각

}
