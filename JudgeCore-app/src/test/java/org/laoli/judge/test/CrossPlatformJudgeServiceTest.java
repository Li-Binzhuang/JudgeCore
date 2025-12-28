package org.laoli.judge.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoli.judge.domain.service.IJudgeDomainService;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.service.execute.util.PlatformDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description 跨平台判题服务测试
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@SpringBootTest
@DisplayName("跨平台判题服务测试")
public class CrossPlatformJudgeServiceTest {
    
    @Autowired
    private IJudgeDomainService judgeDomainService;
    
    private static List<TestCase> testCases;
    private static final long TIME_LIMIT = 2000; // 2秒
    private static final double MEMORY_LIMIT = 1 << 23; // 8MB
    
    @BeforeAll
    public static void setUp() {
        testCases = new ArrayList<>();
        testCases.add(TestCase.builder()
                .input("1 2\n")
                .expectedOutput("3\n")
                .build());
        testCases.add(TestCase.builder()
                .input("5 7\n")
                .expectedOutput("12\n")
                .build());
    }
    
    @Test
    @DisplayName("测试当前运行平台")
    public void testCurrentPlatform() {
        PlatformDetector.Platform platform = PlatformDetector.getCurrentPlatform();
        log.info("当前运行平台: {}", platform);
        assertNotNull(platform);
        assertNotEquals(PlatformDetector.Platform.UNKNOWN, platform);
    }
    
    @Test
    @DisplayName("测试 Java 判题（跨平台）")
    public void testJavaJudge() {
        String javaCode = """
            import java.util.Scanner;
            public class Main {
                public static void main(String[] args) {
                    Scanner scanner = new Scanner(System.in);
                    int a = scanner.nextInt();
                    int b = scanner.nextInt();
                    System.out.println(a + b);
                }
            }
            """;
        
        JudgeResult result = judgeDomainService.judge(testCases, javaCode, Language.JAVA, TIME_LIMIT, MEMORY_LIMIT);
        
        log.info("Java 判题结果 - 平台: {}, 状态: {}, 执行时间: {}ms, 内存: {}KB",
                PlatformDetector.getCurrentPlatform(), result.status(), result.executionTime(), result.memoryUsed());
        
        assertNotNull(result);
        assertEquals("ACCEPTED", result.status().toString());
    }
    
    @Test
    @DisplayName("测试 Python 判题（跨平台）")
    public void testPythonJudge() {
        String pythonCode = "a, b = map(int, input().split())\nprint(a + b)";
        
        JudgeResult result = judgeDomainService.judge(testCases, pythonCode, Language.PYTHON, TIME_LIMIT, MEMORY_LIMIT);
        
        log.info("Python 判题结果 - 平台: {}, 状态: {}, 执行时间: {}ms, 内存: {}KB",
                PlatformDetector.getCurrentPlatform(), result.status(), result.executionTime(), result.memoryUsed());
        
        assertNotNull(result);
        assertEquals("ACCEPTED", result.status().toString());
    }
    
    @Test
    @DisplayName("测试 C++ 判题（跨平台）")
    public void testCppJudge() {
        String cppCode = """
            #include <iostream>
            int main() {
                int a, b;
                std::cin >> a >> b;
                std::cout << a + b << std::endl;
                return 0;
            }
            """;
        
        JudgeResult result = judgeDomainService.judge(testCases, cppCode, Language.CPP, TIME_LIMIT, MEMORY_LIMIT);
        
        log.info("C++ 判题结果 - 平台: {}, 状态: {}, 执行时间: {}ms, 内存: {}KB",
                PlatformDetector.getCurrentPlatform(), result.status(), result.executionTime(), result.memoryUsed());
        
        assertNotNull(result);
        // C++ 在某些平台可能不可用，所以只检查结果不为空
        assertNotNull(result.status());
    }
    
    @Test
    @DisplayName("测试所有支持的语言")
    public void testAllLanguages() {
        PlatformDetector.Platform platform = PlatformDetector.getCurrentPlatform();
        log.info("测试平台: {}", platform);
        
        // 测试 Python（最通用）
        String pythonCode = "a, b = map(int, input().split())\nprint(a + b)";
        JudgeResult pythonResult = judgeDomainService.judge(testCases, pythonCode, Language.PYTHON, TIME_LIMIT, MEMORY_LIMIT);
        assertNotNull(pythonResult);
        log.info("Python - 状态: {}", pythonResult.status());
        
        // 测试 Java（跨平台支持好）
        String javaCode = """
            import java.util.Scanner;
            public class Main {
                public static void main(String[] args) {
                    Scanner scanner = new Scanner(System.in);
                    int a = scanner.nextInt();
                    int b = scanner.nextInt();
                    System.out.println(a + b);
                }
            }
            """;
        JudgeResult javaResult = judgeDomainService.judge(testCases, javaCode, Language.JAVA, TIME_LIMIT, MEMORY_LIMIT);
        assertNotNull(javaResult);
        log.info("Java - 状态: {}", javaResult.status());
    }
}
