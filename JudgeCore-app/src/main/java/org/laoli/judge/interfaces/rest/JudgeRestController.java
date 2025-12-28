package org.laoli.judge.interfaces.rest;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.application.JudgeApplicationService;
import org.laoli.judge.interfaces.dto.ApiResponse;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.dto.JudgeResponse;
import org.springframework.web.bind.annotation.*;

/**
 * @Description HTTP REST API 控制器
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@RestController
@RequestMapping("/api/judge")
public class JudgeRestController {
    
    private final JudgeApplicationService judgeApplicationService;
    
    public JudgeRestController(JudgeApplicationService judgeApplicationService) {
        this.judgeApplicationService = judgeApplicationService;
    }
    
    /**
     * 判题接口
     */
    @PostMapping("/submit")
    public ApiResponse<JudgeResponse> judge(@Valid @RequestBody JudgeRequest request) {
        log.info("Received judge request: language={}, cases={}", request.getLanguage(), request.getCases().size());
        JudgeResponse response = judgeApplicationService.judge(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("OK");
    }
}
