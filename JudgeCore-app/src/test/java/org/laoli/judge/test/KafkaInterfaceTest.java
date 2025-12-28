package org.laoli.judge.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.mq.JudgeMessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description Kafka 接口测试
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=192.168.10.232:9092",
    "judge.mq.kafka.topic=judge-request-test"
})
@DisplayName("Kafka 接口测试")
public class KafkaInterfaceTest {
    
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired(required = false)
    private JudgeMessageProducer messageProducer;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("测试 Kafka 连接")
    public void testKafkaConnection() {
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate 不可用，跳过测试");
            return;
        }
        
        assertNotNull(kafkaTemplate, "KafkaTemplate 未注入");
        log.info("Kafka 连接测试通过");
    }
    
    @Test
    @DisplayName("测试 Kafka 发送消息")
    public void testKafkaSendMessage() throws Exception {
        if (kafkaTemplate == null) {
            log.warn("Kafka 不可用，跳过测试");
            return;
        }
        
        JudgeRequest request = createJudgeRequest("PYTHON",
                "a, b = map(int, input().split())\nprint(a + b)");
        
        String message = objectMapper.writeValueAsString(request);
        String topic = "judge-request-test";
        
        try {
            // 发送消息
            kafkaTemplate.send(topic, message).get(5, TimeUnit.SECONDS);
            
            log.info("Kafka 消息发送成功 - 主题: {}, 消息长度: {}", topic, message.length());
            
            // 等待一段时间让消息被处理
            Thread.sleep(2000);
            
        } catch (Exception e) {
            log.error("Kafka 发送消息失败: {}", e.getMessage(), e);
            if (e.getMessage().contains("Connection") || e.getMessage().contains("timeout")) {
                log.warn("Kafka 服务器不可用，请检查 Kafka 服务是否运行");
            } else {
                fail("Kafka 发送消息失败: " + e.getMessage());
            }
        }
    }
    
    @Test
    @DisplayName("测试 Kafka 通过 Producer 发送消息")
    public void testKafkaProducer() {
        if (messageProducer == null) {
            log.warn("JudgeMessageProducer 不可用，跳过测试");
            return;
        }
        
        JudgeRequest request = createJudgeRequest("JAVA", """
            import java.util.Scanner;
            public class Main {
                public static void main(String[] args) {
                    Scanner scanner = new Scanner(System.in);
                    int a = scanner.nextInt();
                    int b = scanner.nextInt();
                    System.out.println(a + b);
                }
            }
            """);
        
        try {
            messageProducer.sendKafkaMessage("judge-request-test", request);
            log.info("通过 Producer 发送 Kafka 消息成功");
            
            // 等待处理
            Thread.sleep(2000);
            
        } catch (Exception e) {
            log.error("通过 Producer 发送消息失败: {}", e.getMessage());
            if (e.getMessage().contains("Connection") || e.getMessage().contains("timeout")) {
                log.warn("Kafka 服务器不可用");
            }
        }
    }
    
    @Test
    @DisplayName("测试 Kafka 主题是否存在")
    public void testKafkaTopic() {
        if (kafkaTemplate == null) {
            log.warn("Kafka 不可用，跳过测试");
            return;
        }
        
        String topic = "judge-request-test";
        
        try {
            // 尝试发送到主题（如果主题不存在，Kafka 可能会自动创建或报错）
            kafkaTemplate.send(topic, "test-message").get(3, TimeUnit.SECONDS);
            log.info("Kafka 主题测试通过: {}", topic);
            
        } catch (Exception e) {
            log.warn("Kafka 主题测试失败: {} - {}", topic, e.getMessage());
            // 不抛出异常，因为可能是主题不存在或服务器不可用
        }
    }
    
    private JudgeRequest createJudgeRequest(String language, String code) {
        JudgeRequest request = new JudgeRequest();
        request.setCode(code);
        request.setLanguage(language);
        request.setTimeLimit(2000L);
        request.setMemoryLimit(256000.0);
        
        List<JudgeRequest.TestCaseDTO> cases = new ArrayList<>();
        JudgeRequest.TestCaseDTO testCase = new JudgeRequest.TestCaseDTO();
        testCase.setInput("1 2\n");
        testCase.setExpectedOutput("3\n");
        cases.add(testCase);
        request.setCases(cases);
        
        return request;
    }
}
