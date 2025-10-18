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

    @Bean
    public Queue registerProductToCoupang() { return new Queue("register-product-to-coupang", true); }
    @Bean
    public Queue registerProductToSmartstore() { return new Queue("register-product-to-smartstore", true); }
    @Bean
    public Queue registerProductToElevenst() { return new Queue("register-product-to-elevenst", true); }
    @Bean
    public Queue registerProductToCafe() { return new Queue("register-product-to-cafe", true); }

    @Bean
    public Queue crawlAndUpdateEachProductBySupplierQueue() { return new Queue("crawl-and-update-price-stock", true); }
    @Bean
    public Queue manualUpdatePriceStockQueue() { return new Queue("manual-update-price-stock", true); }
    @Bean
    public Queue manualUpdateAllFieldsQueue() { return new Queue("manual-update-all-fields", true); }

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
    public Queue priceUpdateCafeQueue() { return new Queue("price-update-cafe", true); }
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
    public Queue stockUpdateCafeQueue() { return new Queue("stock-update-cafe", true); }
}
