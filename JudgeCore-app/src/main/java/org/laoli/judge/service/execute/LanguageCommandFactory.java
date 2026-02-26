package org.laoli.judge.service.execute;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.config.SandboxConfig;
import org.laoli.judge.model.enums.Language;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;

/**
 * @Description 语言指令工厂，用于生成每种语言的执行命令
 * @Author laoli
 * @Date 2025/4/20 15:58
 */
@Component
@Slf4j
public class LanguageCommandFactory {

    @Resource
    private SandboxConfig sandboxConfig;

    private static final List<String> SANDBOX_COMMON_OPTIONS_DISABLED = Collections.emptyList();

    private final Map<Language, CommandTemplate> languageMap;

    public LanguageCommandFactory() {
        languageMap = new HashMap<>();
        languageMap.put(Language.CPP, this::buildCppCommand);
        languageMap.put(Language.C, this::buildCCommand);
        languageMap.put(Language.RUST, this::buildRustCommand);
        languageMap.put(Language.GO, this::buildGoCommand);
        languageMap.put(Language.JAVA, this::buildJavaCommand);
        languageMap.put(Language.PYTHON, this::buildPythonCommand);
        languageMap.put(Language.PHP, this::buildPhpCommand);
    }

    /**
     * 根据语言和工作目录生成命令
     *
     * @param language 语言名称（如 JAVA、PYTHON 等）
     * @param workDir  工作目录路径
     * @return 生成的命令数组
     */
    public String[] getCommand(Language language, Path workDir) {
        CommandTemplate commandBuilder = languageMap.get(language);
        if (commandBuilder == null) {
            log.info("Unsupported language: {}", language);
            return new String[0];
        }
        return commandBuilder.build(workDir);
    }

    /**
     * 判断当前是否启用沙箱模式
     *
     * @return true 表示启用沙箱模式，false 表示本地测试模式
     */
    public boolean isSandboxEnabled() {
        return sandboxConfig.isEnabled();
    }

    @FunctionalInterface
    private interface CommandTemplate {
        String[] build(Path workDir);
    }

    /**
     * 获取沙箱命令前缀
     *
     * @return 如果启用沙箱则返回配置的沙箱命令，否则返回空列表
     */
    private List<String> getSandboxCommandPrefix() {
        if (sandboxConfig.isEnabled()) {
            List<String> prefix = new ArrayList<>();
            prefix.add(sandboxConfig.getCommand());
            prefix.addAll(getSandboxOptions());
            return prefix;
        }
        return Collections.emptyList();
    }

    /**
     * 获取沙箱选项列表
     *
     * @return 沙箱选项列表
     */
    private List<String> getSandboxOptions() {
        if (!sandboxConfig.isEnabled()) {
            return SANDBOX_COMMON_OPTIONS_DISABLED;
        }

        SandboxConfig.CommonOptions opts = sandboxConfig.getCommonOptions();
        List<String> options = new ArrayList<>();

        if (opts.isQuiet())
            options.add("--quiet");
        if (opts.isSeccomp())
            options.add("--seccomp");
        if (opts.isNetNone())
            options.add("--net=none");
        if (opts.isNoGroups())
            options.add("--nogroups");
        if (opts.isNoNewPrivs())
            options.add("--nonewprivs");
        if (opts.getCapsDrop() != null && !opts.getCapsDrop().isEmpty()) {
            options.add("--caps.drop=" + opts.getCapsDrop());
        }

        return options;
    }

    private String[] buildJavaCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.addAll(getSandboxCommandPrefix());
        if (sandboxConfig.isEnabled()) {
            command.add("--private=" + workDir.toString());
            command.addAll(Arrays.asList(
                    "--env=JAVA_TOOL_OPTIONS='-Djava.security.manager -Djava.security.policy==<<ALL PERMISSIONS DENIED>>'",
                    "java", "-XX:+PerfDisableSharedMem", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-cp",
                    workDir.toString(), "Main"));
        } else {
            command.addAll(Arrays.asList("java", "-XX:+PerfDisableSharedMem", "-XX:+UseG1GC",
                    "-XX:MaxRAMPercentage=75.0", "-cp", workDir.toString(), "Main"));
        }
        return command.toArray(new String[0]);
    }

    private String[] buildPythonCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.addAll(getSandboxCommandPrefix());
        if (sandboxConfig.isEnabled()) {
            command.add("--private=" + workDir.toString());
            command.addAll(Arrays.asList("--read-only=/usr/lib", "--env=PYTHONSAFE=1", "python3", "-OO", "-u",
                    workDir.resolve("solution.py").toString()));
        } else {
            command.addAll(Arrays.asList("python3", "-OO", "-u", workDir.resolve("solution.py").toString()));
        }
        return command.toArray(new String[0]);
    }

    private String[] buildCCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.addAll(getSandboxCommandPrefix());
        if (sandboxConfig.isEnabled()) {
            command.add("--private=" + workDir.toString());
        }
        command.add(workDir.resolve("c_solution").toString());
        return command.toArray(new String[0]);
    }

    private String[] buildCppCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.addAll(getSandboxCommandPrefix());
        if (sandboxConfig.isEnabled()) {
            command.add("--private=" + workDir.toString());
        }
        command.add(workDir.resolve("cpp_solution").toString());
        return command.toArray(new String[0]);
    }

    private String[] buildRustCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.addAll(getSandboxCommandPrefix());
        if (sandboxConfig.isEnabled()) {
            command.add("--private=" + workDir.toString());
        }
        command.add(workDir.resolve("rust_solution").toString());
        return command.toArray(new String[0]);
    }

    private String[] buildGoCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.addAll(getSandboxCommandPrefix());
        if (sandboxConfig.isEnabled()) {
            command.add("--private=" + workDir.toString());
        }
        command.add(workDir.resolve("go_solution").toString());
        return command.toArray(new String[0]);
    }

    private String[] buildPhpCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.addAll(getSandboxCommandPrefix());
        if (sandboxConfig.isEnabled()) {
            command.add("--private=" + workDir.toString());
        }
        command.addAll(Arrays.asList("php", workDir.resolve("solution.php").toString()));
        return command.toArray(new String[0]);
    }
}
