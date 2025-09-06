package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.dto.ProductDto;
import com.jason.purchase_agent.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  // 상품 목록
  /*@GetMapping
  public String list(Model model) {
    List<ProductDto> products = productService.getAllProductDtos();
    model.addAttribute("products", products);
    return "admin/products/list";
  }*/
  @GetMapping
  public String list(
    @RequestParam(value = "page", defaultValue = "0") int page,
    @RequestParam(value = "size", defaultValue = "50") int size,
    @RequestParam(value = "search", required = false) String search,
    @RequestParam(value = "sort", defaultValue = "productCode,asc") String sort,
    Model model
  ) {
    // sort 파라미터 예: "korName,desc"
    String[] sortInfo = sort.split(",");
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortInfo[1]), sortInfo[0]));

    Page<ProductDto> products = productService.getProductPage(search, pageable);

    model.addAttribute("products", products);
    model.addAttribute("search", search);
    model.addAttribute("size", size);
    model.addAttribute("sort", sort);
    return "admin/products/list";
  }

  // 엑셀 업로드 폼
  @GetMapping("/excel-upload")
  public String excelUploadForm() {
    return "admin/products/excel-upload";
  }

  // 엑셀 업로드 실행
  @PostMapping("/excel-upload")
  public String handleExcelUpload(@RequestParam("file") MultipartFile file, Model model) {
    System.out.println("Received file: " + file.getOriginalFilename());
    String result = productService.saveProductsFromExcel(file);
    model.addAttribute("result", result);
    return "admin/products/excel-upload";
  }

  // 체크박스 삭제
  @PostMapping("/delete-selected")
  public String deleteSelected(@RequestParam("productCodes") List<String> productCodes) {
    productService.deleteProducts(productCodes);
    return "redirect:/admin/products";
  }
}






//package com.jason.purchase_agent.controller;
//
//import com.jason.purchase_agent.dto.ProductDto;
//import com.jason.purchase_agent.service.ProductService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//@RequestMapping("/admin/products")
//@RequiredArgsConstructor
//public class ProductController {
//
//  private final ProductService productService;
////
//  @GetMapping
//  public String productList(
//    @RequestParam(defaultValue = "0") int page,
//    @RequestParam(defaultValue = "50") int size,
//    @RequestParam(required = false) String searchKeyword,
//    @RequestParam(required = false) String supplierCode,
//    Model model) {
//
//    Page<ProductDto> products = productService.getProducts(page, size, searchKeyword, supplierCode);
//
//    model.addAttribute("products", products);
//    model.addAttribute("currentPage", page);
//    model.addAttribute("pageSize", size);
//    model.addAttribute("searchKeyword", searchKeyword);
//    model.addAttribute("supplierCode", supplierCode);
//    model.addAttribute("pageSizes", Arrays.asList(50, 100, 250, 500));
//
//    return "admin/products/list";
//  }
//
//  @PostMapping("/upload")
//  @ResponseBody
//  public ResponseEntity<Map<String, Object>> uploadExcel(
//    @RequestParam("file") MultipartFile file) {
//
//    Map<String, Object> response = new HashMap<>();
//
//    try {
//      List<String> errors = productService.uploadProductsFromExcel(file);
//
//      response.put("success", true);
//      response.put("message", "업로드가 완료되었습니다.");
//      if (!errors.isEmpty()) {
//        response.put("errors", errors);
//      }
//
//      return ResponseEntity.ok(response);
//    } catch (Exception e) {
//      response.put("success", false);
//      response.put("message", "업로드 중 오류가 발생했습니다: " + e.getMessage());
//      return ResponseEntity.badRequest().body(response);
//    }
//  }
//}