package com.jason.purchase_agent.service.product_registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jason.purchase_agent.dto.product_registration.*;
import com.jason.purchase_agent.entity.ProcessStatus;
import com.jason.purchase_agent.messaging.MessageQueueService;
import com.jason.purchase_agent.repository.jpa.ProcessStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

import static com.jason.purchase_agent.util.JsonUtils.safeJsonString;

@Slf4j
@Component
@RequiredArgsConstructor
// 메시지를 받아 실제 상품 등록 작업을 처리
public class ProductRegistrationQueueConsumer {

    private final ProductRegistrationProcessor processor;
    private final MessageQueueService messageQueueService;
    private final ProcessStatusRepository psr;
    private final ObjectMapper objectMapper;

    // 알림 서비스 (메일, 슬랙, 카카오톡 등)
    // private final ProductRegistrationAlertService alertService;

    /**
     * 배치(Batch) 단위 단계
     *   BATCH_START : 배치 전체 시작 (INIT)
     *   BATCH_PROGRESS : 배치 진행 중 (IN_PROGRESS)
     *   BATCH_FINISH : 배치 전체 완료(SUCCESS/FAIL)
     *
     * 상품(Product) 단위 단계
     *   SAVE_PRODUCT : 상품 DB 저장 (INIT -> SUCCESS/FAIL)
     *   DOWNLOAD_IMAGE : 이미지 로컬 다운로드 (SUCCESS/FAIL)
     *   UPLOAD_IMAGE : 이미지 ESM 서버 업로드 (SUCCESS/FAIL)
     *   REGISTER_CHANNELS : 각 채널 등록 (SUCCESS/FAIL)
     *
     */

    /**
     * [배치 단위] RabbitMQ로부터 메시지 수신하여 상품등록 배치 처리 시작
     * @param message - 배치ID, 상품목록 등
     */
    @RabbitListener(queues = "product_registration_queue")
    public void receive(ProductRegistrationMessage message) {

        log.info("■ [배치 시작] BatchId={} 상품등록 배치 시작", message.getBatchId());

        String batchId = message.getBatchId();
        List<ProductRegistrationRequest> products = message.getProducts();
        LocalDateTime now = LocalDateTime.now();

        // '배치' 로그 업데이트 - BATCH_PROGRESS (IN_PROGRESS)
        psr.updateBatchStatus(batchId, null,
                "BATCH_PROGRESS", "IN_PROGRESS", "상품등록 배치 진행중");

        /**
         * 1단계: 상품DB 저장 + 2단계: 이미지 로컬 다운로드 (각 상품 단위 처리)
         *  - 각 상품별로 1,2단계 처리
         *  - 1,2단계 모두 성공한 상품만 3단계 업로드 대상 리스트에 추가
         */
        List<ProductRegistrationRequest> readyForImageUpload = new ArrayList<>();
        for (ProductRegistrationRequest productDto : products) {
            // 유저가 생성한 각 상품의 등록정보를 JSON 형태로 details에 저장
            String details  = safeJsonString(objectMapper, productDto);
            // '배치의 각 상품' 로그 등록 - SAVE_PRODUCT (INIT)
            psr.save(ProcessStatus.builder()
                    .batchId(batchId).productCode(productDto.getCode()).startedAt(now).updatedAt(now)
                    .step("SAVE_PRODUCT").status("INIT").message("상품등록 개별상품 로그등록").details(details).build());
            // 1단계 상품DB 저장
            if (!processor.saveProductDB(batchId, productDto)) continue; // 1단계를 실패한 상품은 이후 단계 진행 불가
            // 2단계 이미지 로컬 다운로드
            if (!processor.downloadImages(batchId, productDto)) continue; // 2단계를 실패한 상품은 이후 단계 진행 불가

            // 2단계까지 성공한 경우만 업로드 리스트에 추가
            readyForImageUpload.add(productDto); // 2단계까지 성공한 경우만
        }

        // 2단계까지 성공한 상품이 없으면, 이후 단계 진행 안함
        // 3단계 이미지 업로드 (2단계까지 성공한 상품들만 업로드)
        if (!processor.uploadImages(batchId, readyForImageUpload)) {
            // 3단계 업로드 전체 실패시, 이후 단계 진행 안함
            psr.updateBatchStatus(batchId, null,
                    "BATCH_FINISH", "FAIL", "3단계 이미지 업로드 모두 실패");
            return;
        }

        // 4단계 채널 등록
        // 업로드 성공인 경우만 진행 (이미 FAIL 처리된 상품은 3단계에서 기록됨)
        for (ProductRegistrationRequest dto : readyForImageUpload) {
            processor.registerChannels(batchId, dto, List.of("coupang","smartstore","elevenst"));
        }

        psr.updateBatchStatus(batchId, null,
                "BATCH_FINISH", "SUCCESS", "상품등록 배치 전체 처리 완료");
        log.info("■ [배치 완료] BatchId={} 상품등록 배치 처리 완료", batchId);
    }

