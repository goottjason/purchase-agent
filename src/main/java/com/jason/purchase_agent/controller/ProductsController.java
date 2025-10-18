package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.dto.products.*;
import com.jason.purchase_agent.enums.JobType;
import com.jason.purchase_agent.enums.UpdateType;
import com.jason.purchase_agent.external.iherb.IherbProductCrawler;
import com.jason.purchase_agent.repository.ProductRepository;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductsController {

    private final ProductService productService;
    private final MessageQueueService messageQueueService;
    private final ProductRepository productRepository;

    private final CoupangApiService coupangApiService;
    private final SmartstoreApiService smartstoreApiService;
    private final ElevenstApiService elevenstApiService;
    private final IherbProductCrawler iherbProductCrawler;
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
    // @GetMapping("/products/suppliers")
    // @ResponseBody
    // public ResponseEntity<List<SupplierDto>> getSuppliers() {
    //     List<SupplierDto> suppliers = productService.getSupplierList();
    //     return ResponseEntity.ok(suppliers);
    // }



    /**
     * 상품목록 데이터 (AJAX용) - 필터 파라미터 추가
     */
    // @GetMapping("/products/list/data")
    // @ResponseBody
    // public ResponseEntity<Map<String, Object>> getProductListData(
    //         @RequestParam(required = false) String searchKeyword,
    //         @RequestParam(defaultValue = "50") Integer pageSize,
    //         @RequestParam(defaultValue = "0") Integer pageNumber,
    //         @RequestParam(required = false) String[] supplierCodes,
    //         @RequestParam(defaultValue = "false") Boolean filterNullVendorItemId,
    //         @RequestParam(defaultValue = "false") Boolean filterNullSellerProductId,
    //         @RequestParam(defaultValue = "false") Boolean filterNullSmartstoreId,
    //         @RequestParam(defaultValue = "false") Boolean filterNullElevenstId,
    //         @RequestParam(defaultValue = "createdAt,desc", required = false) String sortOrder
    // ) {
    //
    //     ProductSearchDto searchDto = ProductSearchDto.builder()
    //             .searchKeyword(searchKeyword)
    //             .pageSize(pageSize)
    //             .pageNumber(pageNumber)
    //             .supplierCodes(supplierCodes != null ? Arrays.asList(supplierCodes) : null)
    //             .filterNullVendorItemId(filterNullVendorItemId)
    //             .filterNullSellerProductId(filterNullSellerProductId)
    //             .filterNullSmartstoreId(filterNullSmartstoreId)
    //             .filterNullElevenstId(filterNullElevenstId)
    //             .build();
    //
    //     String[] split = sortOrder.split(",");
    //     Sort sort = Sort.by(Sort.Direction.fromString(split[1]), split[0]);
    //     Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
    //
    //     Page<ProductDto> productPage = productService.getProductList(searchDto, pageable );
    //
    //     Map<String, Object> result = new HashMap<>();
    //     result.put("success", true);
    //     result.put("products", productPage.getContent());
    //     result.put("currentPage", pageNumber);
    //     result.put("totalPages", productPage.getTotalPages());
    //     result.put("totalElements", productPage.getTotalElements());
    //     result.put("pageSize", pageSize);
    //
    //     return ResponseEntity.ok(result);
    // }

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











    // ■■■■■ 상품의 판매가격/재고 수동 설정 - 변경사항 저장
    @PostMapping("/products/modify-and-update-price-stock")
    @ResponseBody
    public ResponseEntity<?> modifyAndUpdatePriceStock(
            @RequestBody List<ProductUpdateRequest> requests
    ) {
        String targetBatchId = productService.updateProductsBatch(requests, null);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "batchId", targetBatchId,
                "message", "수정 요청 접수. 실제 반영 여부는 이력에서 확인"
        ));
    }

    // ■■■■■ 크롤링 후 가격/재고 업데이트 (마진율/쿠폰할인율/최소마진 설정 - 가격/재고 업데이트)
    @PostMapping("/products/crawl-and-update-price-stock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crawlAndPriceStockUpdate (
            @RequestParam int marginRate,
            @RequestParam int couponRate,
            @RequestParam int minMarginPrice,
            @RequestBody List<ProductUpdateRequest> requests
    ) {
        // productService.crawlAndUpdatePriceStock(marginRate, couponRate, minMarginPrice, requests);
        productService.processProductUpdate(JobType.CRAWL_AND_UPDATE_PRICE_STOCK, marginRate, couponRate, minMarginPrice, requests);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수정 요청 접수. 실제 반영 여부는 이력에서 확인"
        ));
    }
    // ■■■■■ 수동 가격/재고만 업데이트
    @PostMapping("/products/manual-update-price-stock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> manualUpdatePriceStock(
            @RequestBody List<ProductUpdateRequest> requests
    ) {
        productService.processProductUpdate(JobType.MANUAL_UPDATE_PRICE_STOCK,
                null, null, null, requests);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수정 요청 접수. 실제 반영 여부는 이력에서 확인"
        ));
    }
    // ■■■■■ 수동 전체 필드 업데이트
    @PostMapping("/products/manual-update-all-fields")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> manualUpdateAllFields(
            @RequestBody List<ProductUpdateRequest> requests
    ) {
        productService.processProductUpdate(JobType.MANUAL_UPDATE_ALL_FIELDS,
                null, null, null, requests);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수정 요청 접수. 실제 반영 여부는 이력에서 확인"
        ));
    }

    @PutMapping("/products/{productCode}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(
            @PathVariable("productCode") String code,
            @ModelAttribute ProductDto productDto,
            @RequestParam(defaultValue = "false") boolean priceChanged,
            @RequestParam(defaultValue = "false") boolean stockChanged
    ) {
        productDto.setCode(code);

        log.info("productDto: {}", productDto);
        // 단일 상품은 리스트로 감싸서 배치 메서드 호출
        List<ProductUpdateRequest> requests = Collections.singletonList(
                new ProductUpdateRequest(code, productDto, priceChanged, stockChanged)
        );

        productService.processProductUpdate(
                JobType.MANUAL_UPDATE_ALL_FIELDS,
                null, null, null, requests);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수정 요청 접수. 실제 반영 여부는 이력에서 확인"
        ));
    }

}
