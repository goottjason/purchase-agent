package com.jason.purchase_agent;

import com.jason.purchase_agent.external.elevenst.ElevenstApiService;
import com.jason.purchase_agent.util.converter.ElevenstXmlConverter;
import com.jason.purchase_agent.util.salechannelapi.elevenst.ElevenstApiUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ElevenstApiServiceTests {

    @Autowired
    private ElevenstApiService elevenstApiService;

    @Autowired
    private ElevenstApiUtil elevenstApiUtil;

    @Autowired
    private ElevenstXmlConverter elevenstXmlConverter;

    @Test
    void 출고지_조회() {
        // 1) Dto
        // 2. API 요청 직전에 XML 변환
        // String xmlRequest = elevenstXmlConverter.convertToXml(request);
        String xmlRequest = null;
        // 3) 전송
        String response = ElevenstApiUtil.sendRequest(
                "http://api.11st.co.kr/rest/areaservice/outboundarea",
                "GET",
                xmlRequest);
        System.out.println("■Test■ response = " + response);
    }
    @Test
    void 반품교환지_조회() {
        // 1) Dto
        // 2. API 요청 직전에 XML 변환
        // String xmlRequest = elevenstXmlConverter.convertToXml(request);
        String xmlRequest = null;
        // 3) 전송
        String response = ElevenstApiUtil.sendRequest(
                "http://api.11st.co.kr/rest/areaservice/inboundarea",
                "GET",
                xmlRequest);
        System.out.println("■Test■ response = " + response);
    }
}
