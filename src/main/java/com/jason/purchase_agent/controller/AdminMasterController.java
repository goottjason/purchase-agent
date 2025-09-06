package com.jason.purchase_agent.controller;

import com.jason.purchase_agent.dto.*;
import com.jason.purchase_agent.entity.*;
import com.jason.purchase_agent.service.AdminMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
@Controller
@RequestMapping("/admin/master")
@RequiredArgsConstructor
public class AdminMasterController {

  private final AdminMasterService adminMasterService;

  @GetMapping
  public String masterForm(Model model) {
    model.addAttribute("supplierDto", new SupplierDto());
    model.addAttribute("currencyDto", new CurrencyDto());
    model.addAttribute("categoryDto", new CategoryDto());
    model.addAttribute("saleChannelDto", new SaleChannelDto());

    model.addAttribute("suppliers", adminMasterService.getSuppliers());
    model.addAttribute("currencies", adminMasterService.getCurrencies());
    model.addAttribute("categories", adminMasterService.getCategories());
//    model.addAttribute("parentCategories", adminMasterService.getLevel1Categories());
    model.addAttribute("saleChannels", adminMasterService.getSaleChannels());

    model.addAttribute("editType", "new"); // 등록 상태 표시용
    return "admin/master/form";
  }

  // ========== 등록 ==========
  @PostMapping("/supplier")
  public String addSupplier(@Valid @ModelAttribute SupplierDto supplierDto, BindingResult result, Model model) {
    if (result.hasErrors()) return masterForm(model);
    adminMasterService.addSupplier(supplierDto);
    return "redirect:/admin/master";
  }

  @PostMapping("/currency")
  public String addCurrency(@Valid @ModelAttribute CurrencyDto currencyDto, BindingResult result, Model model) {
    if (result.hasErrors()) return masterForm(model);
    adminMasterService.addCurrency(currencyDto);
    return "redirect:/admin/master";
  }

  @PostMapping("/category")
  public String addCategory(@Valid @ModelAttribute CategoryDto categoryDto, BindingResult result, Model model) {
    if (result.hasErrors()) return masterForm(model);
    adminMasterService.addCategory(categoryDto);
    return "redirect:/admin/master";
  }

  @PostMapping("/sale-channel")
  public String addSaleChannel(@Valid @ModelAttribute SaleChannelDto saleChannelDto, BindingResult result, Model model) {
    if (result.hasErrors()) return masterForm(model);
    adminMasterService.addSaleChannel(saleChannelDto);
    return "redirect:/admin/master";
  }

  // ========== 수정 폼 ==========
  @GetMapping("/supplier/edit/{supplierCode}")
  public String editSupplierForm(@PathVariable String supplierCode, Model model) {
    SupplierDto dto = adminMasterService.getSupplierDto(supplierCode);
    model.addAttribute("supplierDto", dto);
    prepareFormModel(model, "supplier");
    return "admin/master/form";
  }

  @GetMapping("/currency/edit/{currencyCode}")
  public String editCurrencyForm(@PathVariable String currencyCode, Model model) {
    CurrencyDto dto = adminMasterService.getCurrencyDto(currencyCode);
    model.addAttribute("currencyDto", dto);
    prepareFormModel(model, "currency");
    return "admin/master/form";
  }

  @GetMapping("/category/edit/{categoryCode}")
  public String editCategoryForm(@PathVariable String categoryCode, Model model) {
    CategoryDto dto = adminMasterService.getCategoryDto(categoryCode);
    model.addAttribute("categoryDto", dto);
    prepareFormModel(model, "category");
    return "admin/master/form";
  }

  @GetMapping("/sale-channel/edit/{channelCode}")
  public String editSaleChannelForm(@PathVariable String channelCode, Model model) {
    SaleChannelDto dto = adminMasterService.getSaleChannelDto(channelCode);
    model.addAttribute("saleChannelDto", dto);
    prepareFormModel(model, "saleChannel");
    return "admin/master/form";
  }

  // ========== 수정 처리 ==========
  @PostMapping("/supplier/update")
  public String updateSupplier(@Valid @ModelAttribute SupplierDto supplierDto, BindingResult result, Model model) {
    if (result.hasErrors()) {
      prepareFormModel(model, "supplier");
      return "admin/master/form";
    }
    adminMasterService.updateSupplier(supplierDto);
    return "redirect:/admin/master";
  }

  @PostMapping("/currency/update")
  public String updateCurrency(@Valid @ModelAttribute CurrencyDto currencyDto, BindingResult result, Model model) {
    if (result.hasErrors()) {
      prepareFormModel(model, "currency");
      return "admin/master/form";
    }
    adminMasterService.updateCurrency(currencyDto);
    return "redirect:/admin/master";
  }

  @PostMapping("/category/update")
  public String updateCategory(@Valid @ModelAttribute CategoryDto categoryDto, BindingResult result, Model model) {
    if (result.hasErrors()) {
      prepareFormModel(model, "category");
      return "admin/master/form";
    }
    adminMasterService.updateCategory(categoryDto);
    return "redirect:/admin/master";
  }

  @PostMapping("/sale-channel/update")
  public String updateSaleChannel(@Valid @ModelAttribute SaleChannelDto saleChannelDto, BindingResult result, Model model) {
    if (result.hasErrors()) {
      prepareFormModel(model, "saleChannel");
      return "admin/master/form";
    }
    adminMasterService.updateSaleChannel(saleChannelDto);
    return "redirect:/admin/master";
  }

  // ========== 삭제 ==========
  @PostMapping("/supplier/delete/{supplierCode}")
  public String deleteSupplier(@PathVariable String supplierCode) {
    adminMasterService.deleteSupplier(supplierCode);
    return "redirect:/admin/master";
  }

  @PostMapping("/currency/delete/{currencyCode}")
  public String deleteCurrency(@PathVariable String currencyCode) {
    adminMasterService.deleteCurrency(currencyCode);
    return "redirect:/admin/master";
  }

  @PostMapping("/category/delete/{categoryCode}")
  public String deleteCategory(@PathVariable String categoryCode) {
    adminMasterService.deleteCategory(categoryCode);
    return "redirect:/admin/master";
  }

  @PostMapping("/sale-channel/delete/{channelCode}")
  public String deleteSaleChannel(@PathVariable String channelCode) {
    adminMasterService.deleteSaleChannel(channelCode);
    return "redirect:/admin/master";
  }

  // ------- 공통으로 폼에 필요한 모델 세팅 메서드 -------
  private void prepareFormModel(Model model, String editType) {
    model.addAttribute("editType", editType);
    model.addAttribute("suppliers", adminMasterService.getSuppliers());
    model.addAttribute("currencies", adminMasterService.getCurrencies());
    model.addAttribute("categories", adminMasterService.getCategories());
//    model.addAttribute("parentCategories", adminMasterService.getLevel1Categories());
    model.addAttribute("saleChannels", adminMasterService.getSaleChannels());
  }
}
