package com.jason.purchase_agent.service;

import com.jason.purchase_agent.dto.*;
import com.jason.purchase_agent.entity.*;
import com.jason.purchase_agent.repository.CategoryRepository;
import com.jason.purchase_agent.repository.jpa.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMasterService {

  private final SupplierRepository supplierRepository;
  private final CurrencyRepository currencyRepository;
  private final CategoryRepository categoryRepository;
  private final SaleChannelRepository saleChannelRepository;

  // ===== 목록 조회 =====
  public List<Supplier> getSuppliers() { return supplierRepository.findAll(); }
  public List<Currency> getCurrencies() { return currencyRepository.findAll(); }
  public List<Category> getCategories() { return categoryRepository.findAll(); }
//  public List<Category> getLevel1Categories() { return categoryRepository.findByParentCategoryIsNull(); }
  public List<SaleChannel> getSaleChannels() { return saleChannelRepository.findAll(); }

  // ===== 등록 =====
  @Transactional
  public void addSupplier(SupplierDto dto) {
    Supplier s = new Supplier();
    s.setSupplierCode(dto.getSupplierCode());
    s.setSupplierName(dto.getSupplierName());
    s.setCurrency(currencyRepository.findById(dto.getCurrencyCode()).orElse(null));
    supplierRepository.save(s);
  }

  @Transactional
  public void addCurrency(CurrencyDto dto) {
    Currency c = new Currency();
    c.setCurrencyCode(dto.getCurrencyCode());
    c.setExchangeRate(dto.getExchangeRate());
    currencyRepository.save(c);
  }

  @Transactional
  public void addCategory(CategoryDto dto) {
    Category c = new Category();
//    c.setCategoryCode(dto.getCategoryCode());
//    c.setCategoryName(dto.getCategoryName());
    if (dto.getParentCategoryCode() != null && !dto.getParentCategoryCode().isBlank()) {
      // 중요: DTO는 코드, Entity는 Category
//      c.setParentCategory(categoryRepository.findById(dto.getParentCategoryCode()).orElse(null));
    }
    categoryRepository.save(c);
  }

  @Transactional
  public void addSaleChannel(SaleChannelDto dto) {
    SaleChannel sc = new SaleChannel();
    sc.setChannelCode(dto.getChannelCode());
    sc.setChannelName(dto.getChannelName());
    sc.setFeeRate(dto.getFeeRate());
    saleChannelRepository.save(sc);
  }

  // ===== 단건 조회 (수정폼용) =====
  public SupplierDto getSupplierDto(String supplierCode) {
    return supplierRepository.findById(supplierCode).map(s -> {
      SupplierDto dto = new SupplierDto();
      dto.setSupplierCode(s.getSupplierCode());
      dto.setSupplierName(s.getSupplierName());
      dto.setCurrencyCode(s.getCurrency() != null ? s.getCurrency().getCurrencyCode() : null);
      return dto;
    }).orElseThrow(() -> new RuntimeException("공급처를 찾을 수 없습니다: " + supplierCode));
  }

  public CurrencyDto getCurrencyDto(String currencyCode) {
    return currencyRepository.findById(currencyCode).map(c -> {
      CurrencyDto dto = new CurrencyDto();
      dto.setCurrencyCode(c.getCurrencyCode());
      dto.setExchangeRate(c.getExchangeRate());
      return dto;
    }).orElseThrow(() -> new RuntimeException("통화를 찾을 수 없습니다: " + currencyCode));
  }

  public CategoryDto getCategoryDto(String categoryCode) {
    return categoryRepository.findById(categoryCode).map(c -> {
      CategoryDto dto = new CategoryDto();
//      dto.setCategoryCode(c.getCategoryCode());
//      dto.setCategoryName(c.getCategoryName());
//      dto.setParentCategoryCode(
//        c.getParentCategory() != null ? c.getParentCategory().getCategoryCode() : null
//      );
      return dto;
    }).orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다: " + categoryCode));
  }

  public SaleChannelDto getSaleChannelDto(String channelCode) {
    return saleChannelRepository.findById(channelCode).map(s -> {
      SaleChannelDto dto = new SaleChannelDto();
      dto.setChannelCode(s.getChannelCode());
      dto.setChannelName(s.getChannelName());
      dto.setFeeRate(s.getFeeRate());
      return dto;
    }).orElseThrow(() -> new RuntimeException("판매채널을 찾을 수 없습니다: " + channelCode));
  }

  // ===== 수정 =====
  @Transactional
  public void updateSupplier(SupplierDto dto) {
    Supplier s = supplierRepository.findById(dto.getSupplierCode()).orElseThrow();
    s.setSupplierName(dto.getSupplierName());
    s.setCurrency(currencyRepository.findById(dto.getCurrencyCode()).orElse(null));
    supplierRepository.save(s);
  }

  @Transactional
  public void updateCurrency(CurrencyDto dto) {
    Currency c = currencyRepository.findById(dto.getCurrencyCode()).orElseThrow();
    c.setExchangeRate(dto.getExchangeRate());
    currencyRepository.save(c);
  }

  @Transactional
  public void updateCategory(CategoryDto dto) {
    Category c = categoryRepository.findById(dto.getCategoryCode()).orElseThrow();
//    c.setCategoryName(dto.getCategoryName());
//    if (dto.getParentCategoryCode() != null && !dto.getParentCategoryCode().isBlank()) {
//      c.setParentCategory(categoryRepository.findById(dto.getParentCategoryCode()).orElse(null));
//    } else {
//      c.setParentCategory(null);
//    }
//    categoryRepository.save(c);
  }

  @Transactional
  public void updateSaleChannel(SaleChannelDto dto) {
    SaleChannel s = saleChannelRepository.findById(dto.getChannelCode()).orElseThrow();
    s.setChannelName(dto.getChannelName());
    s.setFeeRate(dto.getFeeRate());
    saleChannelRepository.save(s);
  }

  // ===== 삭제 =====
  @Transactional
  public void deleteSupplier(String supplierCode) {
    supplierRepository.deleteById(supplierCode);
  }

  @Transactional
  public void deleteCurrency(String currencyCode) {
    currencyRepository.deleteById(currencyCode);
  }

  @Transactional
  public void deleteCategory(String categoryCode) {
    categoryRepository.deleteById(categoryCode);
  }

  @Transactional
  public void deleteSaleChannel(String channelCode) {
    saleChannelRepository.deleteById(channelCode);
  }
}
