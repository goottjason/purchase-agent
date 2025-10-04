package com.jason.purchase_agent.dto.categories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class CategoryUpdateDto {
    private String name; // 한글명
    private String link;
}
