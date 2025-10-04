package com.jason.purchase_agent.dto.categories;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class CategoryCreateDto {
    @NotBlank(message = "카테고리의 계층명은 필수입니다.")
    @Size(max = 200, message = "한글명은 200자 이내여야 합니다.")
    private String categoryPath;

    @NotBlank(message = "링크는 필수입니다.")
    @Size(max = 500, message = "링크는 500자 이내여야 합니다.")
    private String link;
}