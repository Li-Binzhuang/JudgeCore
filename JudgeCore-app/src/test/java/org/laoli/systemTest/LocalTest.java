package org.laoli.systemTest;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.service.IJudgeService;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author laoli
 * @Date 2025/4/20 19:48
 */


@Slf4j
@SpringBootTest
public class LocalTest {
    @Resource
    private IJudgeService judgeService;
    static List<TestCase> testCases;

    long timeLimit=2000;
    double memoryLimit=1<<23;

    @BeforeAll
    public static void TestCases(){
        // 创建测试用例
        testCases = new ArrayList<>();
        testCases.add(new TestCase("1 2\n", "3\n"));
        testCases.add(new TestCase("5 7\n", "12\n"));
        testCases.add(new TestCase("1000 1000\n", "2000\n"));
        testCases.add(new TestCase("1000000 1000000\n", "2000000\n"));
    }

    @Test
    public void testJava(){
        // Java示例代码 - 正确答案
        String javaCode =
            "import java.util.Scanner;\n" +
            "public class Main {\n" +
            "    public static void main(String[] args) {\n" +
            "        Scanner scanner = new Scanner(System.in);\n" +
            "        int a = scanner.nextInt();\n" +
            "        int b = scanner.nextInt();\n" +
            "        //throw new RuntimeException(\"test\"); \n"+
            "        System.out.println(a + b);\n" +
            "    }\n" +
            "}";
        JudgeResult judge = judgeService.judge(testCases, javaCode, Language.JAVA,timeLimit,memoryLimit);
        log.info("JavaJudgeResult - Status: {}, Message: {}, Execution Time: {} ms, Memory Used: {} KB, Case Results: {}",
                 judge.status(), judge.message(), judge.executionTime(), judge.memoryUsed(), judge.caseResults());
    }
    @Test
    public void testCpp(){
        // C++示例代码 - 正确答案
        String cppCode =
            "#include <iostream>\n" +
            "int main() {\n" +
            "    int a, b;\n" +
            "    std::cin >> a >> b;\n" +
            "    std::cout << a + b << std::endl;\n" +
            "    return 0;\n" +
            "}";
        JudgeResult judge = judgeService.judge(testCases, cppCode, Language.CPP,timeLimit,memoryLimit);
        log.info("CppJudgeResult - Status: {}, Message: {}, Execution Time: {} ms, Memory Used: {} KB, Case Results: {}",
                 judge.status(), judge.message(), judge.executionTime(), judge.memoryUsed(), judge.caseResults());

    }
    @Test
    public void testPython(){
        // Python示例代码 - 正确答案
        String pythonCode =
            "a, b = map(int, input().split())\n" +
            "print(a + b)";
        JudgeResult judge = judgeService.judge(testCases, pythonCode, Language.PYTHON,timeLimit,memoryLimit);
        log.info("PythonJudgeResult - Status: {}, Message: {}, Execution Time: {} ms, Memory Used: {} KB, Case Results: {}",
                 judge.status(), judge.message(), judge.executionTime(), judge.memoryUsed(), judge.caseResults());
    }

    @Test
    public void testC(){
        // C示例代码 - 正确答案
        String cCode =
                """
                #include <stdio.h>
                int main() {
                    int a, b;
                    scanf("%d %d", &a, &b);
                    printf("%d\\n", a + b);
                    return 0;
                }
                """;
        JudgeResult judge = judgeService.judge(testCases, cCode, Language.C,timeLimit,memoryLimit);
        log.info("CJudgeResult - Status: {}, Message: {}, Execution Time: {} ms, Memory Used: {} KB, Case Results: {}",
                 judge.status(), judge.message(), judge.executionTime(), judge.memoryUsed(), judge.caseResults());
    }
    @Test
    public void testGo(){
        // Go示例代码 - 正确答案
        String goCode = """
                package main
                import "fmt"
                func main() {
                    var a, b int
                    fmt.Scan(&a, &b)
                    fmt.Println(a + b)
                }
                """;
        JudgeResult judge = judgeService.judge(testCases, goCode, Language.GO,timeLimit,memoryLimit);
        log.info("GoJudgeResult - Status: {}, Message: {}, Execution Time: {} ms, Memory Used: {} KB, Case Results: {}",
                 judge.status(), judge.message(), judge.executionTime(), judge.memoryUsed(), judge.caseResults());
    }

    @Test
    public void testPhp(){
        // PHP示例代码 - 正确答案
        String phpCode =
            "<?php\n" +
            "fscanf(STDIN, \"%d %d\", $a, $b);\n" +
            "echo $a + $b;\n";

        JudgeResult judge = judgeService.judge(testCases, phpCode, Language.PHP,timeLimit,memoryLimit);
        log.info("PhpJudgeResult - Status: {}, Message: {}, Execution Time: {} ms, Memory Used: {} KB, Case Results: {}",
                 judge.status(), judge.message(), judge.executionTime(), judge.memoryUsed(), judge.caseResults());
    }
    @Test
    public void testRust(){
        // Rust示例代码 - 正确答案
        String rustCode ="""
            use std::io;
            fn main() {
                let mut input = String::new();
                io::stdin().read_line(&mut input).expect("读取输入失败");
                // 解析输入
                let numbers: Vec<i32> = input
                  .split_whitespace()
                 .map(|s| s.parse().expect("解析失败"))
                 .collect();
                // 确保输入有两个数字
                if numbers.len() != 2 {
                    println!("输入错误");
                    return;
                }
                // 输出结果
                println!("{}", numbers[0] + numbers[1]);
            }
            """;
        JudgeResult judge = judgeService.judge(testCases, rustCode, Language.RUST,timeLimit,memoryLimit);
        log.info("RustJudgeResult - Status: {}, Message: {}, Execution Time: {} ms, Memory Used: {} KB, Case Results: {}",
                 judge.status(), judge.message(), judge.executionTime(), judge.memoryUsed(), judge.caseResults());
    }
}
