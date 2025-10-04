package com.jason.purchase_agent.dto.product_registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRegistrationRetryRequest {
    private String batchId;
    private String productCode;     // 1,2,4단계 재시도용(product 단건)
    private List<String> productCodes; // 3단계 일괄재시도용(product 여러개)
    private String step;
    private List<String> retryChannels; // 4단계(채널등록) 시에만 사용
}
