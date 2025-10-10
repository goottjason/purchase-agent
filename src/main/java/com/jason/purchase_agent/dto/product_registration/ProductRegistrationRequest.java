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
    private List<String> imageLinks; // 이미지링크 (아이허브 크롤링하면서 세팅)
    private List<String> imageFiles; // 이미지 로컬파일 경로 (임시저장된 파일경로, S3 업로드 후에는 null로 세팅)
    private List<String> uploadedImageLinks; // 업로드된 이미지링크 (쿠팡 등록하면서 세팅)
    private List<String> smartstoreImageLinks;

    private String productType; // 고시정보구분 ("FOOD" 또는 "HEALTH")
    private List<String> categoryNames;
    private Integer smartstoreCategoryId;
    private Integer elevenstCategoryId;

}
