package com.jason.purchase_agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
  private String categoryCode;       // 카테고리 코드 (PK)

  private String categoryName;
  private String parentCategoryCode;  // 1차, 2차용
  private String categoryLink;      // 카테고리 링크
}
