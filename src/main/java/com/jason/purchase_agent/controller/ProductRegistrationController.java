package com.jason.purchase_agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.categories.CategoryTreeDto;
import com.jason.purchase_agent.enums.JobType;
import com.jason.purchase_agent.messaging.MessageQueueService;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import com.jason.purchase_agent.repository.ProcessStatusRepository;
import com.jason.purchase_agent.repository.ProductRepository;
import com.jason.purchase_agent.service.product_registration.ProductRegistrationService;
import com.jason.purchase_agent.external.iherb.dto.IherbProductDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProductRegistrationController {

    private final ProductRegistrationService productRegistrationService;
    private final MessageQueueService messageQueueService;
    private final ProductRepository productRepository;
    private final ProcessStatusRepository psr;
    private final ObjectMapper objectMapper;

    // [STEP 1] 카테고리 목록 조회 (CategoryController 처리)
    @GetMapping("/product-registration")
    public String productRegistrationPage() {
        // 프론트에서 loadCategoryTree() 호출 => GET("/categories/tree") 요청
        return "pages/product-registration";
    }

    // [STEP 2] 사용자가 카테고리/상품수 선택했을 때 상품 링크수집
    @PostMapping("/product-registration/step2-fetch-products")
    @ResponseBody
    public List<IherbProductDto> fetchProducts(
            @RequestBody List<CategoryTreeDto> categoryTreeDtos, HttpSession session
    ) {
        log.info("■ [STEP 2] 사용자가 카테고리/상품수 선택했을 때 상품 링크수집");
        log.info("■ [STEP 2] 컨트롤러 PARAMETER :: List<CategoryTreeDto> categoryTreeDtos = {}", categoryTreeDtos);

        // iHerb에서 사용자가 선택한 카테고리/상품수에 해당하는 상품정보를 수집하여 반환
        List<IherbProductDto> iherbProductDtos = productRegistrationService.fetchProducts(categoryTreeDtos);

        // 3단계에서 다시 크롤링하지 않도록 세션에 세팅
        session.setAttribute("iherbProductDtos", iherbProductDtos);
        return iherbProductDtos;
    }

    /**
     * [STEP 3]
     * 선택된 상품 ID(Iherb 고유코드) 리스트를 받아, 세션에 미리 저장한 전체 상품 목록에서
     * 해당 상품들만 추출하여 후처리/상품등록 작업에 활용할 준비를 하는 메서드.
     *
     * @param selectedIds 클라이언트가 선택한 상품 고유번호 문자열(List<String>; ex. ["123456","234567"...])
     * @param session   사용자별 세션(2단계에서 product 목록을 임시 보관)
     * @return (추후 용도에 따라) 오픈마켓용 상품 변환 결과 등을 담을 map
     */
    @PostMapping("/product-registration/step3-prepare-products")
    @ResponseBody
    public List<ProductRegistrationRequest> step3PrepareProducts(
            @RequestBody List<String> selectedIds, HttpSession session
    ) {

        // --- 세션에서 iherbProductDtos(2단계의 전체 상품 DTO 리스트) 불러오기
        List<IherbProductDto> iherbProductDtos = (List<IherbProductDto>) session.getAttribute("iherbProductDtos");

        // --- 전체상품 리스트 → [id : 상품DTO] 맵핑 (고성능 탐색/검색용)
        Map<Long, IherbProductDto> iherbProductDtosMap = iherbProductDtos
                .stream()
                .collect(Collectors.toMap(IherbProductDto::getId, Function.identity()));

        // --- 사용자가 선택한 ID에 해당하는 상품만 골라내기 ---
        List<IherbProductDto> selectedIherbProductDtos = selectedIds
                .stream()
                .map(Long::valueOf)                     // String → Long 변환
                .map(iherbProductDtosMap::get)          // productMap.get(id)로 찾아 상품 DTO 얻기
                .filter(Objects::nonNull)               // 존재하지 않는 항목(=null) 제거
                .collect(Collectors.toList());          // 리스트로 모음

        // --- 후처리/상품등록에 활용할 수 있도록 오픈마켓용 상품 DTO로 변환 ---
        List<ProductRegistrationRequest> productRegistrationRequests =
                productRegistrationService.convertToProductRegistrationDto(selectedIherbProductDtos);

        session.setAttribute("productRegistrationRequests", productRegistrationRequests);

        return productRegistrationRequests;
    }

    /**
     * [STEP 4] 최종 상품등록 요청
     * - 클라이언트에서 전달한 상품목록을 MQ에 발행
     * - DB에 배치 상태 초기레코드 등록
     * - API 응답으로 배치ID 반환
     *
     * @param productRegistrationRequests - 클라이언트에서 전달한 최종 상품목록
     * @return 배치ID를 포함한 처리결과
     */
    @PostMapping("/product-registration/step4-submit-products")
    @ResponseBody
    public ResponseEntity<?> registerProducts (
            @RequestBody List<ProductRegistrationRequest> productRegistrationRequests
    ) {
        // batchId 생성 + 이미지 로컬에 다운로드 + 이미지 ESM에 업로드 + 메세지 발행
        // 메세지 컨슈머에서, Product에 저장 및 각 채널에 상품등록 후 Mapping에 저장
        productRegistrationService.registerProducts(JobType.REGISTER_PRODUCT, productRegistrationRequests);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "상픔 등록 요청. 실제 반영 여부는 이력에서 확인."
        ));
    }



}
