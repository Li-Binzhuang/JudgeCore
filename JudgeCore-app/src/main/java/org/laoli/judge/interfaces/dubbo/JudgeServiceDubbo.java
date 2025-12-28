package org.laoli.judge.interfaces.dubbo;

import org.apache.dubbo.config.annotation.DubboService;
import org.laoli.judge.application.JudgeApplicationService;
import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.dto.JudgeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description Dubbo 服务接口实现
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@DubboService(version = "1.0.0", group = "judge", timeout = 30000)
public class JudgeServiceDubbo implements IJudgeServiceDubbo {
    
    private final JudgeApplicationService judgeApplicationService;
    
    public JudgeServiceDubbo(JudgeApplicationService judgeApplicationService) {
        this.judgeApplicationService = judgeApplicationService;
    }
    
    @Override
    public JudgeResponse judge(JudgeRequest request) {
        log.info("Dubbo judge request: language={}, cases={}", request.getLanguage(), request.getCases().size());
        return judgeApplicationService.judge(request);
    }
}
