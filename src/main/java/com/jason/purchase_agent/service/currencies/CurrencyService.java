package com.jason.purchase_agent.service.currencies;

import com.jason.purchase_agent.dto.currencies.CurrencyDto;
import com.jason.purchase_agent.entity.Currency;
import com.jason.purchase_agent.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public List<CurrencyDto> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CurrencyDto createCurrency(CurrencyDto dto) {
        Currency currency = new Currency();
        currency.setCurrencyCode(dto.getCurrencyCode().toUpperCase());
        currency.setExchangeRate(dto.getExchangeRate());
        return toDto(currencyRepository.save(currency));
    }

    @Transactional
    public CurrencyDto updateCurrency(String code, CurrencyDto dto) {
        Currency currency = currencyRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("통화 정보가 없습니다."));
        currency.setExchangeRate(dto.getExchangeRate());
        return toDto(currencyRepository.save(currency));
    }

    @Transactional
    public void deleteCurrency(String code) {
        currencyRepository.deleteById(code);
    }

    public Optional<CurrencyDto> getCurrency(String code) {
        return currencyRepository.findById(code).map(this::toDto);
    }

    private CurrencyDto toDto(Currency c) {
        return new CurrencyDto(c.getCurrencyCode(), c.getExchangeRate());
    }

}