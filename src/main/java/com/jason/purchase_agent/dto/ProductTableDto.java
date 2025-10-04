package com.jason.purchase_agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTableDto {
    private String code;
    private String link;
    private String unit;
    private String html;
    private Double discountPrice;
    private int finalPrice;
    private int packageQuantity;
    private String korNameMain;
    private String korNameGoin;
    private String engName;
    private String brand;
    private int stock;
}