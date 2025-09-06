package com.jason.purchase_agent.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(
  name = "category",
  indexes = {
    @Index(name = "idx_path", columnList = "path"),
    @Index(name = "idx_parent_id", columnList = "parent_id")
  }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"parent", "children"})
public class Category {

  // PK: UUID 문자열(36자), 저장 후 변경 불가
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false, length = 36)
  private String id;

  // 자식 → 부모 단방향 참조(자기참조), FK는 parent_id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
    name = "parent_id",
    referencedColumnName = "id",
    nullable = true,
    foreignKey = @ForeignKey(name = "fk_category_parent")
  )
  private Category parent;

  // 부모 → 자식들(읽기/동기화 컬렉션), FK는 자식 쪽 parent_id가 소유
  @OneToMany(
    mappedBy = "parent",
    cascade = {
      CascadeType.PERSIST,
      CascadeType.MERGE,
      CascadeType.REFRESH,
      CascadeType.DETACH
    },
    orphanRemoval = false
  )
  private List<Category> children = new ArrayList<>();

  // 전체 경로(Materialized Path) 예: "/0001/0002/0004"
  @Column(name = "path", nullable = false, length = 20)
  private String path;

  // SEO 슬러그(중복 허용) 예: "candida-yeast-treatments"
  @Column(name = "code", length = 200)
  private String code;

  // 영어 표시 이름
  @Column(name = "eng_name", length = 200)
  private String engName;

  // 한국어 표시 이름
  @Column(name = "kor_name", length = 200)
  private String korName;

  // 원본 링크(URL)
  @Column(name = "link", length = 500)
  private String link;

  // 편의 메서드: 양방향 관계를 한 번에 맞춤
  public void addChild(Category child) {
    children.add(child);
    child.setParent(this);
  }

  public void removeChild(Category child) {
    children.remove(child);
    child.setParent(null);
  }
}
