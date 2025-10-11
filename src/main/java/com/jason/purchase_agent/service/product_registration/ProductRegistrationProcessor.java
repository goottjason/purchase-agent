package com.jason.purchase_agent.service.product_registration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.product_registration.ProductImageUploadResult;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import com.jason.purchase_agent.entity.Product;
import com.jason.purchase_agent.entity.ProductChannelMapping;
import com.jason.purchase_agent.external.ChannelResultDto;
import com.jason.purchase_agent.external.coupang.CoupangApiService;
import com.jason.purchase_agent.external.elevenst.ElevenstApiService;
import com.jason.purchase_agent.external.smartstore.SmartstoreApiService;
import com.jason.purchase_agent.repository.jpa.ProcessStatusRepository;
import com.jason.purchase_agent.repository.jpa.ProductChannelMappingRepository;
import com.jason.purchase_agent.repository.jpa.ProductRepository;
import com.jason.purchase_agent.util.downloader.ImageDownloader;
import com.jason.purchase_agent.util.uploader.UploadImagesApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jason.purchase_agent.util.JsonUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRegistrationProcessor {
    private final ProductRepository productRepository;
    private final ProductChannelMappingRepository mappingRepository;
    private final ProcessStatusRepository psr;
    private final UploadImagesApi uploadImagesApi;
    private final CoupangApiService coupangApiService;
    private final SmartstoreApiService smartstoreApiService;
    private final ElevenstApiService elevenstApiService;
    private final ObjectMapper objectMapper;

    /*// 1단계 상품DB 저장
    public boolean saveProductDB(
            String batchId, ProductRegistrationRequest productDto) {
        try {
            Product productEntity = new Product();
            BeanUtils.copyProperties(productDto, productEntity);
            // 수동으로 ImageLinks 리스트를 JSON 문자열로 변환하여 DB에 저장 (추후 값을 활용할 수 있도록 함)
            productEntity.setImageLinks(listToJsonString(objectMapper, productDto.getImageLinks()));
            // Product DB에 저장
            productRepository.save(productEntity);
            mappingRepository.save(ProductChannelMapping.builder().productCode(productDto.getCode()).build());

            psr.updateProductStatus(batchId, productDto.getCode(),
                    "SAVE_PRODUCT", "SUCCESS", "상품 DB 저장 완료", null);
            return true;
        } catch (Exception e) {
            psr.updateProductStatus(batchId, productDto.getCode(),
                    "SAVE_PRODUCT", "FAIL", "상품 DB 저장 실패: " + e.getMessage(), null);
            return false;
        }
    }

    // 2단계 이미지 로컬 다운로드
    public boolean downloadImages(
            String batchId, ProductRegistrationRequest productDto) {
        try {
            // 이미지 다운로드
            List<String> localPaths =
                    ImageDownloader.downloadImagesToLocal(productDto.getCode(), productDto.getImageLinks());
            // 다운로드한 로컬 경로 DTO에 세팅
            productDto.setImageFiles(localPaths);
            String details = safeJsonString(objectMapper, productDto);
            // 상태 업데이트
            psr.updateProductStatus(batchId, productDto.getCode(),
                    "DOWNLOAD_IMAGE", "SUCCESS", "이미지 다운로드 성공", details);
            return true;
        } catch (Exception e) {
            psr.updateProductStatus(batchId, productDto.getCode(),
                    "DOWNLOAD_IMAGE", "FAIL", "이미지 다운로드 실패: " + e.getMessage(), null);
            return false;
        }
    }

    // 3단계 이미지 업로드
    public boolean uploadImages(
            String batchId, List<ProductRegistrationRequest> dtos
    ) {
        // 업로드할 이미지가 없으면 바로 실패 처리
        if (dtos.isEmpty()) return false;

        // 업로드 요청용 맵 리스트 생성
        List<Map<String, Object>> imageUploadRequests = dtos.stream()
                .map(dto ->
                        Map.of("code", dto.getCode(), "imageFiles", dto.getImageFiles()))
                .collect(Collectors.toList());

        // 이미지 업로드 시도
        try {
            // 이미지 업로드 API 호출
            List<ProductImageUploadResult> results = uploadImagesApi.batchUploadImages(imageUploadRequests);

            // 결과 매핑
            Map<String, List<String>> codeToLinks = new HashMap<>();
            for (ProductImageUploadResult result : results) {
                codeToLinks.put(result.getCode(), result.getUploadedImageLinks());
            }
            // 각 DTO에 업로드된 링크 세팅 및 상태 업데이트
            boolean allSuccess = true;
            for (ProductRegistrationRequest dto : dtos) {
                // 업로드된 링크가 있으면 세팅, 없으면 실패 처리
                List<String> links = codeToLinks.get(dto.getCode());
                String detailsJson = null;
                if (links != null && !links.isEmpty()) {
                    // 업로드된 링크 세팅
                    dto.setUploadedImageLinks(links);
                    String details = safeJsonString(objectMapper, dto);
                    // 상태 업데이트
                    psr.updateProductStatus(batchId, dto.getCode(),
                            "UPLOAD_IMAGE", "SUCCESS", "ESM 서버 이미지 업로드 성공", details);
                } else {
                    psr.updateProductStatus(batchId, dto.getCode(),
                            "UPLOAD_IMAGE", "FAIL", "ESM 서버 이미지 업로드 실패: 링크 없음", null);
                    allSuccess = false;
                }
            }
            return allSuccess;
        } catch (Exception e) {
            for (ProductRegistrationRequest dto : dtos) {
                psr.updateProductStatus(batchId, dto.getCode(),
                        "UPLOAD_IMAGE", "FAIL", "ESM 서버 이미지 업로드 실패: " + e.getMessage(), null);
            }
            return false;
        }
    }

    // 4단계 채널 등록
    public boolean registerChannels(
            String batchId, ProductRegistrationRequest dto, List<String> retryChannels) {

        // (1) 이전에 이미 시도한 결과가 있다면 유지,
        //     없으면 신규 객체 생성 (재시도 상황 대응)
        ChannelResultDto coupangResult = (dto.getCoupangResult() != null)
                ? dto.getCoupangResult() : new ChannelResultDto();
        ChannelResultDto smartstoreResult = (dto.getSmartstoreResult() != null)
                ? dto.getSmartstoreResult() : new ChannelResultDto();
        ChannelResultDto elevenstResult = (dto.getElevenstResult() != null)
                ? dto.getElevenstResult() : new ChannelResultDto();

        // (2) 각 채널별로 재시도할 채널만 시도, 그 외에는 이전 결과 유지

        // 기존 매핑 정보 조회
        ProductChannelMapping mapping = mappingRepository.findByProductCode(dto.getCode())
                .orElse(null);

        // 쿠팡
        if (retryChannels.contains("coupang")) {
            try {
                String response = coupangApiService.enrollProducts(dto);
                System.out.println("■ response = " + response);
                // {"code":"ERROR","message":"['1번옵션의 1번인증정보의 타입'에 'HEALTH_FUNCTIONAL_FOOD_LICENSE' 입력할 수 없습니다, '1번옵션의 인증정보' 값을 확인해 주세요]","data":null,"details":null,"errorItems":null}
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response);
                String code = rootNode.path("code").asText();
                String message = rootNode.path("message").asText();
                String data = rootNode.path("data").isNull() ? null : rootNode.path("data").asText();
                if ("SUCCESS".equals(code)) {
                    // 성공 처리
                    if (mapping != null) {
                        mapping.setSellerProductId(data);
                        mappingRepository.save(mapping);
                    }

                    Product product = productRepository.findById(dto.getCode())
                            .orElseThrow(() -> new IllegalArgumentException("Not Found : " + dto.getCode()));


                    if (product != null) {
                        product.setDetailsHtml(dto.getDetailsHtml());
                        productRepository.save(product);
                    }

                    coupangResult.setStatus("SUCCESS");
                    coupangResult.setMessage("쿠팡 등록 성공");
                    coupangResult.setChannelProductId(data);
                } else {
                    // 실패 처리
                    throw new RuntimeException("쿠팡 등록 실패: " + message);
                }
            } catch (Exception e) {
                coupangResult.setStatus("FAIL");
                coupangResult.setMessage("쿠팡 등록 실패: " + e.getMessage());
                coupangResult.setChannelProductId(null);
            }
        }
        dto.setCoupangResult(coupangResult);

        // 스마트스토어
        if (retryChannels.contains("smartstore")) {
            try {
                String apiResponse = smartstoreApiService.enrollProducts(dto);
                // 응답에서 originProductNo 추출
                Long originProductNo = extractOriginProductNo(apiResponse);

                // ✅ 매핑 테이블에 스마트스토어 ID 저장
                if (mapping != null && originProductNo != null) {
                    mapping.setOriginProductNo(originProductNo.toString());
                    mappingRepository.save(mapping);
                }

                smartstoreResult.setStatus("SUCCESS");
                smartstoreResult.setMessage("스마트스토어 등록 성공");
                smartstoreResult.setChannelProductId(originProductNo != null ? originProductNo.toString() : null);
            } catch (Exception e) {
                smartstoreResult.setStatus("FAIL");
                smartstoreResult.setMessage("스마트스토어 등록 실패: " + e.getMessage());
                smartstoreResult.setChannelProductId(null);
            }
        }
        dto.setSmartstoreResult(smartstoreResult);


        // 11번가
        if (retryChannels.contains("elevenst")) {
            try {
                // API 호출하여 XML 응답 받기
                String xmlResponse = elevenstApiService.enrollProducts(dto);
                // XML 응답에서 productNo 추출
                String productNo = extractProductNoFromXml(xmlResponse);
                // ✅ 매핑 테이블에 11번가 ID 저장
                if (mapping != null && productNo != null) {
                    mapping.setElevenstId(productNo);
                    mappingRepository.save(mapping);
                }
                elevenstResult.setStatus("SUCCESS");
                elevenstResult.setMessage("11번가 등록 성공");
                elevenstResult.setChannelProductId(productNo);
            } catch (Exception e) {
                elevenstResult.setStatus("FAIL");
                elevenstResult.setMessage("11번가 등록 실패: " + e.getMessage());
                elevenstResult.setChannelProductId(null);
            }
        }
        dto.setElevenstResult(elevenstResult);

        ObjectMapper mapper = new ObjectMapper();
        String channelResultsLog;
        try {
            channelResultsLog = mapper.writeValueAsString(Map.of(
                    "coupang", dto.getCoupangResult(),
                    "smartstore", dto.getSmartstoreResult(),
                    "elevenst", dto.getElevenstResult()
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 최종적으로 DTO의 모든 채널 결과(성공/실패 포함) 최신상태 반영
        String detailsJson;
        try {
            detailsJson = objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            detailsJson = "";
        }

        boolean allSuccess = "SUCCESS".equals(coupangResult.getStatus()) &&
                        "SUCCESS".equals(smartstoreResult.getStatus()) &&
                        "SUCCESS".equals(elevenstResult.getStatus());

        String overallChannelStatus = allSuccess ? "DONE" : "FAIL";
        psr.updateProductStatus(batchId, dto.getCode(),
                "REGISTER_CHANNELS", overallChannelStatus, channelResultsLog, detailsJson);

        // 실제 결과에 따라 반환
        return allSuccess;
    }*/
}