    @RabbitListener(queues = "product_registration_retry_queue")
    public void receiveRetry(ProductRegistrationRetryMessage message) {
        String batchId = message.getBatchId();
        LocalDateTime now = LocalDateTime.now();
        String startStep = message.getStartStep();

        log.info("■ [재시도 배치 시작] BatchId={} 상품등록 재시도 배치 시작", batchId);

        // 3단계 재시도의 경우 상품리스트 단위로 받음, 1,2,4단계 재시도의 경우 단일 상품
        // 3단계 재시도의 경우, 2단계까지 성공한 상품들만 리스트로 받음
        List<ProductRegistrationRequest> dtos = message.getProducts(); // 여러 상품
        ProductRegistrationRequest dto = message.getProduct(); // 단일 상품 (1,2,4단계)
        List<String> retryChannels = message.getRetryChannels(); // 재시도할 채널 목록 (4단계)

        switch (startStep) {
            case "SAVE_PRODUCT": // 1단계 성공시, 2단계 이후도 재시도
                if (!processor.saveProductDB(batchId, dto)) return; // 1단계 실패시 이후 단계 진행 안함
            case "DOWNLOAD_IMAGE": // 2단계 성공시, 3단계 이후도 재시도
                if (!processor.downloadImages(batchId, dto)) return; // 2단계 실패시 이후 단계 진행 안함
            case "UPLOAD_IMAGE": // 3단계 성공시, 4단계까지 재시도
                // 여러 상품을 일괄로!
                if (!processor.uploadImages(batchId, dtos)) return; // 3단계 실패시 이후 단계 진행 안함
                // 업로드 성공하면 이후 채널등록 진행
                for (ProductRegistrationRequest d : dtos) {
                    processor.registerChannels(batchId, d, retryChannels);
                }
                break;
            case "REGISTER_CHANNELS": // 4단계만 재시도
                processor.registerChannels(batchId, dto, retryChannels); // 마지막 단계까지
                break;
            default:
                throw new IllegalArgumentException("Unknown step: " + startStep);
        }
    }
}









    /*@RabbitListener(queues = "product_registration_queue")
    public void receive(ProductRegistrationMessage message) {

        String batchId = message.getBatchId();
        LocalDateTime now = LocalDateTime.now();
        List<ProductRegistrationDto> dtos = message.getProducts();
        // List<String> failedProductCodes = new ArrayList<>();

        // '배치' 로그 업데이트 - 전체 시작
        psr.updateBatchStatus(batchId, null,
                "BATCH_START", "IN_PROGRESS", "상품등록 배치 시작");

        List<ProductRegistrationDto> readyForUpload = new ArrayList<>(); // 2단계까지 성공한 상품만

        for (ProductRegistrationDto dto : dtos) {
            // ------ [1] 상품 DB 일괄 저장 단계 ------
            try {
                // '배치의 각 상품' 로그 등록 - DB 저장 대기
                psr.save(ProcessStatus.builder()
                        .batchId(batchId).productCode(dto.getCode()).startedAt(now).updatedAt(now)
                        .step("SAVE_PRODUCT").status("PENDING").message("상품 DB 저장 대기").build());
                Product product = new Product();
                BeanUtils.copyProperties(dto, product);
                productRepository.save(product);
                mappingRepository.save(ProductChannelMapping.builder().code(dto.getCode()).build());
                // '배치의 각 상품' 로그 업데이트 - 성공
                psr.updateProductStatus(batchId, dto.getCode(),
                        "SAVE_PRODUCT", "SUCCESS", "상품 DB 저장 완료");
            } catch (Exception e) {
                psr.updateProductStatus(batchId, dto.getCode(),
                        "SAVE_PRODUCT", "FAIL", "상품 DB 저장 실패: " + e.getMessage());
                continue; // 1단계 실패시 이후 단계 진행 안함
            }
            // ------ [2] 이미지 로컬 다운로드 단계 ------
            try {
                // '배치의 각 상품' 로그 업데이트 - 이미지 다운로드 대기
                psr.updateProductStatus(batchId, dto.getCode(),
                        "DOWNLOAD_IMAGE", "PENDING", "이미지 다운로드 대기");
                // 실제 이미지 다운로드 (파일경로 리스트 반환)
                List<String> localPaths = ImageDownloader.downloadImagesToLocal(dto.getCode(), dto.getImageLinks());
                dto.setImageFiles(localPaths);
                readyForUpload.add(dto); // 2단계까지 성공한 상품만 모아서 업로드 리스트에 추가
                // '배치의 각 상품' 로그 업데이트 - 이미지 다운로드 성공
                psr.updateProductStatus(batchId, dto.getCode(),
                        "DOWNLOAD_IMAGE", "SUCCESS", "이미지 다운로드 성공");
            } catch (Exception e) {
                psr.updateProductStatus(batchId, dto.getCode(),
                        "DOWNLOAD_IMAGE", "FAIL", "이미지 다운로드 실패: " + e.getMessage());
                continue; // 해야 돼 말아야 돼 헷갈리는 순간
                // 2단계 실패시 이후 단계 진행 안함
            }
        }

        // ------ [3] 3단계: 배치 단위 이미지 업로드 (2단계까지 성공한 상품들만 업로드) ------
        List<Map<String, Object>> imageUploadRequests = readyForUpload.stream()
                .map(dto -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", dto.getCode());
                    map.put("imageFiles", dto.getImageFiles());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, List<String>> codeToLinks = new HashMap<>();
        List<ProductRegistrationDto> readyForChannelRegister = new ArrayList<>(); // 3단계까지 성공한 상품만

        try {
            *//*psr.updateProductStatus(batchId, dto.getCode(),
                    "UPLOAD_IMAGE", "PENDING", "ESM 서버 이미지 업로드 대기");*//*
            List<ProductImageUploadResult> productImageUploadResults  = uploadImagesApi.batchUploadImages(imageUploadRequests);
            for (ProductImageUploadResult result : productImageUploadResults) {
                List<String> convertedLinks = result.getUploadedImageLinks().stream()
                        .map(link -> link.replace("https://", "http://"))
                        .collect(Collectors.toList());
                codeToLinks.put(result.getCode(), convertedLinks);
            }
            for (ProductRegistrationDto dto : readyForUpload) {
                List<String> links = codeToLinks.get(dto.getCode());
                if (links != null && !links.isEmpty()) {
                    dto.setUploadedImageLinks(links);
                    psr.updateProductStatus(batchId, dto.getCode(), "UPLOAD_IMAGE", "SUCCESS", "ESM 서버 이미지 업로드 성공");
                    readyForChannelRegister.add(dto); // 3단계까지 성공한 상품만 채널 등록 준비
                } else {
                    psr.updateProductStatus(batchId, dto.getCode(), "UPLOAD_IMAGE", "FAIL", "ESM 서버 이미지 업로드 실패: 결과 없음");
                }
            }
        } catch (Exception e) {
            for (ProductRegistrationDto dto : readyForUpload) {
                psr.updateProductStatus(batchId, dto.getCode(),
                        "UPLOAD_IMAGE", "FAIL", "ESM 업로드 전체 에러: " + e.getMessage());
            }
            return;
        }

        // 4단계: 각 상품별 채널 등록
        for (ProductRegistrationDto dto : readyForChannelRegister) {
            ChannelResultDto coupangResult = new ChannelResultDto();
            ChannelResultDto smartstoreResult = new ChannelResultDto();
            ChannelResultDto elevenstResult = new ChannelResultDto();
            ProductChannelMapping mapping = mappingRepository.findByCode(dto.getCode());

            // 쿠팡
            try {
                CoupangApiResponse coupangApiResponse = coupangApiService.enrollProducts(dto);
                String sellerProductId = coupangApiResponse.getBody().getData();
                if (mapping != null) {
                    mapping.setSellerProductId(sellerProductId);
                    mappingRepository.save(mapping);
                }
                Product product = productRepository.findByCode(dto.getCode());
                if (product != null) {
                    product.setDetailsHtml(dto.getDetailsHtml());
                    productRepository.save(product);
                }
                coupangResult.setStatus("SUCCESS");
                coupangResult.setMessage("쿠팡 등록 성공");
                coupangResult.setChannelProductId(sellerProductId);
            } catch (Exception e) {
                coupangResult.setStatus("FAIL");
                coupangResult.setMessage("쿠팡 등록 실패: " + e.getMessage());
                coupangResult.setChannelProductId(null);
            }
            dto.setCoupangResult(coupangResult);

            // 스마트스토어
            try {
                smartstoreApiService.enrollProducts(dto);
                smartstoreResult.setStatus("SUCCESS");
                smartstoreResult.setMessage("스마트스토어 등록 성공");
                smartstoreResult.setChannelProductId(null);
            } catch (Exception e) {
                smartstoreResult.setStatus("FAIL");
                smartstoreResult.setMessage("스마트스토어 등록 실패: " + e.getMessage());
                smartstoreResult.setChannelProductId(null);
            }
            dto.setSmartstoreResult(smartstoreResult);

            // 11번가
            try {
                elevenstApiService.enrollProducts(dto);
                elevenstResult.setStatus("SUCCESS");
                elevenstResult.setMessage("11번가 등록 성공");
                elevenstResult.setChannelProductId(null);
            } catch (Exception e) {
                elevenstResult.setStatus("FAIL");
                elevenstResult.setMessage("11번가 등록 실패: " + e.getMessage());
                elevenstResult.setChannelProductId(null);
            }
            dto.setElevenstResult(elevenstResult);

            ObjectMapper mapper = new ObjectMapper();
            String channelResultsLog = null;
            try {
                channelResultsLog = mapper.writeValueAsString(Map.of(
                        "coupang", dto.getCoupangResult(),
                        "smartstore", dto.getSmartstoreResult(),
                        "elevenst", dto.getElevenstResult()
                ));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            // 3채널 결과 status 검사
            boolean allSuccess =
                    "SUCCESS".equals(coupangResult.getStatus()) &&
                    "SUCCESS".equals(smartstoreResult.getStatus()) &&
                    "SUCCESS".equals(elevenstResult.getStatus());

            String overallChannelStatus = allSuccess ? "DONE" : "FAIL";
            // '배치의 각 상품' 로그 업데이트 - 채널 등록 완료
            psr.updateProductStatus(
                    batchId, dto.getCode(),
                    "REGISTER_CHANNELS", overallChannelStatus, channelResultsLog);
        }

        // 최종 배치 로그 (모든 상품 처리 후)
        psr.updateBatchStatus(batchId, null,
                "BATCH_FINISH", "SUCCESS", "상품등록 배치 전체 처리 완료");
        log.info("■ [배치 완료] BatchId={} 상품등록 배치 처리 완료", batchId);
    }*/
