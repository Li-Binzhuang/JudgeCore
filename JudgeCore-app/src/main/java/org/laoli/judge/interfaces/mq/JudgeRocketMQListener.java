package org.laoli.judge.interfaces.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.laoli.judge.application.JudgeApplicationService;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.dto.JudgeResponse;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description RocketMQ 消息监听器
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = "${judge.mq.rocketmq.topic:judge-request}",
    consumerGroup = "${judge.mq.rocketmq.consumer-group:judge-group}"
)
public class JudgeRocketMQListener implements RocketMQListener<String> {
    
    private final JudgeApplicationService judgeApplicationService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public JudgeRocketMQListener(JudgeApplicationService judgeApplicationService, ObjectMapper objectMapper) {
        this.judgeApplicationService = judgeApplicationService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void onMessage(String message) {
        try {
            log.info("Received RocketMQ message: {}", message);
            JudgeRequest request = objectMapper.readValue(message, JudgeRequest.class);
            JudgeResponse response = judgeApplicationService.judge(request);
            
            // 处理响应（可以发送到响应队列）
            log.info("Judge completed: status={}", response.getStatus());
        } catch (Exception e) {
            log.error("Error processing RocketMQ message", e);
            // RocketMQ 会自动重试，也可以手动处理异常
            throw new RuntimeException("Failed to process message", e);
        }
    }
}
