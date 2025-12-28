package org.laoli.judge.service.execute;

import lombok.extern.slf4j.Slf4j;
import org.laoli.config.ExecutorConfigProperties;
import org.laoli.judge.service.execute.impl.CrossPlatformExecutor;
import org.laoli.judge.service.execute.impl.FirejailExecutor;
import org.laoli.judge.service.execute.util.PlatformDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description 执行器工厂，根据平台和配置选择合适的执行器
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Component
public class ExecutorFactory {
    
    @Autowired(required = false)
    private FirejailExecutor firejailExecutor;
    
    @Autowired
    private CrossPlatformExecutor crossPlatformExecutor;
    
    @Autowired
    private ExecutorConfigProperties executorConfig;
    
    /**
     * 获取合适的执行器
     * @param preferFirejail 是否优先使用 Firejail（如果可用）
     * @return 执行器实例
     */
    public CodeExecutor getExecutor(boolean preferFirejail) {
        // 如果强制使用跨平台执行器
        if (executorConfig.isForceCrossPlatform()) {
            log.info("Using cross-platform executor (forced by configuration)");
            return crossPlatformExecutor;
        }
        
        // 如果优先使用 Firejail 且可用
        if (preferFirejail && !executorConfig.isForceCrossPlatform() 
                && PlatformDetector.isFirejailAvailable() && firejailExecutor != null) {
            log.info("Using Firejail executor (Linux with Firejail available)");
            return firejailExecutor;
        }
        
        // 否则使用跨平台执行器
        log.info("Using cross-platform executor (Platform: {})", PlatformDetector.getCurrentPlatform());
        return crossPlatformExecutor;
    }
    
    /**
     * 获取默认执行器（根据配置自动选择）
     */
    public CodeExecutor getDefaultExecutor() {
        return getExecutor(executorConfig.isPreferFirejail());
    }
}
