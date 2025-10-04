package com.jason.purchase_agent.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class JsonUtils {
    public static String safeJsonString(ObjectMapper mapper, Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "";
        }
    }

    // JsonUtils에 추가
    public static String listToJsonString(ObjectMapper mapper, List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    // JsonUtils에 추가
    public static Long extractOriginProductNo(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = mapper.readValue(jsonResponse,
                    new TypeReference<Map<String, Object>>() {});

            Object originProductNo = response.get("originProductNo");
            if (originProductNo != null) {
                return Long.valueOf(originProductNo.toString());
            }
            return null;
        } catch (Exception e) {
            System.out.println("■ originProductNo 추출 실패: " + e.getMessage());
            return null;
        }
    }

    // JsonUtils에 XML 파싱 메소드 추가 (또는 별도 XmlUtils 클래스 생성)
    public static String extractProductNoFromXml(String xmlResponse) {
        if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // XML 문자열을 InputStream으로 변환
            InputStream inputStream = new ByteArrayInputStream(xmlResponse.getBytes("euc-kr"));
            Document document = builder.parse(inputStream);

            // normalize 처리
            document.getDocumentElement().normalize();

            // productNo 요소 찾기
            NodeList productNoNodes = document.getElementsByTagName("productNo");
            if (productNoNodes.getLength() > 0) {
                return productNoNodes.item(0).getTextContent();
            }

            return null;
        } catch (Exception e) {
            System.out.println("■ XML에서 productNo 추출 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
