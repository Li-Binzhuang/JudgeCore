package org.laoli.judge.service.execute;

import lombok.extern.slf4j.Slf4j;
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
    // 沙盒执行器命令前缀，用于限制进程的资源使用和权限
    private static final String FIREJAIL_CMD = "firejail";
    private static final List<String> SANDBOX_COMMON_OPTIONS = Arrays.asList(
            "--quiet", "--seccomp", "--net=none" , "--nogroups",
            "--nonewprivs", "--caps.drop=all"
    );

    // 存储每种语言对应的命令模板
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
        languageMap.put(Language.KOTLIN, this::buildKotlinCommand);
    }

    /**
     * 根据语言和工作目录生成命令
     *
     * @param language 语言名称（如 JAVA、PYTHON 等）
     * @param workDir  工作目录路径
     * @return 生成的命令数组
     */
    public String[] getCommand( Language language, Path workDir) {
        CommandTemplate commandBuilder = languageMap.get(language);
        if (commandBuilder == null) {
            log.info("Unsupported language: {}", language);
        }
        return commandBuilder.build(workDir);
    }

    // 定义命令构建器接口
    @FunctionalInterface
    private interface CommandTemplate {
        String[] build(Path workDir);
    }

    // 构建 Java 命令
    private String[] buildJavaCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.addAll(Arrays.asList("--env=JAVA_TOOL_OPTIONS=\'-Djava.security.manager -Djava.security.policy==<<ALL PERMISSIONS DENIED>>\'","java","-XX:+PerfDisableSharedMem","-XX:+UseG1GC","-XX:MaxRAMPercentage=75.0" ,"-cp", workDir.toString(), "Main"));
        return command.toArray(new String[0]);
    }

    // 构建 Python 命令
    private String[] buildPythonCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.addAll(Arrays.asList("--read-only=/usr/lib","--env=PYTHONSAFE=1","python3","-OO", "-u", workDir.resolve("solution.py").toString()));
        return command.toArray(new String[0]);
    }
        // 构建 C 命令
    private String[] buildCCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.add(workDir.resolve("c_solution").toString());
        return command.toArray(new String[0]);
    }
        // 构建 C++ 命令
    private String[] buildCppCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.add(workDir.resolve("cpp_solution").toString());
        return command.toArray(new String[0]);
    }
    // 构建 Rust 命令
    private String[] buildRustCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.add(workDir.resolve("rust_solution").toString());
        return command.toArray(new String[0]);
    }


    // 构建 Go 命令
    private String[] buildGoCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.add(workDir.resolve("go_solution").toString());
        return command.toArray(new String[0]);
    }

    // 构建 PHP 命令
    private String[] buildPhpCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.addAll(Arrays.asList("php", workDir.resolve("solution.php").toString()));
        return command.toArray(new String[0]);
    }

    // 构建 Kotlin 命令
    private String[] buildKotlinCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.addAll(Arrays.asList("java",
                "-XX:+PerfDisableSharedMem",
                "-jar", workDir.resolve("Main.jar").toString()));
        return command.toArray(new String[0]);
    }
}