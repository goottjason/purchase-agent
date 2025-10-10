package com.jason.purchase_agent.dto.categories;

import lombok.*;

import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@ToString
public class CategoryTreeDto {
    private String id;
    private String name;
    private String link;
    private String path;
    private List<CategoryTreeDto> children;

    private Integer productCount;  // 선택한 상품 수량
    private Integer sortOrder;
    private List<String> productIds; //
}