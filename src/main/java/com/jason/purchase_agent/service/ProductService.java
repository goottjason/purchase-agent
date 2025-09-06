package com.jason.purchase_agent.service;

import com.jason.purchase_agent.dto.ProductDto;
import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.repository.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;

  public List<ProductDto> getAllProductDtos() {
    List<Product> products = productRepository.findAll();
    List<ProductDto> list = new ArrayList<>();
    for (Product p : products) {
      list.add(ProductDto.fromEntity(p));
    }
    return list;
  }

  // ===== 엑셀 업로드 구현 =====
  @Transactional
  public String saveProductsFromExcel(MultipartFile file) {
    int inserted = 0, updated = 0;
    try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
      Sheet sheet = workbook.getSheetAt(0);
      int rowNum = 0;
      for (Row row : sheet) {
        if (rowNum++ == 0) continue; // 헤더스킵
        // 이 컬럼 순서는 엑셀 시트 순서에 맞게 수정
        String productCode = getString(row.getCell(2));
        if (productCode == null || productCode.isBlank()) continue;

        Product entity = productRepository.findById(productCode).orElse(new Product());
        entity.setProductCode(productCode);
        entity.setSupplierCode(getString(row.getCell(0)));
        entity.setIsAvailable("1".equals(getString(row.getCell(1))));
        entity.setSourceLink(getString(row.getCell(3)));
        entity.setCategoryCode(getString(row.getCell(4)));
        entity.setKorName(getString(row.getCell(5)));
        entity.setEngName(getString(row.getCell(6)));
        entity.setUnitValue(toBigDecimal(row.getCell(7)));
        entity.setUnit(getString(row.getCell(8)));
        entity.setDetailsHtml(getString(row.getCell(9)));
        entity.setPurchaseCost(toBigDecimal(row.getCell(10)));
        entity.setPackSize(toInt(row.getCell(11)));
        entity.setShippingCost(toBigDecimal(row.getCell(12)));

        boolean isUpdate = productRepository.existsById(productCode);
        productRepository.save(entity);
        if (isUpdate)
          updated++;
        else
          inserted++;
      }
    } catch (Exception e) {
      return "엑셀 처리 오류: " + e.getMessage();
    }
    return "신규 등록: " + inserted + "건\n수정: " + updated + "건";
  }

  // ===== 체크박스 일괄 삭제 =====
  @Transactional
  public void deleteProducts(List<String> productCodes) {
    for (String code : productCodes) productRepository.deleteById(code);
  }

  // 유틸 (엑셀 cell 파싱)
  /*private String getString(Cell cell) {
    if (cell == null) return null;
    return cell.getCellType() == CellType.NUMERIC ? String.valueOf((long)cell.getNumericCellValue()) : cell.getStringCellValue();
  }*/
  private String getString(Cell cell) {
    if (cell == null) return null;
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        // 숫자 셀은 문자열로 변환
        double n = cell.getNumericCellValue();
        // 소숫점 0만 있으면 정수로 처리
        return (n == Math.floor(n)) ? String.valueOf((long)n) : String.valueOf(n);
      case FORMULA:
        // 수식 셀의 유형도 추가적으로 체크!
        return switch (cell.getCachedFormulaResultType()) {
          case STRING -> cell.getStringCellValue();
          case NUMERIC -> {
            double f = cell.getNumericCellValue();
            yield (f == Math.floor(f)) ? String.valueOf((long)f) : String.valueOf(f);
          }
          default -> "";
        };
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      case BLANK:
        return "";
      default:
        return cell.toString();
    }
  }
  /*private BigDecimal toBigDecimal(Cell cell) {
    if (cell == null) return null;
    if (cell.getCellType() == CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue());
    try { return new BigDecimal(cell.getStringCellValue()); } catch (Exception e) { return null; }
  }
  private Integer toInt(Cell cell) {
    if (cell == null) return null;
    if (cell.getCellType() == CellType.NUMERIC) return (int)cell.getNumericCellValue();
    try { return Integer.valueOf(cell.getStringCellValue()); } catch (Exception e) { return null; }
  }*/
  private BigDecimal toBigDecimal(Cell cell) {
    String v = getString(cell);
    try { return v != null && !v.isBlank() ? new BigDecimal(v) : null; }
    catch (NumberFormatException e) { return null; }
  }
  private Integer toInt(Cell cell) {
    String v = getString(cell);
    try { return v != null && !v.isBlank() ? Integer.valueOf(v) : null; }
    catch (NumberFormatException e) { return null; }
  }

  public Page<ProductDto> getProductPage(String search, Pageable pageable) {
    if (search == null || search.isBlank()) {
      return productRepository.findAll(pageable).map(ProductDto::fromEntity);
    }
    return productRepository.findByKorNameContainingOrEngNameContainingOrProductCodeContaining(
      search, search, search, pageable).map(ProductDto::fromEntity);
  }
}







