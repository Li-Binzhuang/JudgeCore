package org.laoli.judge.interfaces.mq;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @Description 消息队列生产者（支持 Kafka 和 RocketMQ）
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Component
public class JudgeMessageProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public JudgeMessageProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            RocketMQTemplate rocketMQTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.rocketMQTemplate = rocketMQTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 发送 Kafka 消息
     */
    public void sendKafkaMessage(String topic, JudgeRequest request) {
        try {
            String message = objectMapper.writeValueAsString(request);
            kafkaTemplate.send(topic, message);
            log.info("Sent Kafka message to topic: {}", topic);
        } catch (Exception e) {
            log.error("Error sending Kafka message", e);
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }
    
    /**
     * 发送 RocketMQ 消息
     */
    public void sendRocketMQMessage(String topic, JudgeRequest request) {
        try {
            String message = objectMapper.writeValueAsString(request);
            rocketMQTemplate.send(topic, MessageBuilder.withPayload(message).build());
            log.info("Sent RocketMQ message to topic: {}", topic);
        } catch (Exception e) {
            log.error("Error sending RocketMQ message", e);
            throw new RuntimeException("Failed to send RocketMQ message", e);
        }
    }
}
