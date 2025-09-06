package com.jason.purchase_agent.dto.channel;

import lombok.Data;

import java.util.List;

@Data
public class SmartstoreProductPageResultDto {
  private int page;
  private int size;
  private int totalPages;
  private int totalElements;
  private boolean first;
  private boolean last;
  private List<SmartstoreProductDto> contents;
  // 필요시 sort, 기타 boolean 등 추가
}