package org.laoli.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @Description Dubbo 启用配置（当注册中心地址不是 "none" 时启用）
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
    name = "dubbo.registry.address",
    havingValue = "none",
    matchIfMissing = true
)
@EnableDubbo(scanBasePackages = "org.laoli.judge.interfaces.dubbo")
public class DubboEnableConfig {
    
    public DubboEnableConfig() {
        log.info("Dubbo is enabled. To disable, set dubbo.registry.address=none");
    }
}