//package com.jason.purchase_agent.service;
//
//import com.jason.purchase_agent.dto.ProductDetailDto;
//import com.jason.purchase_agent.dto.ProductDto;
//import com.jason.purchase_agent.entity.Product;
//import com.jason.purchase_agent.entity.Supplier;
//import com.jason.purchase_agent.repository.jpa.CategoryRepository;
//import com.jason.purchase_agent.repository.jpa.ProductRepository;
//import com.jason.purchase_agent.repository.jpa.SupplierRepository;
//import com.jason.purchase_agent.repository.mybatis.ProductMapper;
//import lombok.RequiredArgsConstructor;
//import org.apache.poi.ss.usermodel.*;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.StringUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * 지원되는 엑셀 헤더명 목록 (대소문자 무관)
// *
// * - code: 상품코드 (필수)
// * - eng_name: 영문 상품명 (필수)
// * - mkt: 공급처 코드 (필수)
// * - purchase_price: 매입가
// * - weight: 무게
// * - link: 소스 링크
// * - stock: 재고 여부 (T/F, TRUE/FALSE, 1/0, Y/N)
// * - kor_name: 한글 상품명
// * - ctgy1: 1차 카테고리
// * - ctgy2: 2차 카테고리
// */
//
//
//
//
///**
// * 상품(Product) 관련 비즈니스 로직을 처리하는 서비스 클래스
// * - 상품 목록 조회, 검색, 페이징 처리
// * - 엑셀 파일을 통한 상품 일괄 업로드
// * - JPA와 MyBatis를 상황에 따라 선택적으로 사용
// */
//@Service // 스프링 IoC 컨테이너에 서비스 빈으로 등록
//@Transactional(readOnly = true) // 클래스 전체에 읽기 전용 트랜잭션 적용 (성능 최적화)
//@RequiredArgsConstructor // Lombok - final 필드에 대한 생성자 자동 생성
//public class ProductService {
//
//  // 의존성 주입을 위한 Repository 및 Mapper 선언 (생성자 주입 방식)
//  private final ProductRepository productRepository; // JPA 기반 상품 저장소
//  private final ProductMapper productMapper; // MyBatis 기반 복잡한 쿼리 처리용
//  private final SupplierRepository supplierRepository; // 공급처 정보 저장소
//  private final CategoryRepository categoryRepository; // 카테고리 정보 저장소
//
//
//  /**
//   * 엑셀 파일을 업로드하여 상품 데이터를 일괄 등록하는 메서드 (헤더명 기반)
//   * 컬럼 순서에 관계없이 헤더명으로 데이터를 매핑
//   *
//   * @param file 업로드된 엑셀 파일 (MultipartFile 형태)
//   * @return 처리 중 발생한 오류 목록 (빈 리스트면 모든 처리 성공)
//   * @throws IOException 파일 읽기 중 발생할 수 있는 예외
//   */
//  @Transactional // 쓰기 작업이므로 읽기전용 트랜잭션을 오버라이드
//  public List<String> uploadProductsFromExcel(MultipartFile file) throws IOException {
//
//    List<String> errors = new ArrayList<>(); // 오류 메시지를 저장할 리스트
//
//    // 파일 유효성 검사
//    if (file.isEmpty()) {
//      errors.add("업로드된 파일이 비어있습니다.");
//      return errors;
//    }
//
//    // try-with-resources 구문으로 Workbook 리소스 자동 해제 보장
//    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
//
//      Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트 가져오기
//
//      // 시트가 비어있는지 확인
//      if (sheet.getLastRowNum() < 1) {
//        errors.add("엑셀 파일에 데이터가 없습니다.");
//        return errors;
//      }
//
//      // 첫 번째 행(헤더)을 읽어서 컬럼 매핑 생성
//      Row headerRow = sheet.getRow(0);
//      if (headerRow == null) {
//        errors.add("헤더 행을 찾을 수 없습니다.");
//        return errors;
//      }
//
//      Map<String, Integer> headerMap = createHeaderMap(headerRow); // 헤더 매핑 생성
//
//      // 필수 헤더가 있는지 검증
//      List<String> requiredHeaders = Arrays.asList("code", "eng_name", "mkt"); // 필수 컬럼명들
//      List<String> missingHeaders = new ArrayList<>();
//
//      for (String requiredHeader : requiredHeaders) {
//        if (!headerMap.containsKey(requiredHeader)) {
//          missingHeaders.add(requiredHeader);
//        }
//      }
//
//      if (!missingHeaders.isEmpty()) {
//        errors.add("필수 헤더가 누락되었습니다: " + String.join(", ", missingHeaders));
//        return errors;
//      }
//
//      // 데이터 행들을 순회하며 처리 (헤더 행 제외)
//      for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
//
//        Row row = sheet.getRow(rowIndex);
//
//        if (row == null || isEmptyRow(row)) {
//          continue; // 빈 행은 건너뛰기
//        }
//
//        try {
//          // 헤더 맵을 사용하여 Product 객체 생성
//          Product product = parseRowToProductByHeader(row, headerMap, rowIndex + 1);
//          productRepository.save(product); // 데이터베이스에 저장
//        } catch (Exception e) {
//          // 개별 행 처리 중 오류 발생 시 오류 목록에 추가
//          errors.add("Row " + (rowIndex + 1) + ": " + e.getMessage());
//        }
//      }
//    }
//
//    return errors; // 처리 결과(오류 목록) 반환
//  }
//
//  /**
//   * 엑셀의 헤더 행을 읽어서 컬럼명과 인덱스를 매핑하는 Map을 생성
//   *
//   * @param headerRow 엑셀의 첫 번째 행(헤더 행)
//   * @return 컬럼명을 키로, 컬럼 인덱스를 값으로 하는 Map
//   */
//  private Map<String, Integer> createHeaderMap(Row headerRow) {
//
//    Map<String, Integer> headerMap = new HashMap<>();
//
//    // 헤더 행의 모든 셀을 순회하면서 컬럼명-인덱스 매핑 생성
//    for (Cell cell : headerRow) {
//      if (cell != null) {
//        String headerName = getCellValue(cell).trim().toLowerCase(); // 대소문자 구분 없이 처리
//        int columnIndex = cell.getColumnIndex(); // 해당 셀의 컬럼 인덱스
//        headerMap.put(headerName, columnIndex); // Map에 저장
//      }
//    }
//
//    return headerMap;
//  }
//
//  /**
//   * 엑셀 셀의 값을 문자열로 변환하는 유틸리티 메서드
//   * 셀의 타입에 따라 적절한 방식으로 문자열 변환 처리
//   *
//   * @param cell 읽어올 엑셀 셀 (null 가능)
//   * @return 셀의 값을 문자열로 변환한 결과 (셀이 null이면 빈 문자열)
//   */
//  private String getCellValue(Cell cell) {
//    if (cell == null) { // 셀이 null인 경우 (빈 셀)
//      return ""; // 빈 문자열 반환
//    }
//
//    // 셀의 데이터 타입에 따라 분기 처리
//    switch (cell.getCellType()) {
//      case STRING: // 문자열 타입
//        return cell.getStringCellValue(); // 문자열 값 그대로 반환
//
//      case NUMERIC: // 숫자 타입
//        if (DateUtil.isCellDateFormatted(cell)) { // 날짜 형식인지 확인
//          return cell.getDateCellValue().toString(); // 날짜면 문자열로 변환
//        } else {
//          // 일반 숫자를 문자열로 변환 (소수점 제거 처리)
//          double numericValue = cell.getNumericCellValue(); // 숫자 값 추출
//          if (numericValue == Math.floor(numericValue)) { // 정수인지 확인
//            return String.valueOf((long) numericValue); // 정수면 long으로 변환 (소수점 제거)
//          } else {
//            return String.valueOf(numericValue); // 실수면 그대로 문자열 변환
//          }
//        }
//
//      case BOOLEAN: // 불린 타입
//        return String.valueOf(cell.getBooleanCellValue()); // "true" 또는 "false" 문자열 반환
//
//      case FORMULA: // 수식 타입
//        return cell.getCellFormula(); // 수식 문자열 반환
//
//      case BLANK: // 빈 셀
//      case _NONE: // 알 수 없는 타입
//      default: // 그 외 모든 경우
//        return ""; // 빈 문자열 반환
//    }
//  }
//
//  /**
//   * 헤더 맵을 사용하여 엑셀의 한 행을 Product 엔티티로 변환하는 메서드
//   * 컬럼 순서에 관계없이 헤더명으로 데이터를 매핑
//   *
//   * @param row 엑셀의 데이터 행
//   * @param headerMap 헤더명-인덱스 매핑 맵
//   * @param rowNumber 현재 행 번호 (오류 메시지용)
//   * @return 변환된 Product 엔티티 객체
//   * @throws IllegalArgumentException 필수 데이터가 없거나 잘못된 경우
//   */
//  private Product parseRowToProductByHeader(Row row, Map<String, Integer> headerMap, int rowNumber) {
//
//    Product product = new Product(); // 새로운 Product 객체 생성
//
//    try {
//      // 1. 상품코드 (필수)
//      String productCode = getValueByColumnName(row, headerMap, "code");
//      if (!StringUtils.hasText(productCode)) {
//        throw new IllegalArgumentException("상품코드가 비어있습니다.");
//      }
//      product.setProductCode(productCode.trim());
//
//      // 2. 영문 상품명 (필수)
//      String engName = getValueByColumnName(row, headerMap, "eng_name");
//      if (!StringUtils.hasText(engName)) {
//        throw new IllegalArgumentException("영문 상품명이 비어있습니다.");
//      }
//      product.setEngName(engName.trim());
//
//      // 3. 공급처 코드 (필수)
//      String supplierCode = getValueByColumnName(row, headerMap, "mkt");
//      if (!StringUtils.hasText(supplierCode)) {
//        throw new IllegalArgumentException("공급처 코드가 비어있습니다.");
//      }
//
//      Supplier supplier = supplierRepository.findBySupplierCode(supplierCode.trim())
//        .orElseThrow(() -> new IllegalArgumentException("공급처를 찾을 수 없습니다: " + supplierCode));
//      product.setSupplier(supplier);
//
//      // 4. 매입가 (선택사항)
//      String purchasePriceStr = getValueByColumnName(row, headerMap, "purchase_price");
//      if (StringUtils.hasText(purchasePriceStr)) {
//        try {
//          BigDecimal purchasePrice = new BigDecimal(purchasePriceStr.trim());
//          product.setPurchasePrice(purchasePrice);
//        } catch (NumberFormatException e) {
//          throw new IllegalArgumentException("매입가 형식이 올바르지 않습니다: " + purchasePriceStr);
//        }
//      }
//
//      // 5. 무게 (선택사항)
//      String weightStr = getValueByColumnName(row, headerMap, "weight");
//      if (StringUtils.hasText(weightStr)) {
//        try {
//          BigDecimal weight = new BigDecimal(weightStr.trim());
//          product.setWeight(weight);
//        } catch (NumberFormatException e) {
//          throw new IllegalArgumentException("무게 형식이 올바르지 않습니다: " + weightStr);
//        }
//      }
//
//      // 6. 소스 링크 (선택사항)
//      String sourceLink = getValueByColumnName(row, headerMap, "link");
//      if (StringUtils.hasText(sourceLink)) {
//        product.setSourceLink(sourceLink.trim());
//      }
//
//      // 7. 재고 여부 (선택사항)
//      String stockStr = getValueByColumnName(row, headerMap, "stock");
//      if (StringUtils.hasText(stockStr)) {
//        boolean isAvailable = "T".equalsIgnoreCase(stockStr.trim()) ||
//          "TRUE".equalsIgnoreCase(stockStr.trim()) ||
//          "1".equals(stockStr.trim()) ||
//          "Y".equalsIgnoreCase(stockStr.trim());
//        product.setIsAvailable(isAvailable);
//      } else {
//        product.setIsAvailable(true); // 기본값: 재고 있음
//      }
//
//      // 8. 한글 상품명 (선택사항)
//      String korName = getValueByColumnName(row, headerMap, "kor_name");
//      if (StringUtils.hasText(korName)) {
//        // 필요시 별도 필드나 로직으로 처리
//        // product.setKorName(korName.trim());
//      }
//
//      // 9. 카테고리 (선택사항)
//      String categoryName1 = getValueByColumnName(row, headerMap, "ctgy1");
//      String categoryName2 = getValueByColumnName(row, headerMap, "ctgy2");
//      if (StringUtils.hasText(categoryName1)) {
//        // 카테고리 로직 처리 (필요시 구현)
//        // Category category = findOrCreateCategory(categoryName1, categoryName2);
//        // product.setCategory(category);
//      }
//
//      return product; // 완성된 Product 객체 반환
//
//    } catch (Exception e) {
//      throw new RuntimeException("Row " + rowNumber + " 파싱 오류: " + e.getMessage(), e);
//    }
//  }
//
//  /**
//   * 헤더 맵을 사용하여 특정 컬럼의 값을 안전하게 가져오는 메서드
//   *
//   * @param row 데이터 행
//   * @param headerMap 헤더명-인덱스 매핑 맵
//   * @param columnName 찾고자 하는 컬럼명
//   * @return 해당 컬럼의 값 (컬럼이 없으면 빈 문자열)
//   */
//  private String getValueByColumnName(Row row, Map<String, Integer> headerMap, String columnName) {
//    Integer columnIndex = headerMap.get(columnName.toLowerCase()); // 컬럼명으로 인덱스 찾기
//    if (columnIndex != null) { // 해당 컬럼이 존재하는 경우
//      Cell cell = row.getCell(columnIndex); // 인덱스로 셀 가져오기
//      return getCellValue(cell); // 셀 값 반환
//    }
//    return ""; // 컬럼이 없으면 빈 문자열 반환
//  }
//
//
//
//
//
//
//
//
//
//
//
//
//  /**
//   * 상품 목록을 페이징 처리하여 조회하는 메서드
//   * 검색 조건이 있으면 MyBatis를 사용하고, 없으면 JPA를 사용하여 성능을 최적화
//   *
//   * @param page 페이지 번호 (0부터 시작)
//   * @param size 페이지당 표시할 상품 수
//   * @param searchKeyword 상품명 또는 상품코드로 검색할 키워드 (선택사항)
//   * @param supplierCode 공급처 코드로 필터링 (선택사항)
//   * @return 페이징 처리된 상품 DTO 목록
//   */
//  public Page<ProductDto> getProducts(int page, int size, String searchKeyword, String supplierCode) {
//    // 페이징 정보 설정 - 생성일자 기준으로 내림차순 정렬
//    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//
//    // 검색 키워드나 공급처 코드가 있는지 확인 (복잡한 조회 조건)
//    if (StringUtils.hasText(searchKeyword) || StringUtils.hasText(supplierCode)) {
//      // 복잡한 조회는 MyBatis를 사용하여 최적화된 SQL 실행
//      int offset = page * size; // OFFSET 계산 (건너뛸 레코드 수)
//
//      // MyBatis Mapper를 통한 상세 조회 실행
//      List<ProductDetailDto> productDetails = productMapper.findProductsWithDetails(
//        offset, size, searchKeyword, supplierCode);
//
//      // 전체 레코드 수 조회 (페이징 정보 계산용)
//      Long total = productMapper.countProductsWithDetails(searchKeyword, supplierCode);
//
//      // ProductDetailDto를 ProductDto로 변환 (스트림 API 사용)
//      List<ProductDto> productDtos = productDetails.stream()
//        .map(this::convertDetailToDto) // 메서드 레퍼런스로 변환 메서드 호출
//        .collect(Collectors.toList()); // List로 수집
//
//      // PageImpl 객체로 페이징 결과 생성하여 반환
//      return new PageImpl<>(productDtos, pageable, total);
//    } else {
//      // 단순 조회는 JPA 사용 - Spring Data JPA의 기본 기능 활용
//      return productRepository.findByIsAvailableTrue(pageable) // 재고 있는 상품만 조회
//        .map(this::convertToDto); // Page의 map 메서드로 DTO 변환
//    }
//  }
//
//
//
//
//  /**
//   * 엑셀의 한 행(Row) 데이터를 Product 엔티티 객체로 변환하는 메서드
//   * 각 셀의 데이터를 읽어서 Product의 필드에 매핑
//   *
//   * @param row 엑셀의 한 행 데이터
//   * @return 변환된 Product 엔티티 객체
//   * @throws IllegalArgumentException 필수 데이터가 없거나 잘못된 경우
//   */
//  private Product parseRowToProduct(Row row) {
//    Product product = new Product(); // 새로운 Product 객체 생성
//
//    // 엑셀 컬럼에 따른 파싱 로직 (컬럼 순서는 엑셀 파일 구조에 따름)
//    // 0번 컬럼: 상품코드
//    product.setProductCode(getCellValue(row.getCell(0)));
//
//    // 1번 컬럼: 영문 상품명
//    product.setEngName(getCellValue(row.getCell(1)));
//
//    // 2번 컬럼: 공급처 코드 - 데이터베이스에서 실제 Supplier 엔티티 조회 필요
//    String supplierCode = getCellValue(row.getCell(2));
//    Supplier supplier = supplierRepository.findBySupplierCode(supplierCode) // Optional<Supplier> 반환
//      .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierCode)); // 없으면 예외 발생
//    product.setSupplier(supplier); // 조회된 Supplier 객체 설정
//
//    // 나머지 필드들... (주석에서는 생략, 실제로는 추가 필드들을 여기서 처리)
//    // 예: 매입가, 무게, 재고여부, 카테고리 등
//
//    return product; // 완성된 Product 객체 반환
//  }
//
//  /**
//   * Product 엔티티를 간단한 ProductDto로 변환하는 메서드
//   * JPA 조회 결과를 화면 표시용 DTO로 변환할 때 사용
//   *
//   * @param product 변환할 Product 엔티티
//   * @return 화면 표시용 ProductDto 객체
//   */
//  private ProductDto convertToDto(Product product) {
//    return ProductDto.builder() // Builder 패턴으로 객체 생성
//      .productCode(product.getProductCode()) // 상품코드 복사
//      .engName(product.getEngName()) // 영문명 복사
//      .supplierCode(product.getSupplier().getSupplierCode()) // 연관된 Supplier에서 코드 추출
//      .purchasePrice(product.getPurchasePrice()) // 매입가 복사
//      .weight(product.getWeight()) // 무게 복사
//      .isAvailable(product.getIsAvailable()) // 재고 여부 복사
//      .build(); // 최종 객체 생성
//  }
//
//  /**
//   * ProductDetailDto를 ProductDto로 변환하는 메서드
//   * MyBatis 조회 결과(상세 정보 포함)를 화면 표시용 DTO로 변환할 때 사용
//   *
//   * @param detail MyBatis로 조회한 상세 정보가 담긴 DTO
//   * @return 화면 표시용 ProductDto 객체
//   */
//  private ProductDto convertDetailToDto(ProductDetailDto detail) {
//    return ProductDto.builder() // Builder 패턴으로 객체 생성
//      .productCode(detail.getProductCode()) // 상품코드
//      .engName(detail.getEngName()) // 영문명
//      .supplierCode(detail.getSupplierCode()) // 공급처 코드
//      .purchasePrice(detail.getPurchasePrice()) // 매입가
//      .weight(detail.getWeight()) // 무게
//      .isAvailable(detail.getIsAvailable()) // 재고 여부
//      .categoryName(detail.getCategoryName()) // 카테고리명 (MyBatis 조회에서만 포함)
//      .createdAt(detail.getCreatedAt()) // 생성일시 (MyBatis 조회에서만 포함)
//      .build(); // 최종 객체 생성
//  }
//
//
//
//
//
//  /**
//   * 엑셀 행이 비어있는지 확인하는 유틸리티 메서드
//   *
//   * @param row 확인할 행
//   * @return 빈 행이면 true, 데이터가 있으면 false
//   */
//  private boolean isEmptyRow(Row row) {
//    if (row == null) {
//      return true;
//    }
//
//    // 행의 모든 셀을 확인
//    for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
//      Cell cell = row.getCell(cellIndex);
//      if (cell != null && cell.getCellType() != CellType.BLANK) {
//        String cellValue = getCellValue(cell);
//        if (StringUtils.hasText(cellValue)) {
//          return false; // 하나라도 데이터가 있으면 빈 행이 아님
//        }
//      }
//    }
//    return true; // 모든 셀이 비어있으면 빈 행
//  }
//
//
//
//
//
//
//}
