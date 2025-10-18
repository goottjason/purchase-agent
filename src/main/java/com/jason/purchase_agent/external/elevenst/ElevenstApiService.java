package com.jason.purchase_agent.external.elevenst;

import com.jason.purchase_agent.dto.channel.elevenst.ElevenstProductRequest;
import com.jason.purchase_agent.dto.product_registration.ProductRegistrationRequest;
import com.jason.purchase_agent.util.converter.ElevenstXmlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElevenstApiService {
    private final ElevenstXmlConverter xmlConverter = new ElevenstXmlConverter();

    public String updatePrice(String elevenstId, Integer salePrice) {
        try {
            String urlPrice = "http://api.11st.co.kr/rest/prodservices/product/price/" + elevenstId + "/" + salePrice;

            String response = ElevenstApiUtil.sendRequest(urlPrice, "GET", null);
            return response;
        } catch (Exception e) {
            log.error("[ElevenstUpdatePrice] 요청 에러 (elevenstId={}, salePrice={}, e.getMessage()={}",
                    elevenstId, salePrice, e.getMessage());
            // 필요시 에러 JSON etc
            return "{}";
        }
    }

    public String updateStock(String elevenstId, Integer stock) {
        try {
            String urlStock = (stock != 0)
                    ? "http://api.11st.co.kr/rest/prodstatservice/stat/restartdisplay/" + elevenstId
                    : "http://api.11st.co.kr/rest/prodstatservice/stat/stopdisplay/" + elevenstId;

            String response = ElevenstApiUtil.sendRequest(urlStock, "PUT", null);
            return response;
        } catch (Exception e) {
            log.error("[ElevenstUpdateStock] 요청 에러 (elevenstId={}, stock={}, e.getMessage()={}",
                    elevenstId, stock, e.getMessage());
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

    public String registerProduct(ProductRegistrationRequest request) throws Exception {
        try {
            // 1. 빌더 메서드로 ElevenstProductRequest 생성
            ElevenstProductRequest elevenstProductRequest = ElevenstProductRequest.fromForm(request);
            // 2. API 요청 직전에 XML 변환
            String xmlRequest = xmlConverter.convertToXml(elevenstProductRequest);
            String url = "http://api.11st.co.kr/rest/prodservices/product";
            // 3. 전송
            String responseXml = ElevenstApiUtil.sendRequest(url, "POST", xmlRequest);
            // 4. 응답 반환 (XML 문자열)
            return responseXml;
        } catch (Exception e) {
            log.error("[ElevenstRegister] 요청 에러 (request={}, e.getMessage()={}",
                    request, e.getMessage());
            // 필요시 에러 JSON etc
            return "{}";
        }
    }
}
