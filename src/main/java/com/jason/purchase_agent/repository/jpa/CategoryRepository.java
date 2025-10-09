// src/main/java/com/jason/purchase_agent/repository/jpa/CategoryRepository.java
package com.jason.purchase_agent.repository.jpa;

import com.jason.purchase_agent.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    // 이름/링크에 키워드를 포함하는 모든 카테고리를 조회
    @Query("SELECT c FROM Category c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(c.link) LIKE LOWER(CONCAT('%', :kw, '%'))")
    List<Category> findAllByKeyword(@Param("kw") String kw);

    // 루트 카테고리만 조회
    List<Category> findByParentIsNullOrderByPath();

    @Query("SELECT c FROM Category c WHERE " +
            "c.parent = :parent AND " +
            "c.name = :name AND " +
            "c.link = :link")
    Category findByParentAndNameAndLink(Category parent, String name, String link);

    // 루트(부모X)도 체크
    @Query("SELECT c FROM Category c WHERE " +
            "c.parent IS NULL AND " +
            "c.name = :name AND " +
            "c.link = :link")
    Category findByNameAndLinkAndParentIsNull(String name, String link);





    Category findByNameAndParentIsNull(String name);
    Category findByNameAndParent(String name, Category parent);












    // 부모 하위 customIdx 최댓값 (루트 포함)
    @Query("""
        SELECT MAX(c.customIdx)
        FROM Category c
        WHERE (:parentId IS NULL AND c.parent IS NULL)
           OR (c.parent.id = :parentId)
    """)
    Integer findMaxCustomIdxByParent(@Param("parentId") String parentId);
}
