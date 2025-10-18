package com.jason.purchase_agent.util;

import com.jason.purchase_agent.external.iherb.dto.IherbProductDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Calculator {
    public static Integer calculateSalePrice(
            Integer marginRate,
            Integer couponRate,
            Integer minMarginPrice,
            Integer packQty,
            IherbProductDto dto) {

        // 현재 상품 자체에서 할인중인 할인율
        Double salesDiscountPercentage = dto.getSalesDiscountPercentage();

        Double listPriceAmount = dto.getListPriceAmount(); // 상품 원가
        Double discountPriceAmount = dto.getDiscountPriceAmount(); // 할인가
        Integer discountType = dto.getDiscountType();
        log.info("salesDiscountPercentage={}, listPriceAmount={}, discountPriceAmount={}, discountType={}",
                salesDiscountPercentage, listPriceAmount, discountPriceAmount, discountType);

        Double buyPrice = 0.0;

        if (discountType == 2) {
            // 특가상품: 구입처 세일가격만 적용
            buyPrice = discountPriceAmount;
        } else {
            // 일반상품: 쿠폰할인율과 기본할인율 중 큰 할인율 적용
            Double appliedDiscountRate = Math.max(couponRate, salesDiscountPercentage); // 20%, 0%
            buyPrice = listPriceAmount * ((100 - appliedDiscountRate) / 100.0); // 14824 * (1 - 20%) = 11859.2
        }

        Integer deliveryFee = 6000;
        Double totalBuyPrice = buyPrice * packQty; // 11859.2 * 2 = 23,718.4
        // 4만원 미만은 배송비 추가
        if (totalBuyPrice < 40000) { totalBuyPrice += deliveryFee; } // 23,718.4 + 6000 = 29,718.4

        // xx,x00원 변환
        Integer salePrice = (int) ((Math.ceil(totalBuyPrice / ((100 - marginRate - 18.5) / 100.0) / 100.0)) * 100);
        // 37200

        salePrice = (salePrice - totalBuyPrice) < minMarginPrice
                ? (int) (salePrice + (minMarginPrice - (salePrice - totalBuyPrice)))
                : salePrice;
                // (totalBuyPrice + minMarginPrice) / ((100 - 13) * 100.0);

        return salePrice;
    }
}
