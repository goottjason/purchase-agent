// src/main/java/com/jason/purchase_agent/service/CategoryService.java
package com.jason.purchase_agent.service;

import com.jason.purchase_agent.dto.CategoryForm;
import com.jason.purchase_agent.entity.Category;
import com.jason.purchase_agent.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  // 유틸: 세그먼트 4자리 0패딩
  private String seg(int n) {
    return String.format("%04d", n);
  }

  // 유틸: 새 자식 세그먼트 결정 (형제들 중 마지막 세그먼트 최댓값 + 1)
  private String nextChildSegmentUnder(Category parent) {
    List<Category> siblings = (parent == null)
      ? categoryRepository.findByParentIsNullOrderByPathAsc()
      : categoryRepository.findByParent_IdOrderByPathAsc(parent.getId());

    int max = 0;
    for (Category c : siblings) {
      String[] parts = c.getPath().split("/");
      // 마지막 유효 세그먼트 추출
      for (int i = parts.length - 1; i >= 0; i--) {
        if (!parts[i].isBlank()) {
          try {
            max = Math.max(max, Integer.parseInt(parts[i]));
          } catch (NumberFormatException ignored) {}
          break;
        }
      }
    }
    return seg(max + 1);
  }

  private String buildPath(Category parent, String childSeg) {
    if (parent == null) {
      return "/" + childSeg;
    }
    return parent.getPath() + "/" + childSeg;
  }

  @Transactional
  public Category create(CategoryForm form) {
    Category parent = null;
    if (form.getParentId() != null && !form.getParentId().isBlank()) {
      parent = categoryRepository.findById(form.getParentId())
        .orElseThrow(() -> new EntityNotFoundException("부모를 찾을 수 없습니다: " + form.getParentId()));
    }
    String seg = nextChildSegmentUnder(parent);
    String path = buildPath(parent, seg);

    Category c = new Category();
    c.setParent(parent);
    c.setPath(path);
    c.setCode(form.getCode());
    c.setEngName(form.getEngName());
    c.setKorName(form.getKorName());
    c.setLink(form.getLink());
    return categoryRepository.save(c);
  }

  @Transactional(readOnly = true)
  public Page<Category> list(String keyword, Pageable pageable) {
    if (keyword == null || keyword.isBlank()) {
      return categoryRepository.findAll(PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by("path").ascending()
      ));
    }
    return categoryRepository
      .findByCodeContainingIgnoreCaseOrEngNameContainingIgnoreCaseOrKorNameContainingIgnoreCaseOrPathContainingIgnoreCase(
        keyword, keyword, keyword, keyword, pageable
      );
  }

  @Transactional(readOnly = true)
  public Category get(String id) {
    return categoryRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다: " + id));
  }

  @Transactional
  public Category update(CategoryForm form) {
    if (form.getId() == null || form.getId().isBlank()) {
      throw new IllegalArgumentException("수정에는 id가 필요합니다.");
    }
    Category entity = get(form.getId());

    // 부모 변경 여부 확인
    String newParentId = (form.getParentId() == null || form.getParentId().isBlank()) ? null : form.getParentId();
    String oldParentId = (entity.getParent() == null) ? null : entity.getParent().getId();

    Category newParent = null;
    if (newParentId != null) {
      newParent = categoryRepository.findById(newParentId)
        .orElseThrow(() -> new EntityNotFoundException("부모를 찾을 수 없습니다: " + newParentId));
    }

    // 부모가 바뀌면 이 노드의 path 재계산 (자손 경로 일괄 변경은 별도 배치/확장 과제)
    if (!java.util.Objects.equals(newParentId, oldParentId)) {
      String seg = nextChildSegmentUnder(newParent);
      String newPath = buildPath(newParent, seg);
      entity.setParent(newParent);
      entity.setPath(newPath);
    }

    entity.setCode(form.getCode());
    entity.setEngName(form.getEngName());
    entity.setKorName(form.getKorName());
    entity.setLink(form.getLink());
    return entity;
  }

  @Transactional
  public void delete(String id) {
    if (categoryRepository.existsByParent_Id(id)) {
      throw new IllegalStateException("하위 카테고리가 있어 삭제할 수 없습니다.");
    }
    categoryRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<Category> allForParentSelect() {
    // 드롭다운용: 경로 순으로 전체 나열
    return categoryRepository.findAll(Sort.by("path").ascending());
  }
}
