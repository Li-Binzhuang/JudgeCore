package org.laoli.judge.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.interfaces.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description Kafka 健康检查控制器
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class KafkaHealthController {
    
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${spring.kafka.bootstrap-servers:}")
    private String bootstrapServers;
    
    @Value("${judge.mq.kafka.topic:judge-request}")
    private String testTopic;
    
    /**
     * 检查 Kafka 连接状态
     */
    @GetMapping("/kafka")
    public ApiResponse<Map<String, Object>> checkKafka() {
        Map<String, Object> result = new HashMap<>();
        result.put("bootstrapServers", bootstrapServers);
        result.put("enabled", bootstrapServers != null && !bootstrapServers.equals("none"));
        
        if (bootstrapServers == null || bootstrapServers.equals("none")) {
            result.put("status", "disabled");
            result.put("message", "Kafka is disabled (bootstrap-servers=none)");
            return ApiResponse.success(result);
        }
        
        if (kafkaTemplate == null) {
            result.put("status", "not_configured");
            result.put("message", "KafkaTemplate is not available");
            return ApiResponse.success(result);
        }
        
        try {
            // 尝试发送一条测试消息
            String testMessage = "health-check-" + System.currentTimeMillis();
            kafkaTemplate.send(testTopic, testMessage).get(3, TimeUnit.SECONDS);
            
            result.put("status", "connected");
            result.put("message", "Kafka connection successful");
            result.put("testTopic", testTopic);
            result.put("testMessage", testMessage);
            log.info("Kafka health check passed: {}", bootstrapServers);
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Kafka connection failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            log.error("Kafka health check failed: {}", e.getMessage());
        }
        
        return ApiResponse.success(result);
    }
    
    /**
     * 检查所有中间件状态
     */
    @GetMapping("/middleware")
    public ApiResponse<Map<String, Object>> checkMiddleware() {
        Map<String, Object> result = new HashMap<>();
        
        // Kafka 状态
        Map<String, Object> kafkaStatus = new HashMap<>();
        kafkaStatus.put("bootstrapServers", bootstrapServers);
        kafkaStatus.put("enabled", bootstrapServers != null && !bootstrapServers.equals("none"));
        
        if (kafkaTemplate != null && bootstrapServers != null && !bootstrapServers.equals("none")) {
            try {
                kafkaTemplate.send(testTopic, "health-check").get(2, TimeUnit.SECONDS);
                kafkaStatus.put("status", "connected");
            } catch (Exception e) {
                kafkaStatus.put("status", "error");
                kafkaStatus.put("error", e.getMessage());
            }
        } else {
            kafkaStatus.put("status", bootstrapServers == null || bootstrapServers.equals("none") ? "disabled" : "not_configured");
        }
        
        result.put("kafka", kafkaStatus);
        
        return ApiResponse.success(result);
    }
}
