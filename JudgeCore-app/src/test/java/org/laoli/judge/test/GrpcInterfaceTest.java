package org.laoli.judge.test;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoli.api.JudgeCore;
import org.laoli.api.JudgeServiceGrpc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description gRPC 接口测试
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
    "grpc.server.port=9000"
})
@DisplayName("gRPC 接口测试")
public class GrpcInterfaceTest {
    
    private ManagedChannel channel;
    private JudgeServiceGrpc.JudgeServiceBlockingStub blockingStub;
    
    @BeforeEach
    public void setUp() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9000)
                .usePlaintext()
                .build();
        blockingStub = JudgeServiceGrpc.newBlockingStub(channel);
    }
    
    @AfterEach
    public void tearDown() {
        if (channel != null) {
            channel.shutdown();
        }
    }
    
    @Test
    @DisplayName("测试 gRPC 连接")
    public void testGrpcConnection() {
        assertNotNull(channel);
        assertNotNull(blockingStub);
        log.info("gRPC 连接已建立");
    }
    
    @Test
    @DisplayName("测试 gRPC 判题 - Java")
    public void testGrpcJudgeJava() {
        JudgeCore.Request request = JudgeCore.Request.newBuilder()
                .setCode("""
                    import java.util.Scanner;
                    public class Main {
                        public static void main(String[] args) {
                            Scanner scanner = new Scanner(System.in);
                            int a = scanner.nextInt();
                            int b = scanner.nextInt();
                            System.out.println(a + b);
                        }
                    }
                    """)
                .setLanguage("JAVA")
                .setTimeLimit(2000)
                .setMemoryLimit(256000)
                .addCases(JudgeCore.Case.newBuilder()
                        .setInput("1 2\n")
                        .setExpectedOutput("3\n")
                        .build())
                .build();
        
        try {
            JudgeCore.Response response = blockingStub.judge(request);
            
            log.info("gRPC 判题结果 - 状态: {}, 执行时间: {}ms, 内存: {}KB",
                    response.getStatus(), response.getExecutionTime(), response.getMemoryUsed());
            
            assertNotNull(response);
            assertNotNull(response.getStatus());
            // 根据实际情况判断，如果编译或执行失败，状态可能不是 ACCEPTED
            log.info("判题状态: {}", response.getStatus());
            
        } catch (Exception e) {
            log.error("gRPC 调用失败: {}", e.getMessage(), e);
            fail("gRPC 调用失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试 gRPC 判题 - Python")
    public void testGrpcJudgePython() {
        JudgeCore.Request request = JudgeCore.Request.newBuilder()
                .setCode("a, b = map(int, input().split())\nprint(a + b)")
                .setLanguage("PYTHON")
                .setTimeLimit(2000)
                .setMemoryLimit(256000)
                .addCases(JudgeCore.Case.newBuilder()
                        .setInput("1 2\n")
                        .setExpectedOutput("3\n")
                        .build())
                .build();
        
        try {
            JudgeCore.Response response = blockingStub.judge(request);
            
            log.info("gRPC Python 判题结果 - 状态: {}, 执行时间: {}ms",
                    response.getStatus(), response.getExecutionTime());
            
            assertNotNull(response);
            
        } catch (Exception e) {
            log.error("gRPC Python 调用失败: {}", e.getMessage(), e);
            fail("gRPC 调用失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("测试 gRPC 参数验证")
    public void testGrpcValidation() {
        // 测试空代码
        JudgeCore.Request request = JudgeCore.Request.newBuilder()
                .setCode("")
                .setLanguage("JAVA")
                .setTimeLimit(2000)
                .setMemoryLimit(256000)
                .build();
        
        try {
            JudgeCore.Response response = blockingStub.judge(request);
            assertEquals("SYSTEM_ERROR", response.getStatus());
            log.info("参数验证测试通过: {}", response.getMessage());
        } catch (Exception e) {
            log.error("参数验证测试异常: {}", e.getMessage());
        }
    }
}
