package com.jason.purchase_agent.controller.products;

import com.jason.purchase_agent.dto.products.*;
import com.jason.purchase_agent.service.products.ProductService;
import com.jason.purchase_agent.messaging.MessageQueueService;

import com.jason.purchase_agent.dto.suppliers.SupplierDto;
import com.jason.purchase_agent.external.coupang.CoupangApiService;
import com.jason.purchase_agent.external.elevenst.ElevenstApiService;
import com.jason.purchase_agent.external.smartstore.SmartstoreApiService;
import com.jason.purchase_agent.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductsController {

    private final ProductService productService;
    private final MessageQueueService messageQueueService;


    private final CoupangApiService coupangApiService;
    private final SmartstoreApiService smartstoreApiService;
    private final ElevenstApiService elevenstApiService;

    /**
     * 상품목록 페이지 (GET) - 필터 파라미터 추가
     */
    @GetMapping("/products/list")
    public String productList(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false) String[] supplierCodes,
            @RequestParam(defaultValue = "false") Boolean filterNullVendorItemId,
            @RequestParam(defaultValue = "false") Boolean filterNullSellerProductId,
            @RequestParam(defaultValue = "false") Boolean filterNullSmartstoreId,
            @RequestParam(defaultValue = "false") Boolean filterNullElevenstId,
            @RequestParam(defaultValue = "createdAt,desc") String sortOrder,
            Model model
    ) {
        log.info("상품목록 페이지 요청 - 검색어: {}, 공급업체: {}, 페이지크기: {}, 페이지번호: {}",
                searchKeyword, supplierCodes, pageSize, pageNumber);

        String[] split = sortOrder.split(",");
        Sort sort = Sort.by(Sort.Direction.fromString(split[1]), split[0]);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // 검색 조건 생성
        ProductSearchDto searchDto = ProductSearchDto.builder()
                .searchKeyword(searchKeyword)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .supplierCodes(supplierCodes != null ? Arrays.asList(supplierCodes) : null)
                .filterNullVendorItemId(filterNullVendorItemId)
                .filterNullSellerProductId(filterNullSellerProductId)
                .filterNullSmartstoreId(filterNullSmartstoreId)
                .filterNullElevenstId(filterNullElevenstId)
                .build();

        // 서비스단에서 pageable로 정렬/페이징!
        Page<ProductDto> productPage = productService.getProductList(searchDto, pageable); // ← 꼭 pageable 넘기기

        // 공급업체 목록 조회 (필터 옵션용)
        List<SupplierDto> suppliers = productService.getSupplierList();

        // 페이지 정보 계산
        int startPage = Math.max(0, pageNumber - 5);
        int endPage = Math.min(productPage.getTotalPages() - 1, pageNumber + 5);

        // 모델에 데이터 추가
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("hasNext", productPage.hasNext());
        model.addAttribute("hasPrevious", productPage.hasPrevious());

        // 필터 관련 데이터 추가
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("selectedSuppliers", supplierCodes);
        model.addAttribute("filterNullVendorItemId", filterNullVendorItemId);
        model.addAttribute("filterNullSellerProductId", filterNullSellerProductId);
        model.addAttribute("filterNullSmartstoreId", filterNullSmartstoreId);
        model.addAttribute("filterNullElevenstId", filterNullElevenstId);

        model.addAttribute("sortOrder", sortOrder);

        return "pages/products/list";  // templates/products/list.html
    }


    /**
     * 공급업체 목록 조회 API (AJAX용)
     */
    @GetMapping("/products/suppliers")
    @ResponseBody
    public ResponseEntity<List<SupplierDto>> getSuppliers() {
        List<SupplierDto> suppliers = productService.getSupplierList();
        return ResponseEntity.ok(suppliers);
    }



    /**
     * 상품목록 데이터 (AJAX용) - 필터 파라미터 추가
     */
    @GetMapping("/products/list/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductListData(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false) String[] supplierCodes,
            @RequestParam(defaultValue = "false") Boolean filterNullVendorItemId,
            @RequestParam(defaultValue = "false") Boolean filterNullSellerProductId,
            @RequestParam(defaultValue = "false") Boolean filterNullSmartstoreId,
            @RequestParam(defaultValue = "false") Boolean filterNullElevenstId,
            @RequestParam(defaultValue = "createdAt,desc", required = false) String sortOrder
    ) {

        ProductSearchDto searchDto = ProductSearchDto.builder()
                .searchKeyword(searchKeyword)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .supplierCodes(supplierCodes != null ? Arrays.asList(supplierCodes) : null)
                .filterNullVendorItemId(filterNullVendorItemId)
                .filterNullSellerProductId(filterNullSellerProductId)
                .filterNullSmartstoreId(filterNullSmartstoreId)
                .filterNullElevenstId(filterNullElevenstId)
                .build();

        String[] split = sortOrder.split(",");
        Sort sort = Sort.by(Sort.Direction.fromString(split[1]), split[0]);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<ProductDto> productPage = productService.getProductList(searchDto, pageable );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", productPage.getContent());
        result.put("currentPage", pageNumber);
        result.put("totalPages", productPage.getTotalPages());
        result.put("totalElements", productPage.getTotalElements());
        result.put("pageSize", pageSize);

        return ResponseEntity.ok(result);
    }

    /**
     * 상품 상세페이지
     */
    @GetMapping("/products/detail/{code}")
    public String productDetail(@PathVariable String code, Model model) {
        log.info("상품 상세페이지 요청 - 코드: {}", code);

        Product product = productService.getProductDetail(code);
        if (product == null) {
            return "redirect:/products/list?error=notfound";
        }

        model.addAttribute("product", product);
        return "pages/products/detail";  // templates/products/detail.html
    }

    /**
     * 상품 가격/재고 일괄 업데이트 (AJAX)
     */
    @PostMapping("/products/bulk-update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkUpdatePriceAndStock(
            @RequestBody ProductUpdateDto updateDto
    ) {
        log.info("상품 가격/재고 일괄 업데이트 요청 - 대상: {}개", updateDto.getUpdateItems().size());

        Map<String, Object> result = new HashMap<>();

        try {
            // Product 테이블에서 업데이트
            productService.updateProductPriceAndStock(updateDto);

            for (ProductUpdateDto.ProductUpdateItemDto item : updateDto.getUpdateItems()) {
                System.out.println("item = " + item);
                coupangApiService.updatePriceStock(item.getVendorItemId(), item.getSalePrice(), item.getStock());
                smartstoreApiService.updatePriceStock(item.getOriginProductNo(), item.getSalePrice(), item.getStock());
                elevenstApiService.updatePriceStock(item.getElevenstId(), item.getSalePrice(), item.getStock());
            }
            
            result.put("success", true);
            result.put("message", "성공적으로 업데이트되었습니다.");
            result.put("updatedCount", updateDto.getUpdateItems().size());
        } catch (Exception e) {
            log.error("상품 업데이트 실패", e);
            result.put("success", false);
            result.put("message", "업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/products/batch-auto-price-stock-update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchAutoPriceStockUpdate(
            @RequestBody BatchAutoPriceStockUpdateRequest request
    ) {

        try {
            // 배치아이디 생성
            String batchId = UUID.randomUUID().toString();

            // 변수 선언
            Integer marginRate = request.getMarginRate();
            Integer couponRate = request.getCouponRate();
            Integer minMarginPrice = request.getMinMarginPrice();
            List<ProductDto> products = request.getProducts();

            // 배치의 상품들 순회하여 메시지 발행 (
            for (ProductDto product : products) {
                // 메시지 빌더
                BatchAutoPriceStockUpdateMessage message = BatchAutoPriceStockUpdateMessage.builder()
                        .batchId(batchId)
                        .requestedBy("ADMIN")
                        .requestedAt(LocalDateTime.now())
                        .marginRate(marginRate)
                        .couponRate(couponRate)
                        .minMarginPrice(minMarginPrice)
                        .productDto(product)
                    .build();

                // 메시지 발행
                messageQueueService.publishProductsBatchAutoPriceStockUpdate(message);
            }
            // 모든 메시지 발행 성공
            return ResponseEntity.ok(
                    Map.of("success", true));
        } catch (Exception e) {

            // 메시지 발행 중 에러 발생
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/products/{code}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(
            @PathVariable String code,
            @ModelAttribute ProductUpdateRequest request,
            @RequestParam(defaultValue = "false") boolean priceChanged,
            @RequestParam(defaultValue = "false") boolean stockChanged,
            @RequestParam(required = false) String batchId // 신규는 null, 재요청은 기존 배치ID
    ) {

        String targetBatchId = productService.updateProductAndMappingWithSync(
                code, request, priceChanged, stockChanged, batchId);


        return ResponseEntity.ok(Map.of(
                "success", true,
                "batchId", targetBatchId,
                "message", "수정 요청 접수. 실제 반영 여부는 이력에서 확인"
        ));
    }

}
