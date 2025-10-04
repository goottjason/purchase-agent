/*
package com.jason.purchase_agent.repository.jpa;


import com.jason.purchase_agent.entity.ProcessStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class ProcessStatusCustomRepositoryImpl implements ProcessStatusCustomRepository {

    private final EntityManager em;

    @Override
    @Transactional
    public void insertBatch(String batchId, String step, String message, String status, LocalDateTime startedAt) {
        // (예시: 상품코드 없이 배치 단위 row 한건 넣음)
        ProcessStatus batchRow = new ProcessStatus();
        batchRow.setBatchId(batchId);
        batchRow.setStatus(status);
        batchRow.setStep(step);
        batchRow.setMessage(message);
        batchRow.setStartedAt(startedAt);
        batchRow.setUpdatedAt(startedAt);
        em.persist(batchRow);
    }

    @Override
    @Transactional
    public void updateStatus(String productCode, String status, String step, String message, LocalDateTime now) {
        em.createQuery(
                        "UPDATE ProcessStatus s SET s.status = :status, s.step = :step, s.message = :message, s.updatedAt = :now " +
                                "WHERE s.productCode = :productCode"
                )
                .setParameter("status", status)
                .setParameter("step", step)
                .setParameter("message", message)
                .setParameter("now", now)
                .setParameter("productCode", productCode)
                .executeUpdate();
    }

    @Override
    @Transactional
    public void updateBatchStatus(String batchId, String status, String step, String message, LocalDateTime now) {
        em.createQuery(
                        "UPDATE ProcessStatus s SET s.status = :status, s.step = :step, s.message = :message, s.updatedAt = :now " +
                                "WHERE s.batchId = :batchId AND s.productCode IS NULL"
                )
                .setParameter("status", status)
                .setParameter("step", step)
                .setParameter("message", message)
                .setParameter("now", now)
                .setParameter("batchId", batchId)
                .executeUpdate();
    }
}

*/
