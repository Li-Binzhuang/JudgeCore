package org.laoli.judge.interfaces.dubbo;

import org.laoli.judge.interfaces.dto.JudgeRequest;
import org.laoli.judge.interfaces.dto.JudgeResponse;

/**
 * @Description Dubbo 服务接口定义
 * @Author laoli
 * @Date 2025/4/21
 */
public interface IJudgeServiceDubbo {
    
    /**
     * 判题方法
     * @param request 判题请求
     * @return 判题响应
     */
    JudgeResponse judge(JudgeRequest request);
}
