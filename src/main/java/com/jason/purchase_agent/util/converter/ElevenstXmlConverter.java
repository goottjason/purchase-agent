package com.jason.purchase_agent.util.converter;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElevenstXmlConverter {
    private static final XmlMapper xmlMapper;

    static {
        xmlMapper = new XmlMapper();
        // XML 선언 및 인코딩 설정
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
    }

    /**
     * DTO를 11번가 API용 XML로 변환
     */
    public <T> String convertToXml(T request) {

        try {
            String xml = xmlMapper.writeValueAsString(request);

            // euc-kr 인코딩 설정 (11번가 API 요구사항)
            xml = xml.replace(
                    "<?xml version='1.0' encoding='UTF-8'?>",
                    "<?xml version=\"1.0\" encoding=\"euc-kr\" standalone=\"yes\"?>");

            // 특정 필드들을 CDATA로 감싸기
            xml = wrapWithCDATA(xml, "prdNm");           // 상품명
            xml = wrapWithCDATA(xml, "prdNmEng");       // 영어상품명
            xml = wrapWithCDATA(xml, "advrtStmt");       // 상품홍보문구
            xml = wrapWithCDATA(xml, "htmlDetail");     // HTML 상세설명
            xml = wrapWithCDATA(xml, "brand");          // 브랜드명 (필요시
            return xml;
        } catch (Exception e) {
            throw new RuntimeException("11번가 XML 변환 실패", e);
        }
    }

    /**
     * 특정 태그의 내용을 CDATA로 감싸기
     */
    /**
     * 특정 XML 태그의 내용을 CDATA로 감싸는 메소드
     */
    /**
     * 특정 XML 태그의 내용을 CDATA로 감싸는 메소드 (개선 버전)
     */
    private String wrapWithCDATA(String xml, String tagName) {
        // 1. 자기 닫는 태그 처리: <tagName/>
        String selfClosingPattern = "<" + tagName + "\\s*/>";
        xml = xml.replaceAll(selfClosingPattern, "<" + tagName + "></" + tagName + ">");

        // 2. 빈 태그 처리: <tagName></tagName>
        String emptyPattern = "<" + tagName + "></" + tagName + ">";
        // 빈 태그는 CDATA로 감싸지 않음

        // 3. 내용이 있는 태그만 CDATA로 감싸기
        String contentPattern = "<" + tagName + ">([^<]+)</" + tagName + ">";
        String replacement = "<" + tagName + "><![CDATA[$1]]></" + tagName + ">";

        return xml.replaceAll(contentPattern, replacement);
    }
    /**
     * 빈 태그 제거 (옵션 미사용 시)
     */
    public String removeEmptyTags(String xml) {
        // 빈 태그 패턴 제거: <tagName/>
        return xml.replaceAll("<[^>]+/>", "")
                .replaceAll("\\s*\n\\s*\n", "\n"); // 빈 줄 정리
    }
}
