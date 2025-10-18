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

    /**
     * ■■□□ : 2단계(상품등록 준비하기)에서 확정
     * ■■■□ : 3단계(최종 상품등록)에서 확정
     */

    private ProductDto productDto;

    // ■■□□ ["https://cloudinary.images-iherb.com/image/44.jpg", ...]
    private List<String> imageLinks;
    // ■■□□ ['YumEarth (야미얼스)', '브랜드 A-Z', '하드 캔디 & 막대사탕', '식료품', '카테고리']
    private List<String> categoryNames;
    // ■■■□
    private Integer smartstoreCategoryId;
    // ■■■□
    private Integer elevenstCategoryId;
    // ■■■■ (registerProducts 메서드에서 로컬에 이미지 다운로드하면서 세팅)
    private List<String> imageFiles;
    // ■■■■ (registerProducts 메서드 내의 crawlEsmAndUploadImages 메서드에서 이미지 업로드하면서 세팅)
    private List<String> uploadedImageLinks;
    // ■■■■ (handleRegisterProduct 메서드
    private List<String> smartstoreImageLinks;
}
