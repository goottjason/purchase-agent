// src/main/java/com/jason/purchase_agent/dto/CategoryForm.java
package com.jason.purchase_agent.dto;

import lombok.Data;

@Data
public class CategoryForm {
  private String id;         // 수정 시에만 사용 (신규는 null)
  private String parentId;   // 부모 선택 (루트면 null)
  private String code;       // 슬러그 (중복 허용)
  private String engName;    // 영어명
  private String korName;    // 한국어명
  private String link;       // URL
}
