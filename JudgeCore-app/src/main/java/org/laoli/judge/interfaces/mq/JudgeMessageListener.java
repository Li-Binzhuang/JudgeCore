package org.laoli.judge.interfaces.mq;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.application.JudgeApplicationService;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.dto.JudgeResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description Kafka 消息监听器（条件启用：当 bootstrap-servers 不是 "none" 时启用）
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Component
@ConditionalOnExpression("'${spring.kafka.bootstrap-servers:192.168.10.232:9092}' != 'none'")
public class JudgeMessageListener {
    
    private final JudgeApplicationService judgeApplicationService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public JudgeMessageListener(JudgeApplicationService judgeApplicationService, ObjectMapper objectMapper) {
        this.judgeApplicationService = judgeApplicationService;
        this.objectMapper = objectMapper;
    }
    
    @KafkaListener(topics = "${judge.mq.kafka.topic:judge-request}", groupId = "${judge.mq.kafka.group-id:judge-group}")
    public void handleKafkaMessage(@Payload String message, Acknowledgment acknowledgment) {
        try {
            log.info("Received Kafka message: {}", message);
            JudgeRequest request = objectMapper.readValue(message, JudgeRequest.class);
            JudgeResponse response = judgeApplicationService.judge(request);
            
            // 处理响应（可以发送到响应队列）
            log.info("Judge completed: status={}", response.getStatus());
            
            // 手动确认消息
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (Exception e) {
            log.error("Error processing Kafka message", e);
            // 可以根据业务需求决定是否确认消息
        }
    }
}
