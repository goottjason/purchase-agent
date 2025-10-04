package com.jason.purchase_agent.service.autoupdate;

import com.jason.purchase_agent.dto.autoupdate.AutoUpdateMessage;
import com.jason.purchase_agent.dto.products.ProductDto;
import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.entity.ProductChannelMapping;
import com.jason.purchase_agent.repository.jpa.ProcessStatusRepository;
import com.jason.purchase_agent.repository.jpa.ProductChannelMappingRepository;
import com.jason.purchase_agent.repository.jpa.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.jason.purchase_agent.service.products.ProductService.convertToListDto;

@Service
@RequiredArgsConstructor
public class AutoUpdateService {

    private final ProductRepository productRepo;
    private final ProductChannelMappingRepository channelMappingRepository;
    private final AutoUpdateQueueService autoUpdateQueueService;
    private final ProcessStatusRepository psr;

    /**
     * 자동 가격/재고 업데이트 배치 시작
     * @param supplierCode 공급업체 코드(PK, String)
     * @param marginRate 마진율
     * @param requestedBy 요청자정보(이메일/ID 등)
     */
    public void startAutoUpdate(
            String supplierCode, Integer marginRate, Integer couponRate, Integer minMarginPrice, String requestedBy
    ) {


        // [1] 배치ID 생성 (UUID)
        String batchId = UUID.randomUUID().toString();

        // [2] 공급업체의 모든 상품 조회
        List<Product> products = productRepo.findBySupplier_SupplierCode(supplierCode);

        // 상품 코드 리스트 추출
        List<String> productCodes = products.stream()
                .map(Product::getCode)
                .collect(Collectors.toList());

        // 채널매핑 정보 조회
        List<ProductChannelMapping> channelMappings = channelMappingRepository.findByProductCodeIn(productCodes);
        Map<String, ProductChannelMapping> mappingMap = channelMappings.stream()
                .collect(Collectors.toMap(ProductChannelMapping::getProductCode, mapping -> mapping));

        // DTO 변환
        List<ProductDto> productDtos = products.stream()
                .map(product -> convertToListDto(
                        product, mappingMap.get(product.getCode())))
                .collect(Collectors.toList());

        // [3] 배치 전체 시작 로그 남기기
        String msgStr = String.format("[%s] %s 공급업체, 마진율=%d, 쿠폰할인율=%d, 최소마진=%d, 요청자=%s, 생성상품=%d개",
                batchId, supplierCode, marginRate, couponRate, minMarginPrice, requestedBy, productDtos.size());
        psr.insert(batchId, null, null,
                "PROCESSING", "배치 시작", msgStr);


        // [3] 각 상품별 메시지 생성 & MQ 발행
        for (ProductDto prod : productDtos) {
            AutoUpdateMessage message = AutoUpdateMessage.builder()
                    .batchId(batchId)                         // 동일 배치 식별자
                    .supplierCode(supplierCode)               // 공급업체 PK
                    .productDto(prod)                     // 상품객체
                    .marginRate(marginRate)
                    .couponRate(couponRate)
                    .minMarginPrice(minMarginPrice)
                    .requestedBy(requestedBy)
                    .requestedAt(LocalDateTime.now())
                    .build();

            // MQ에 개별 메시지 발행 (아래 서비스 또는 MQ템플릿)
            autoUpdateQueueService.publishAutoUpdate(message);
        }
    }
}
