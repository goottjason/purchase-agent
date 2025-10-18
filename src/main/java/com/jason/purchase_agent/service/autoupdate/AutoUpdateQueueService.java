package com.jason.purchase_agent.service.autoupdate;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutoUpdateQueueService {
    private final AmqpTemplate amqpTemplate;
    private static final String QUEUE_NAME = "product.auto_update";

    /**
     * 단일 상품 메시지(MQ) 발행 메서드
     * @param message 배치ID, 공급업체, 상품코드 등의 정보를 담은 메시지 DTO
     *//*
    public void publishAutoUpdate(AutoUpdateMessage message) {
        // AmqpTemplate을 통해 지정한 큐명에 메시지를 발행(serialize하여 MQ로 쏨)
        amqpTemplate.convertAndSend(QUEUE_NAME, message);
        // (Spring은 직렬화 자동 처리, 메시지 = 자바 객체 → MQ에 들어감)
        // 큐에 쌓이고 나면 오토MQ Consumer에서 알아서 병렬로 처리 시작됨
    }*/
}
