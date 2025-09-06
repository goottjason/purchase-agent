// src/main/java/com/jason/purchase_agent/controller/CategoryController.java
package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.dto.CategoryForm;
import com.jason.purchase_agent.entity.Category;
import com.jason.purchase_agent.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public String list(@RequestParam(value = "q", required = false) String q,
                     Pageable pageable,
                     Model model) {
    Page<Category> page = categoryService.list(q, pageable);
    model.addAttribute("page", page);
    model.addAttribute("q", q == null ? "" : q);
    return "category/list";
  }

  @GetMapping("/new")
  public String createForm(Model model) {
    model.addAttribute("form", new CategoryForm());
    List<Category> parents = categoryService.allForParentSelect();
    model.addAttribute("parents", parents);
    return "category/form";
  }

  @PostMapping
  public String create(@ModelAttribute("form") CategoryForm form,
                       BindingResult bindingResult,
                       RedirectAttributes ra,
                       Model model) {
    try {
      categoryService.create(form);
      ra.addFlashAttribute("msg", "카테고리를 등록했습니다.");
      return "redirect:/categories";
    } catch (Exception e) {
      bindingResult.reject("createError", e.getMessage());
      model.addAttribute("parents", categoryService.allForParentSelect());
      return "category/form";
    }
  }

  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable String id, Model model) {
    Category c = categoryService.get(id);
    CategoryForm form = new CategoryForm();
    form.setId(c.getId());
    form.setParentId(c.getParent() == null ? null : c.getParent().getId());
    form.setCode(c.getCode());
    form.setEngName(c.getEngName());
    form.setKorName(c.getKorName());
    form.setLink(c.getLink());
    model.addAttribute("form", form);
    model.addAttribute("parents", categoryService.allForParentSelect());
    return "category/form";
  }

  @PostMapping("/{id}")
  public String update(@PathVariable String id,
                       @ModelAttribute("form") CategoryForm form,
                       BindingResult bindingResult,
                       RedirectAttributes ra,
                       Model model) {
    try {
      form.setId(id);
      categoryService.update(form);
      ra.addFlashAttribute("msg", "카테고리를 수정했습니다.");
      return "redirect:/categories";
    } catch (Exception e) {
      bindingResult.reject("updateError", e.getMessage());
      model.addAttribute("parents", categoryService.allForParentSelect());
      return "category/form";
    }
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable String id, RedirectAttributes ra) {
    try {
      categoryService.delete(id);
      ra.addFlashAttribute("msg", "카테고리를 삭제했습니다.");
    } catch (Exception e) {
      ra.addFlashAttribute("err", e.getMessage());
    }
    return "redirect:/categories";
  }
}
