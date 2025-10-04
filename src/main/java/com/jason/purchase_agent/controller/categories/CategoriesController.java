package com.jason.purchase_agent.controller.categories;

import com.jason.purchase_agent.dto.categories.CategoryCreateDto;
import com.jason.purchase_agent.dto.categories.CategoryTreeDto;
import com.jason.purchase_agent.dto.categories.CategoryUpdateDto;
import com.jason.purchase_agent.service.categories.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public String categoriesPage() { return "pages/categories"; }

    // 카테고리 조회
    @GetMapping("/categories/tree")
    @ResponseBody
    public List<CategoryTreeDto> getCategoryTree(
            @RequestParam(value = "keyword", required = false) String keyword) {
        return categoryService.getCategoryTree(keyword);
    }

    // 카테고리 신규등록
    @PostMapping("/admin/categories/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCategory(
            @Valid @RequestBody CategoryCreateDto dto,
            BindingResult bindingResult
    ) {
        Map<String, Object> response = new HashMap<>();

        // 1. 입력값 유효성 오류(프론트에서 required 등 체크 실패)
        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "입력값을 확인해주세요.");
            response.put("errors", bindingResult.getFieldErrors());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            categoryService.createCategory(dto);  // 정상 등록 시도
            response.put("success", true);
            response.put("message", "카테고리가 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // 2. 중복 카테고리 등 "검증 실패" 로직(400 or 409)
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (IllegalArgumentException e) {
            // 3. 잘못된 요청(비즈니스상 허용 실패) - 400
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            // 4. 서버 내부 처리 오류
            log.error("카테고리 등록 실패", e);
            response.put("success", false);
            response.put("message", "서버 오류로 등록 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/admin/categories/upload-excel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadCategoryExcel(@RequestParam("excelFile") MultipartFile excelFile) {
        Map<String, Object> response = new HashMap<>();
        try {
            int count = categoryService.uploadCategoryExcel(excelFile);
            response.put("success", true);
            response.put("message", "엑셀에서 " + count + "건이 등록되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalStateException | IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "엑셀 등록 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/admin/categories/{categoryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable String categoryId,
            @RequestBody CategoryUpdateDto updateDto) {
        Map<String, Object> result = new HashMap<>();
        try {
            categoryService.updateCategory(categoryId, updateDto);
            result.put("message", "수정 완료");
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            result.put("message", "수정 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 카테고리 삭제 API
     */
    @DeleteMapping("/admin/categories/{categoryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable String categoryId) {
        Map<String, Object> result = new HashMap<>();
        try {
            categoryService.deleteCategory(categoryId);
            result.put("message", "삭제 완료");
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            result.put("message", "삭제 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
