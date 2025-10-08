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
     * 상품 가격/재고 업데이트 (유저가 직접 가격/재고 수정)
     */
    @PostMapping("/products/bulk-update")
    @ResponseBody
    public ResponseEntity<?> bulkUpdatePriceAndStock(
            @RequestBody ProductUpdateDto updateDto
    ) {
        log.info("상품 가격/재고 일괄 업데이트 요청 - 대상: {}개",
                updateDto.getUpdateItems().size());

        List<String> resultCodes = new ArrayList<>();
        for (ProductUpdateDto.ProductUpdateItemDto item : updateDto.getUpdateItems()) {
            // ProductUpdateItemDto → ProductUpdateRequest로 변환
            ProductUpdateRequest req = convertToRequest(item);
            String targetBatchId = productService.updateProductAndMappingWithSync(
                    req.getCode(), req, item.isPriceChanged(), item.isStockChanged(), null
            );
            resultCodes.add(req.getCode());
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "codes", resultCodes,
                "message", "일괄 수정 요청 접수. 실제 반영 여부는 이력에서 확인"
        ));
    }

    private ProductUpdateRequest convertToRequest(
            ProductUpdateDto.ProductUpdateItemDto item
    ) {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setCode(item.getCode());
        req.setSalePrice(item.getSalePrice());
        req.setStock(item.getStock());
        req.setSellerProductId(item.getSellerProductId());
        req.setVendorItemId(item.getVendorItemId());
        req.setSmartstoreId(item.getSmartstoreId());
        req.setOriginProductNo(item.getOriginProductNo());
        req.setElevenstId(item.getElevenstId());
        // 필요한 필드 계속 복사 (DTO 구조에 맞게 수정)
        return req;
    }

    @PostMapping("/products/calculated-price-update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> calculatedPriceUpdate (
            @RequestBody BatchAutoPriceStockUpdateRequest request
    ) {
        List<String> successCodes = new ArrayList<>();
        List<String> failCodes = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        // String batchId = request.getBatchId(); // 신규 or null은 서비스에서 처리

        for (ProductDto dto: request.getProducts()) {
            try {
                // STEP 1. 크롤링해서 IherbProductDto를 반환 받는다
                // STEP 2. 만들어진 calculateSalePrice로 salePrice를 계산한다.
                // STEP 3. IherbProductDto에서 isAvailableToPurchase에 맞춰서 계산한다.
                // STEP 4. updateProductAndMappingWithSync에 보낸다.

/*                // STEP 1. 크롤링 (재고/구매가 등) - 동기 or 비동기 처리
                Integer crawledStock = crawlingService.getStock(item.getLink(), ...);
                Integer crawledBuyPrice = crawlingService.getBuyPrice(item.getLink(), ...);

                // STEP 2. salePrice/stock 계산 (비즈니스 로직)
                Integer calculatedSalePrice = calculateSalePrice(crawledBuyPrice, request.getMarginRate(), ...);
                Integer calculatedStock = calculateAvailableStock(crawledStock, ...);

                // STEP 3. Update DTO 생성 후 updateProductAndMappingWithSync 호출
                ProductUpdateRequest req = new ProductUpdateRequest();
                req.setCode(item.getCode());
                req.setBuyPrice(crawledBuyPrice);
                req.setSalePrice(calculatedSalePrice);
                req.setStock(calculatedStock);

                productService.updateProductAndMappingWithSync(
                        req.getCode(), req, true, true, null
                );

                successCodes.add(item.getCode());*/
            } catch (Exception ex) {
                failCodes.add(item.getCode());
                messages.add(item.getCode() + ": " + ex.getMessage());
                // 로깅 등
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", failCodes.isEmpty());
        result.put("batchId", batchId);
        result.put("successCodes", successCodes);
        result.put("failCodes", failCodes);
        result.put("messages", messages);

        return ResponseEntity.ok(result);
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
