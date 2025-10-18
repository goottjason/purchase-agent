package com.jason.purchase_agent.service.suppliers;

import com.jason.purchase_agent.dto.suppliers.SupplierDto;
import com.jason.purchase_agent.entity.Currency;
import com.jason.purchase_agent.entity.Supplier;
import com.jason.purchase_agent.repository.CurrencyRepository;
import com.jason.purchase_agent.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final CurrencyRepository currencyRepository;

    public List<SupplierDto> getAllSuppliers() {
        return supplierRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public SupplierDto createSupplier(SupplierDto dto) {
        Currency currency = currencyRepository.findById(dto.getCurrencyCode())
                .orElseThrow(() -> new IllegalArgumentException("통화가 존재하지 않습니다."));
        Supplier supplier = new Supplier(
                dto.getSupplierCode(),
                dto.getSupplierName(),
                currency
        );
        return toDto(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierDto updateSupplier(String code, SupplierDto dto) {
        Supplier supplier = supplierRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("공급사가 존재하지 않습니다."));
        supplier.setSupplierName(dto.getSupplierName());
        if (dto.getCurrencyCode() != null) {
            Currency currency = currencyRepository.findById(dto.getCurrencyCode())
                    .orElseThrow(() -> new IllegalArgumentException("통화가 존재하지 않습니다."));
            supplier.setCurrency(currency);
        }
        return toDto(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(String code) {
        supplierRepository.deleteById(code);
    }

    private SupplierDto toDto(Supplier s) {
        return SupplierDto.builder()
                .supplierCode(s.getSupplierCode())
                .supplierName(s.getSupplierName())
                .currencyCode(s.getCurrency() != null ? s.getCurrency().getCurrencyCode() : null)
                .build();
    }

}