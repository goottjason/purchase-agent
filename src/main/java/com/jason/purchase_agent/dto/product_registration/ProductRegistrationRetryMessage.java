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
public class ProductRegistrationRetryMessage {
    private String batchId;
    private String startStep;
    private String requestedBy;
    private LocalDateTime requestedAt;

    private List<ProductRegistrationRequest> products; // 3단계 일괄
    private ProductRegistrationRequest product;        // 단건
    private List<String> retryChannels;            // 4단계 선택
}
