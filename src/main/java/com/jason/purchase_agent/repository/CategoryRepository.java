// src/main/java/com/jason/purchase_agent/repository/CategoryRepository.java
package com.jason.purchase_agent.repository;

import com.jason.purchase_agent.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String> {

  // 자식 존재 여부(삭제 가드)
  boolean existsByParent_Id(String parentId);

  // 특정 부모의 직계 자식
  List<Category> findByParent_IdOrderByPathAsc(String parentId);

  // 루트 노드들
  List<Category> findByParentIsNullOrderByPathAsc();

  // 간단 키워드 검색 (code/engName/korName/path)
  Page<Category>
  findByCodeContainingIgnoreCaseOrEngNameContainingIgnoreCaseOrKorNameContainingIgnoreCaseOrPathContainingIgnoreCase(
    String code, String engName, String korName, String path, Pageable pageable
  );
}
