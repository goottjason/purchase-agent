package com.jason.purchase_agent.dto.product_registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRegistrationMessage {
    private String batchId; // 배치 추적용 (일괄 작업 그룹 생성자)
    private Integer totalProductCount;
    private ProductRegistrationRequest request;
}