package org.laoli.judge.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoli.judge.interfaces.dto.ApiResponse;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.dto.JudgeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @Description HTTP REST API 接口测试
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("HTTP REST API 接口测试")
public class HttpInterfaceTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("测试健康检查端点")
    public void testHealthEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/judge/health"))
                .andExpect(status().isOk())
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        log.info("健康检查响应: {}", content);
        
        ApiResponse<?> response = objectMapper.readValue(content, ApiResponse.class);
        assertEquals(200, response.getCode());
        assertEquals("OK", response.getData());
    }
    
    @Test
    @DisplayName("测试判题接口 - Java")
    public void testJudgeEndpointJava() throws Exception {
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
        
        MvcResult result = mockMvc.perform(post("/api/judge/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        log.info("判题响应: {}", content);
        
        ApiResponse<JudgeResponse> response = objectMapper.readValue(
                content,
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, JudgeResponse.class)
        );
        
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("ACCEPTED", response.getData().getStatus());
    }
    
    @Test
    @DisplayName("测试判题接口 - Python")
    public void testJudgeEndpointPython() throws Exception {
        JudgeRequest request = createJudgeRequest("PYTHON", 
                "a, b = map(int, input().split())\nprint(a + b)");
        
        MvcResult result = mockMvc.perform(post("/api/judge/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        ApiResponse<JudgeResponse> response = objectMapper.readValue(
                content,
                objectMapper.getTypeFactory().constructParametricType(
                        ApiResponse.class,
                        JudgeResponse.class
                )
        );
        
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        log.info("Python 判题结果: {}", response.getData().getStatus());
    }
    
    @Test
    @DisplayName("测试参数验证")
    public void testValidation() throws Exception {
        JudgeRequest request = new JudgeRequest();
        // 缺少必填字段
        
        MvcResult result = mockMvc.perform(post("/api/judge/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        log.info("验证失败响应: {}", result.getResponse().getContentAsString());
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
