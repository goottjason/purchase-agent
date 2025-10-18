package com.jason.purchase_agent.repository;

import com.jason.purchase_agent.entity.ProcessStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessStatusRepository extends JpaRepository<ProcessStatus, Long> {

    List<ProcessStatus> findAllByBatchId(String batchId);

    void deleteByBatchIdAndProductCode(String batchId, String productCode);

    void deleteByBatchId(String batchId);

    // 배치ID + 상품코드 기준 단일 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProcessStatus> findByBatchIdAndProductCode(String batchId, String productCode);

    // 전체 Processing 중인 현황
    List<ProcessStatus> findByStatus(String status);
    // 배치별 상품 현황
    List<ProcessStatus> findByBatchIdOrderByStartedAtDesc(String batchId);
    // 완료/실패 등 이력 조회
    List<ProcessStatus> findByStatusIn(List<String> statusList);
    // 배치별 구분 ID 리스트
    List<ProcessStatus> findDistinctByBatchIdIsNotNullOrderByStartedAtDesc();



    // 상품코드별 현황
    List<ProcessStatus> findByProductCode(String productCode);

    // 특정 배치(Batch) 전체 조회
    List<ProcessStatus> findByBatchId(String batchId);

    // 최근 수정 순 등으로 현황 조회
    List<ProcessStatus> findAllByOrderByUpdatedAtDesc();

    // ----------- 업데이트/삽입 쿼리(커스텀 없이) -----------

    // batchId + productCode=null(배치행만) 기준으로 업데이트
    @Transactional
    @Modifying
    @Query("UPDATE ProcessStatus s SET s.step = :step, s.status = :status, s.message = :message, s.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE s.batchId = :batchId AND s.productCode IS NULL")
    int updateBatchStatus(String batchId, String productCode, String step, String status, String message);

    // productCode 기준 상태/단계/메시지/수정시간 업데이트
    @Transactional
    @Modifying
    @Query(
            "UPDATE ProcessStatus s " +
                    "SET s.step = :step, " +
                    "    s.status = :status, " +
                    "    s.message = :message, " +
                    "    s.details = CASE WHEN :details IS NOT NULL THEN :details ELSE s.details END, " +
                    "    s.updatedAt = CURRENT_TIMESTAMP " +
                    "WHERE s.batchId = :batchId " +
                    "  AND s.productCode = :productCode"
    )
    int updateProductStatus(
            String batchId,
            String productCode,
            String step,
            String status,
            String message,
            String details
    );



    // batchId, status,

    // 배치 Row 직접 Insert (JPA는 persist 대신 save 사용)
    // 만약 신규 배치 행 생성 필요하면 Service에서 아래 식으로 제작
    default ProcessStatus insertBatch(String batchId, String step, String status, String message) {
        ProcessStatus ps = new ProcessStatus();
        ps.setBatchId(batchId);
        ps.setStatus(status);
        ps.setStep(step);
        ps.setMessage(message);
        ps.setStartedAt(LocalDateTime.now());
        ps.setUpdatedAt(LocalDateTime.now());
        return save(ps); // JPA save
    }

    default ProcessStatus insertStep(String batchId, String productCode, String productName, String status, String step, String message) {
        ProcessStatus ps = new ProcessStatus();
        ps.setBatchId(batchId);
        ps.setProductCode(productCode);
        ps.setStatus(status);
        ps.setStep(step);
        ps.setMessage(message);
        ps.setStartedAt(LocalDateTime.now());
        ps.setUpdatedAt(LocalDateTime.now());
        // finishedAt은 성공/실패 단계에서만 갱신
        return save(ps);
    }

    default ProcessStatus insert(
            String batchId, String productCode, String productName, String status, String step, String message
    ) {
        ProcessStatus ps = new ProcessStatus();
        ps.setBatchId(batchId);
        ps.setProductCode(productCode);
        ps.setStatus(status);
        ps.setStep(step);
        ps.setMessage(message);
        ps.setStartedAt(LocalDateTime.now());
        ps.setUpdatedAt(LocalDateTime.now());
        // finishedAt은 성공/실패 단계에서만 갱신
        return save(ps);
    }


    // 배치(상품 없음)만, 최신순
    List<ProcessStatus> findByProductCodeIsNullOrderByStartedAtDesc();

    // 특정 배치의 상품 리스트(상품코드 null이 아닌 것), 상품코드 순서
    List<ProcessStatus> findByBatchIdAndProductCodeIsNotNullOrderByProductCode(String batchId);


}
