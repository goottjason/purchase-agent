package com.jason.purchase_agent.service.product_registration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jason.purchase_agent.dto.channel.coupang.CoupangCategoryMetaInfoDto;
import com.jason.purchase_agent.dto.channel.coupang.CoupangProductRequest;
import com.jason.purchase_agent.dto.product_registration.*;
import com.jason.purchase_agent.dto.products.ProductDto;
import com.jason.purchase_agent.external.cafe.CafeApiService;
import com.jason.purchase_agent.external.coupang.CoupangApiService;
import com.jason.purchase_agent.external.elevenst.ElevenstApiService;
import com.jason.purchase_agent.external.smartstore.SmartstoreApiService;
import com.jason.purchase_agent.messaging.MessageQueueService;
import com.jason.purchase_agent.service.process_status.ProcessStatusService;
import com.jason.purchase_agent.service.products.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;
import static com.jason.purchase_agent.util.exception.ExceptionUtils.uncheck;

@Slf4j
@Component
@RequiredArgsConstructor
// 메시지를 받아 실제 상품 등록 작업을 처리
public class ProductRegistrationQueueConsumer {
    private final ProductService productService;
    private final MessageQueueService messageQueueService;

    private final ProcessStatusService pss;

    private final CoupangApiService coupangApiService;
    private final SmartstoreApiService smartstoreApiService;
    private final ElevenstApiService elevenstApiService;
    private final CafeApiService cafeApiService;


