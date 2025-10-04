package com.jason.purchase_agent.dto.autoupdate;
import com.jason.purchase_agent.dto.products.ProductDto;
import lombok.*;

import java.time.LocalDateTime;

// 상품 하나마다 개별 메시지 (배치ID, 공급업체코드, 상품코드, 배치 파라미터)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AutoUpdateMessage {
    private String batchId;       // 배치 고유 ID
    private String supplierCode;  // 요청 공급업체 코드
    private Integer marginRate;   // 요청 마진율
    private Integer couponRate;
    private Integer minMarginPrice;
    private String requestedBy;   // 요청 유저ID
    private ProductDto productDto;
    private LocalDateTime requestedAt;
}
