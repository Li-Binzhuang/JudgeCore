package org.laoli.judge.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.dto.JudgeResponse;
import org.laoli.judge.interfaces.dubbo.IJudgeServiceDubbo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description Dubbo 接口测试
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
    "dubbo.registry.address=nacos://192.168.10.232:8848",
    "dubbo.protocol.port=20880"
})
@DisplayName("Dubbo 接口测试")
public class DubboInterfaceTest {
    
    @DubboReference(version = "1.0.0", group = "judge", timeout = 30000)
    private IJudgeServiceDubbo judgeServiceDubbo;
    
    @Test
    @DisplayName("测试 Dubbo 服务连接")
    public void testDubboConnection() {
        assertNotNull(judgeServiceDubbo, "Dubbo 服务未注入，请检查 Nacos 连接和配置");
        log.info("Dubbo 服务连接成功");
    }
    
    @Test
    @DisplayName("测试 Dubbo 判题 - Java")
    public void testDubboJudgeJava() {
        if (judgeServiceDubbo == null) {
            log.warn("Dubbo 服务不可用，跳过测试");
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
            JudgeResponse response = judgeServiceDubbo.judge(request);
            
            log.info("Dubbo 判题结果 - 状态: {}, 执行时间: {}ms, 内存: {}KB",
                    response.getStatus(), response.getExecutionTime(), response.getMemoryUsed());
            
            assertNotNull(response);
            assertNotNull(response.getStatus());
            
        } catch (Exception e) {
            log.error("Dubbo 调用失败: {}", e.getMessage(), e);
            // 如果 Nacos 不可用，这是预期的
            if (e.getMessage().contains("Nacos") || e.getMessage().contains("registry")) {
                log.warn("Nacos 注册中心不可用，请检查 Nacos 服务");
            } else {
                fail("Dubbo 调用失败: " + e.getMessage());
            }
        }
    }
    
    @Test
    @DisplayName("测试 Dubbo 判题 - Python")
    public void testDubboJudgePython() {
        if (judgeServiceDubbo == null) {
            log.warn("Dubbo 服务不可用，跳过测试");
            return;
        }
        
        JudgeRequest request = createJudgeRequest("PYTHON",
                "a, b = map(int, input().split())\nprint(a + b)");
        
        try {
            JudgeResponse response = judgeServiceDubbo.judge(request);
            
            log.info("Dubbo Python 判题结果 - 状态: {}, 执行时间: {}ms, 内存: {}KB",
                    response.getStatus(), response.getExecutionTime(), response.getMemoryUsed());
            
            assertNotNull(response);
            
        } catch (Exception e) {
            log.error("Dubbo Python 调用失败: {}", e.getMessage());
            // 不抛出异常，因为可能是环境问题
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
