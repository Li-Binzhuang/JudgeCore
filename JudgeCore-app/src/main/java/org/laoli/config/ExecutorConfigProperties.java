package org.laoli.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description 执行器配置属性
 * @Author laoli
 * @Date 2025/4/21
 */
@Data
@Component
@ConfigurationProperties(prefix = "judge.executor")
public class ExecutorConfigProperties {
    /**
     * 是否优先使用 Firejail（如果可用）
     * 默认值：true
     */
    private boolean preferFirejail = true;
    
    /**
     * 是否强制使用跨平台执行器（忽略 Firejail）
     * 默认值：false
     */
    private boolean forceCrossPlatform = false;
}
