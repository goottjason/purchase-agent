package com.jason.purchase_agent.external.elevenst;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jason.purchase_agent.dto.channel.elevenst.ElevenstEnrollRequest;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationDto;
import com.jason.purchase_agent.util.salechannelapi.elevenst.ElevenstApiUtil;
import com.jason.purchase_agent.util.converter.ElevenstXmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.jason.purchase_agent.util.converter.StringListConverter.objectMapper;
@Slf4j
@Service
@RequiredArgsConstructor
public class ElevenstApiService {
    private final ElevenstXmlConverter xmlConverter = new ElevenstXmlConverter();

    public String updatePrice(String elevenstId, Integer salePrice) {
        log.info("[11stAPI][Price] 가격 변경 요청 - elevenstId={}, salePrice={}", elevenstId, salePrice);
        try {
            String urlPrice = "http://api.11st.co.kr/rest/prodservices/product/price/" + elevenstId + "/" + salePrice;
            log.debug("[11stAPI][Price] 요청 URL: {}", urlPrice);

            String response = ElevenstApiUtil.sendRequest(urlPrice, "GET", null);
            log.debug("[11stAPI][Price] API 응답(XML): {}", response);
            return response;
        } catch (Exception e) {
            log.error("[11stAPI][Price] 가격 변경 장애 - elevenstId={}, salePrice={}, 원인={}", elevenstId, salePrice, e.getMessage(), e);
            // 필요시 에러 JSON etc
            return "{}";
        }
    }

    public String updateStock(String elevenstId, Integer stock) {
        log.info("[11stAPI][Stock] 재고 변경 요청 - elevenstId={}, stock={}", elevenstId, stock);
        try {
            String urlStock = (stock != 0)
                    ? "http://api.11st.co.kr/rest/prodstatservice/stat/restartdisplay/" + elevenstId
                    : "http://api.11st.co.kr/rest/prodstatservice/stat/stopdisplay/" + elevenstId;
            log.debug("[11stAPI][Stock] 요청 URL: {}", urlStock);

            String response = ElevenstApiUtil.sendRequest(urlStock, "PUT", null);
            log.debug("[11stAPI][Stock] API 응답(XML): {}", response);
            return response;
        } catch (Exception e) {
            log.error("[11stAPI][Stock] 재고 변경 장애 - elevenstId={}, stock={}, 원인={}", elevenstId, stock, e.getMessage(), e);
            return "{}";
        }
    }



    public void updatePriceStock(String elevenstId, Integer salePrice, Integer stock) {

        String urlPrice = "http://api.11st.co.kr/rest/prodservices/product/price/" + elevenstId + "/" + salePrice;
        String responseStock = ElevenstApiUtil.sendRequest(
                urlPrice,
                "GET",
                null
        );
        System.out.println("responseStock = " + responseStock);

        String urlStock;
        if (stock != 0) {
            urlStock = "http://api.11st.co.kr/rest/prodstatservice/stat/restartdisplay/" + elevenstId;
        } else {
            urlStock = "http://api.11st.co.kr/rest/prodstatservice/stat/stopdisplay/" + elevenstId;
        }

        String responsePrice = ElevenstApiUtil.sendRequest(
                urlStock,
                "PUT",
                null
        );
        System.out.println("responsePrice = " + responsePrice);
    }

    public String enrollProducts(ProductRegistrationDto product) throws Exception {

        // 1. 빌더 메서드로 ElevenstProductRequest 생성
        ElevenstEnrollRequest elevenstEnrollRequest = ElevenstEnrollRequest.fromForm(product);
        // 2. API 요청 직전에 XML 변환
        String xmlRequest = xmlConverter.convertToXml(elevenstEnrollRequest);
        System.out.println("◆◆◆◆◆ xmlRequest = " + xmlRequest);
        // 3. 전송
        String response = ElevenstApiUtil.sendRequest(
                "http://api.11st.co.kr/rest/prodservices/product",
                "POST",
                xmlRequest);
        // 4. 응답 반환 (XML 문자열)
        return response;
    }
}
