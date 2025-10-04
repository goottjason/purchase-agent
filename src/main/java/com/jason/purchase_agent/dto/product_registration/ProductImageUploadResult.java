package com.jason.purchase_agent.dto.product_registration;

import java.util.List;

// (Lombok 추천)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUploadResult {
    private String code;
    private List<String> uploadedImageLinks;
}