    @RabbitListener(queues = "register-product-to-coupang", concurrency = "1")
    public void handleRegisterProductToCoupang(
            ProductRegistrationMessage msg
    ) {
        try {
            String batchId = msg.getBatchId();
            ProductRegistrationRequest request = msg.getRequest();
            ProductDto productDto = msg.getRequest().getProductDto();

            // 1-1. CoupangProductRequest from 팩토리메서드로 빌드
            // (productName, startedAt, displayProductName, brand, generalProductName 세팅)
            CoupangProductRequest.CoupangProductRequestBuilder coupangProductRequest
                    = CoupangProductRequest.from(productDto).toBuilder();
            // 1-2. product의 korName, brandName을 토대로 카테고리추천 API 호출 (displayCategoryCode 세팅)
            Long categoryId = uncheck(() -> Long.parseLong(coupangApiService.recommendDisplayCategory(productDto)));
            coupangProductRequest.displayCategoryCode(categoryId);
            // 1-3. categoryId를 토대로 카테고리 메타정보 API 호출
            // (CoupangCategoryMetaInfoDto 빌드 및 Attributes 세팅)
            CoupangCategoryMetaInfoDto metaInfo = uncheck(() -> coupangApiService.fetchCategoryMetaInfo(categoryId));
            List<CoupangProductRequest.Attribute> attributes =
                    coupangApiService.buildRequiredAttributes(metaInfo, productDto);
            // 1-4. Notice 세팅
            List<CoupangProductRequest.Notice> notices = coupangApiService.buildNoticeByProductType(productDto);
            // 1-5. Image 세팅
            List<CoupangProductRequest.Image> images = coupangApiService.buildImageList(request.getUploadedImageLinks());
            // 1-6. Content 세팅 (내부에서 detailsHtml 세팅)
            List<CoupangProductRequest.Content> contents = coupangApiService.buildProductContents(request);
            // 1-7. Item 세팅
            CoupangProductRequest.Item item = CoupangProductRequest.Item.builder()
                    .itemName(coupangApiService.buildItemName(productDto))
                    .originalPrice((int) (Math.round(productDto.getSalePrice() * (1 + productDto.getMarginRate() / 100.0) / 100.0) * 100))
                    .salePrice(productDto.getSalePrice())
                    .unitCount(productDto.getPackQty() > 0 ? productDto.getPackQty() : 1)
                    .externalVendorSku(productDto.getCode())
                    .images(images)
                    .notices(notices)
                    .attributes(attributes)
                    .contents(contents)
                    // .certifications(certifications)
                    .build();
            coupangProductRequest.items(List.of(item));

            // 2. 상품등록 요청
            String responseJson = coupangApiService.registerProduct(coupangProductRequest.build());
            JsonNode root = objectMapper.readTree(responseJson);

            // 성공 조건
            boolean findSuccess = false;
            JsonNode dataNode = root.get("data");
            Map<String, Object> channelResult = new HashMap<>();
            if (dataNode.isNull()) { // 실패
                JsonNode messageNode = root.get("message");
                String message = messageNode != null ? messageNode.asText() : null;
                channelResult.put("status", "FAIL");
                channelResult.put("message", message);
                log.error("[{}][Coupang][Register] 실패 (responseJson={})",
                        productDto.getCode(), responseJson);
            } else { // 성공
                JsonNode innerDataNode = dataNode.path("data");
                String sellerProductId = innerDataNode != null ? innerDataNode.asText() : null;
                String message = String.format("상품등록 완료");
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", message);
                log.info("[{}][Coupang][Register] 성공 (sellerProductId={})",
                        productDto.getCode(), sellerProductId);
                // sellerProductId 세팅
                productDto.setSellerProductId(sellerProductId);

                // 2. Product 및 Mapping에 업데이트 (sellerProductId, detailsHtml)
                productService.saveProductAndMapping(productDto);

                // 3. 쿠팡 완료 후, 스마트스토어와 11번가 메세지 발행
                messageQueueService.publishRegisterProductToSmartstore(batchId, request, msg.getTotalProductCount());
                messageQueueService.publishRegisterProductToElevenst(batchId, request, msg.getTotalProductCount());
            }
            pss.mergeChannelResult(msg.getBatchId(), productDto.getCode(), "coupang", channelResult);

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        } catch (Exception e) {
            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            pss.mergeChannelResult(
                    msg.getBatchId(), msg.getRequest().getProductDto().getCode(), "coupang", channelResult
            );

            log.error("[{}][Coupang][Stock] 실패 (e.getMessage()={})",
                    msg.getRequest().getProductDto().getCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

    @RabbitListener(queues = "register-product-to-smartstore", concurrency = "1")
    public void handleRegisterProductToSmartstore(
            ProductRegistrationMessage msg
    ) {
        try {
            String batchId = msg.getBatchId();
            ProductRegistrationRequest request = msg.getRequest();
            ProductDto productDto = msg.getRequest().getProductDto();

            String responseJson = smartstoreApiService.registerProduct(request);

            Map<String, Object> channelResult = new HashMap<>();
            JsonNode secondRoot = objectMapper.readTree(responseJson);
            JsonNode originNode = secondRoot.get("originProductNo");
            if (originNode != null && !originNode.isMissingNode()) {
                // 성공: 값 추출
                Long originProductNo = originNode.asLong();
                String message = String.format("상품등록 완료 (originProductNo={}", originProductNo);
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", message);
                log.info("[{}][Coupang][Register] 성공 (originProductNo={})",
                        productDto.getCode(), originProductNo);
                // sellerProductId 세팅
                productDto.setOriginProductNo(String.valueOf(originProductNo));
            } else {
                // 실패
                String message = ""; // 테스트해보고 json 구조 확인
                channelResult.put("status", "FAIL");
                channelResult.put("message", message);
                log.error("[{}][Smartstore][Register] 실패 (responseJson={})",
                        productDto.getCode(), responseJson);
            }
        } catch (Exception e) {
            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            pss.mergeChannelResult(
                    msg.getBatchId(), msg.getRequest().getProductDto().getCode(), "coupang", channelResult
            );

            log.error("[{}][Smartstore][Register] 실패 (e.getMessage()={})",
                    msg.getRequest().getProductDto().getCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

    @RabbitListener(queues = "register-product-to-elevenst", concurrency = "1")
    public void handleRegisterProductToElevenst(
            ProductRegistrationMessage msg
    ) {
        try {
            String batchId = msg.getBatchId();
            ProductRegistrationRequest request = msg.getRequest();
            ProductDto productDto = msg.getRequest().getProductDto();

            String responseXml = elevenstApiService.registerProduct(request);
            XmlMapper xmlMapper = new XmlMapper();
            Map<String, Object> xmlResult = xmlMapper.readValue(responseXml, Map.class);
            String code = String.valueOf(xmlResult.getOrDefault("resultCode", ""));
            String returnedMessage = String.valueOf(xmlResult.getOrDefault("message", "상세 메시지 없음"));
            // 성공 조건: resultCode == 200 || (가격 동일시 보통 "동일한 가격"류 메시지가 포함)
            boolean findSuccess = "200".equals(code);
            Map<String, Object> channelResult = new HashMap<>();
            if (findSuccess) {
                String elevenstId = String.valueOf(xmlResult.getOrDefault("productNo", ""));
                channelResult.put("status", "SUCCESS");
                channelResult.put("message", String.format("상품 등록 완료 (%s)", elevenstId));
                productDto.setElevenstId(elevenstId);
                log.info("[{}][Elevenst][Register] 성공 (elevenstId={})",
                        productDto.getCode(), elevenstId);
            } else {
                channelResult.put("status", "FAIL");
                channelResult.put("message", code + " : " + returnedMessage);
                log.error("[{}][Elevenst][Register] 실패 (responseXml={})",
                        productDto.getCode(), responseXml);
            }
            pss.mergeChannelResult(
                    batchId, productDto.getCode(), "coupang", channelResult);
            pss.updateBatchProgressSummary(batchId, msg.getTotalProductCount());
        } catch (Exception e) {
            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            pss.mergeChannelResult(
                    msg.getBatchId(), msg.getRequest().getProductDto().getCode(), "coupang", channelResult
            );

            log.error("[{}][Elevenst][Register] 실패 (e.getMessage()={})",
                    msg.getRequest().getProductDto().getCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }

    @RabbitListener(queues = "register-product-to-cafe", concurrency = "1")
    public void handleRegisterProductToCafe(
            ProductRegistrationMessage msg
    ) {
        try {
            String batchId = msg.getBatchId();
            ProductRegistrationRequest request = msg.getRequest();
            ProductDto productDto = msg.getRequest().getProductDto();
            String responseJson = cafeApiService.registerProduct(request);

            Map<String, Object> channelResult = new HashMap<>();

            // JSON 파싱
            JsonNode root = objectMapper.readTree(responseJson);
            // responseJson에서 product_no, product_code 추출하여 cafeNo, cafeCode, cafeOptCode(cafeCode+"000A") 추출하며 추출되면 성공
            if (root.has("product")) {
                JsonNode prodNode = root.get("product");
                int cafeNo = prodNode.path("product_no").asInt(-1);
                String cafeCode = prodNode.path("product_code").asText();
                // 옵션코드 같은 경우 규칙 적용
                String cafeOptCode = cafeCode + "000A";
                // 필수값 보장 확인
                if (cafeNo > 0 && cafeCode != null && !cafeCode.isEmpty()) {
                    String message = String.format("상품등록 완료 (cafeNo=%d, cafeCode=%s, cafeOptCode=%s)", cafeNo, cafeCode, cafeOptCode);
                    channelResult.put("status", "SUCCESS");
                    channelResult.put("message", message);

                    log.info("[{}][Cafe][Register] 성공 (cafeNo={}, cafeCode={})",
                            productDto.getCode(), cafeNo, cafeCode);

                    // 결과 ProductDto 세팅
                    productDto.setCafeNo(String.valueOf(cafeNo));
                    productDto.setCafeCode(cafeCode);
                    productDto.setCafeOptCode(cafeOptCode);
                } else {
                    String message = "상품 등록 결과에서 필수정보 추출 실패";
                    channelResult.put("status", "FAIL");
                    channelResult.put("message", message);

                    log.error("[{}][Cafe][Register] 실패 (필수정보 누락, responseJson={})",
                            productDto.getCode(), responseJson);
                }
            } else {
                // 실패
                String message = ""; // 테스트해보고 json 구조 확인
                channelResult.put("status", "FAIL");
                channelResult.put("message", message);
                log.error("[{}][Smartstore][Register] 실패 (responseJson={})",
                        productDto.getCode(), responseJson);
            }
        } catch (Exception e) {
            Map<String, Object> channelResult = new HashMap<>();
            channelResult.put("status", "FAIL");
            channelResult.put("message", "[예외] " + e.getMessage());

            pss.mergeChannelResult(
                    msg.getBatchId(), msg.getRequest().getProductDto().getCode(), "coupang", channelResult
            );

            log.error("[{}][Cafe][Register] 실패 (e.getMessage()={})",
                    msg.getRequest().getProductDto().getCode(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("MQ 폐기(파싱 실패)", e);
        }
    }
}