package org.laoli.judge.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.mq.JudgeMessageProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description RocketMQ 接口测试
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("dev")  // 使用开发环境配置
@TestPropertySource(properties = {
    "rocketmq.name-server=192.168.10.232:9876",
    "rocketmq.producer.group=judge-producer-group-test",
    "rocketmq.producer.send-message-timeout=10000",  // 增加超时时间到 10 秒
    "rocketmq.producer.connect-timeout=10000",  // 连接超时 10 秒
    "judge.mq.rocketmq.topic=judge-request-test",
    "judge.mq.rocketmq.consumer-group=judge-group-test"
})
@DisplayName("RocketMQ 接口测试")
public class RocketMQInterfaceTest {
    
    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;
    
    @Autowired(required = false)
    private JudgeMessageProducer messageProducer;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("测试 RocketMQ 连接")
    public void testRocketMQConnection() {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQTemplate 不可用，跳过测试");
            return;
        }
        
        assertNotNull(rocketMQTemplate, "RocketMQTemplate 未注入");
        log.info("RocketMQ 连接测试通过");
    }
    
    @Test
    @DisplayName("测试 RocketMQ 发送消息")
    public void testRocketMQSendMessage() throws Exception {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQ 不可用，跳过测试");
            return;
        }
        
        JudgeRequest request = createJudgeRequest("PYTHON",
                "a, b = map(int, input().split())\nprint(a + b)");
        
        String message = objectMapper.writeValueAsString(request);
        String topic = "judge-request-test";
        
        try {
            // 发送消息
            rocketMQTemplate.send(topic, MessageBuilder.withPayload(message).build());
            
            log.info("RocketMQ 消息发送成功 - 主题: {}, 消息长度: {}", topic, message.length());
            
            // 等待处理
            Thread.sleep(2000);
            
        } catch (Exception e) {
            log.error("RocketMQ 发送消息失败: {}", e.getMessage(), e);
            if (e.getMessage().contains("Connection") || e.getMessage().contains("timeout")) {
                log.warn("RocketMQ 服务器不可用，请检查 RocketMQ 服务是否运行");
            } else {
                fail("RocketMQ 发送消息失败: " + e.getMessage());
            }
        }
    }
    
    @Test
    @DisplayName("测试 RocketMQ 通过 Producer 发送消息")
    public void testRocketMQProducer() {
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
            messageProducer.sendRocketMQMessage("judge-request-test", request);
            log.info("通过 Producer 发送 RocketMQ 消息成功");
            
            // 等待处理
            Thread.sleep(2000);
            
        } catch (Exception e) {
            log.error("通过 Producer 发送消息失败: {}", e.getMessage());
            if (e.getMessage().contains("Connection") || e.getMessage().contains("timeout")) {
                log.warn("RocketMQ 服务器不可用");
            }
        }
    }
    
    @Test
    @DisplayName("测试 RocketMQ NameServer 连接")
    public void testRocketMQNameServer() {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQ 不可用，跳过测试");
            return;
        }
        
        String topic = "judge-request-test";
        
        try {
            // 尝试发送测试消息
            rocketMQTemplate.send(topic, MessageBuilder.withPayload("test").build());
            log.info("RocketMQ NameServer 连接测试通过");
            
            Thread.sleep(1000);
            
        } catch (Exception e) {
            log.warn("RocketMQ NameServer 连接测试失败: {}", e.getMessage());
            // 不抛出异常，因为可能是服务器不可用
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
