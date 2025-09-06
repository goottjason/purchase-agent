package com.jason.purchase_agent.dto.perplexity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerplexityRequest {
  private String model;
  private List<PerplexityMessage> messages;
  private int maxTokens;
  private double temperature;
}