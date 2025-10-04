package com.jason.purchase_agent.dto.perplexity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerplexityMessage {
    private String role;
    private String content;

}