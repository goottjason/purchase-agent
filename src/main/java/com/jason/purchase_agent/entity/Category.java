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
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"parent", "children"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "custom_idx", nullable = false)
    private int customIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_id",
            referencedColumnName = "id",
            nullable = true,
            foreignKey = @ForeignKey(name = "fk_category_parent")
    )
    private Category parent;

    @OneToMany(
            mappedBy = "parent",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = false
    )
    private List<Category> children = new ArrayList<>();

    @Column(name = "path", nullable = false, length = 20)
    private String path;

    // korName -> name (한글 이름만 유지)
    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "link", length = 500)
    private String link;

    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Category child) {
        children.remove(child);
        child.setParent(null);
    }
}
