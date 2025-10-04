package com.jason.purchase_agent.dto.channel.coupang;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoupangCategoryMetaInfoDto {
    @JsonProperty("isAllowSingleItem")
    private boolean isAllowSingleItem;
    private List<AttributeDto> attributes;
    private List<NoticeCategoryDto> noticeCategories;
    private List<RequiredDocumentDto> requiredDocumentNames;
    private List<CertificationDto> certifications;
    private List<String> allowedOfferConditions;
    @JsonProperty("isExpirationDateRequiredForRocketGrowth")
    private boolean isExpirationDateRequiredForRocketGrowth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttributeDto {
        private String attributeTypeName;
        private String dataType;
        private String inputType;
        private List<String> inputValues;
        private String basicUnit;
        private List<String> usableUnits;
        private String required;
        private String groupNumber;
        private String exposed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NoticeCategoryDto {
        private String noticeCategoryName;
        private List<NoticeCategoryDetailNameDto> noticeCategoryDetailNames;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class NoticeCategoryDetailNameDto {
            private String noticeCategoryDetailName;
            private String required;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequiredDocumentDto {
        private String templateName;
        private String required;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CertificationDto {
        private String certificationType;
        private String name;
        private String dataType;
        private String required;
        private List<String> verification_OMIT_LIST;
    }
}
