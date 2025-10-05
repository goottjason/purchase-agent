package com.jason.purchase_agent.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 메시지 컨버터 (JSON)
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate: message converter 반드시 적용
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter()); // 반드시 설정
        return template;
    }

    // ===========================
    // 큐 선언부 (durable=true 일관)
    // ===========================

    // ===========================
    // 큐 선언부 (durable=true 일관)
    // ===========================
    @Bean
    public Queue productRegistrationQueue() {
        return new Queue("product_registration_queue", true);
    }
    @Bean
    public Queue productRegistrationRetryQueue() {
        return new Queue("product_registration_retry_queue", true);
    }
    @Bean
    public Queue autoUpdateQueue() {
        return new Queue("product.auto_update", true);
    }
    @Bean
    public Queue productsBatchAutoPriceStockUpdateQueue() {
        return new Queue("products.batch_auto_price_stock_update_queue", true);
    }
    @Bean
    public Queue productsBatchManualPriceStockUpdateQueue() {
        return new Queue("products.batch_manual_price_stock_update_queue", true);
    }

    // ===========================
    // price/stock update 큐들 추가
    // ===========================
    @Bean
    public Queue priceUpdateCoupangQueue() {
        return new Queue("price-update-coupang", true);
    }
    @Bean
    public Queue priceUpdateSmartstoreQueue() {
        return new Queue("price-update-smartstore", true);
    }
    @Bean
    public Queue priceUpdateElevenstQueue() {
        return new Queue("price-update-elevenst", true);
    }
    @Bean
    public Queue stockUpdateCoupangQueue() {
        return new Queue("stock-update-coupang", true);
    }
    @Bean
    public Queue stockUpdateSmartstoreQueue() {
        return new Queue("stock-update-smartstore", true);
    }
    @Bean
    public Queue stockUpdateElevenstQueue() {
        return new Queue("stock-update-elevenst", true);
    }
    @Bean
    public Queue vendorItemIdSyncCoupangQueue() { return new Queue("vendoritemid-sync-coupang", true); }
}
