package org.laoli.judge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Description 沙箱配置属性
 * @Author laoli
 * @Date 2025/4/20 15:58
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sandbox")
public class SandboxConfig {

    /**
     * 是否启用沙箱模式
     * true: 使用firejail沙箱执行代码
     * false: 本地测试模式，直接执行命令
     */
    private boolean enabled = true;

    /**
     * 沙箱命令前缀
     * 默认使用firejail
     */
    private String command = "firejail";

    /**
     * 沙箱通用选项
     */
    private CommonOptions commonOptions = new CommonOptions();

    @Data
    public static class CommonOptions {
        private boolean quiet = true;
        private boolean seccomp = true;
        private boolean netNone = true;
        private boolean noGroups = true;
        private boolean noNewPrivs = true;
        private String capsDrop = "all";
    }
}
