package com.jason.purchase_agent.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class ChannelResultDto {
    private String status;           // SUCCESS, FAIL 등
    private String message;          // 등록 결과 메시지
    private String channelProductId; // 실제 등록된 상품번호 등
}