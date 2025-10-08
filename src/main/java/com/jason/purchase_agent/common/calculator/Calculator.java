package com.jason.purchase_agent.common.calculator;

import com.jason.purchase_agent.external.iherb.dto.IherbProductDto;
import com.jason.purchase_agent.dto.products.ProductDto;

public class Calculator {
    public static Integer calculateSalePrice(
            Integer marginRate,
            Integer couponRate,
            Integer minMarginPrice,
            ProductDto product,
            IherbProductDto dto) {

        // 현재 상품 자체에서 할인중인 할인율
        Double salesDiscountPercentage = dto.getSalesDiscountPercentage();

        Double listPriceAmount = dto.getListPriceAmount(); // 상품 원가
        Double discountPriceAmount = dto.getDiscountPriceAmount(); // 할인가
        Integer discountType = dto.getDiscountType();

        Double buyPrice = 0.0;

        if (discountType == 2) {
            // 특가상품: 구입처 세일가격만 적용
            buyPrice = discountPriceAmount;
        } else {
            // 일반상품: 쿠폰할인율과 기본할인율 중 큰 할인율 적용
            Double appliedDiscountRate = Math.max(couponRate, salesDiscountPercentage);
            buyPrice = listPriceAmount * ((100 - appliedDiscountRate) / 100.0);
        }

        Integer deliveryFee = 6000;
        Double totalBuyPrice = buyPrice * product.getPackQty();
        // 4만원 미만은 배송비 추가
        if (totalBuyPrice < 40000) { totalBuyPrice += deliveryFee; }

        //

        // 기본 계산
        Double basePrice = totalBuyPrice / ((100 - marginRate) / 100.0);

        // 최소 마진 보장 계산 (각 채널 수수료와 제세금을 15%로 가정했을 때, 최소 minMarginPrice는 남아야 하는 가격)
        Double minPrice = (totalBuyPrice + minMarginPrice) / ((100 - 12) * 100.0);

        // 최종 가격 결정
        Integer salePrice = (int) Math.max(basePrice, minMarginPrice);

        // xx,900원 변환
        salePrice = (int) ((Math.ceil(salePrice / 1000.0)) * 1000 - 100);

        return salePrice;
    }
}
