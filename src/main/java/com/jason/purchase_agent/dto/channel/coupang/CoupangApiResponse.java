package com.jason.purchase_agent.dto.channel.coupang;

import lombok.Data;

@Data
public class CoupangApiResponse {
    private int code;
    private Body body;

    @Data
    public static class Body {
        private String code;
        private String message;
        private String data;
    }
}
