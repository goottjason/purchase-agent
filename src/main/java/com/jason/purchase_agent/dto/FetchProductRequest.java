package com.jason.purchase_agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 2단계 요청(선택카테고리+개수)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchProductRequest {
    private List<CategorySelect> categories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySelect {
        private String id;
        private int count;
    }
}