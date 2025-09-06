package com.jason.purchase_agent;

import com.jason.purchase_agent.service.channel.elevenst.ElevenstService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ElevenstServiceTests {

  @Autowired
  private ElevenstService elevenstService;

  @Test
  void testRegisterProduct() {
    // When & Then
//    elevenstService.register();

    // 콘솔에서 XML 출력 확인
  }

}
