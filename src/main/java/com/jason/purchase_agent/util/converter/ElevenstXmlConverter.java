package com.jason.purchase_agent.util.converter;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.jason.purchase_agent.dto.channel.elevenst.ElevenstListingEnrollRequest;

public class ElevenstXmlConverter {
  private final XmlMapper xmlMapper;

  public ElevenstXmlConverter() {
    this.xmlMapper = new XmlMapper();
    // XML 선언 및 인코딩 설정
    xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
  }

  /**
   * DTO를 11번가 API용 XML로 변환
   */
  public String convertToXml(ElevenstListingEnrollRequest request) {
    try {
      String xml = xmlMapper.writeValueAsString(request);

      // euc-kr 인코딩 설정 (11번가 API 요구사항)
      xml = xml.replace("<?xml version='1.0' encoding='UTF-8'?>",
        "<?xml version=\"1.0\" encoding=\"euc-kr\" standalone=\"yes\"?>");

      return xml;

    } catch (Exception e) {
      throw new RuntimeException("11번가 XML 변환 실패", e);
    }
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
