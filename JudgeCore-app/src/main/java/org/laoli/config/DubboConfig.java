package org.laoli.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;

/**
 * @Description Dubbo 配置类（条件启用）
 * 只有当注册中心地址不是 "none" 时才启用 Dubbo
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Configuration
@ConditionalOnExpression("'${dubbo.registry.address:nacos://192.168.10.232:8848}' != 'none'")
@EnableDubbo(scanBasePackages = "org.laoli.judge.interfaces.dubbo")
public class DubboConfig {
    
    public DubboConfig() {
        log.info("Dubbo is enabled. To disable, set dubbo.registry.address=none");
    }
}
