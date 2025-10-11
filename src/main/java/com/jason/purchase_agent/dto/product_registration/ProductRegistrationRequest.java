package com.jason.purchase_agent.dto.product_registration;

import com.jason.purchase_agent.dto.products.ProductDto;
import com.jason.purchase_agent.external.ChannelResultDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRegistrationRequest {

    private ProductDto productDto;

    // ■■■□ ["https://cloudinary.images-iherb.com/image/44.jpg", ...]
    private List<String> imageLinks;
    // ■■■□ ['YumEarth (야미얼스)', '브랜드 A-Z', '하드 캔디 & 막대사탕', '식료품', '카테고리']
    private List<String> categoryNames;
    // ●●●○
    private Integer smartstoreCategoryId;
    // ●●●○
    private Integer elevenstCategoryId;
    // ■■■■-■□□
    private List<String> imageFiles;
    // ■■■■-■■□
    private List<String> uploadedImageLinks;
    //
    private List<String> smartstoreImageLinks;
}
