package com.jason.purchase_agent.dto.channel.smartstore;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor
@AllArgsConstructor
public class SmartstoreCategoryResult {
    /**
     * 전체 카테고리명 (예: "패션잡화 > 신발 > 운동화")
     */
    @JsonProperty("wholeCategoryName")
    private String wholeCategoryName;

    /**
     * 카테고리 ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 카테고리명 (현재 카테고리의 이름만)
     */
    @JsonProperty("name")
    private String name;

    /**
     * 리프 카테고리 여부 (최종 종단 카테고리인지)
     */
    @JsonProperty("last")
    private Boolean last;

    /**
     * 리프 카테고리인지 확인하는 편의 메서드
     */
    public boolean isLeafCategory() {
        return last != null && last;
    }

    /**
     * 상품 등록에 사용 가능한 카테고리인지 확인
     */
    public boolean isValidForProductRegistration() {
        return isLeafCategory();
    }

    public String getLeafCategoryId() {
        return isLeafCategory() ? id : null;
    }
}
