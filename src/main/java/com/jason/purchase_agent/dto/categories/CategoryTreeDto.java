package com.jason.purchase_agent.dto.categories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class CategoryTreeDto {
    private String id;
    private String name;
    private String link;
    private String path;
    private List<CategoryTreeDto> children;

    // 추가: 상품 수집용 필드
    private Integer productCount;  // 선택한 상품 수량
    private Integer sortOrder;
    private List<String> productIds;
}