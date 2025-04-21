package org.laoli.judge.service.execute;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
            "--quiet", "--seccomp", "--net=none" , "--nogroups", "--nonewprivs", "--caps.drop=all"
    );

    // 存储每种语言对应的命令模板
    private final Map<Language, CommandTemplate> languageMap;

    public LanguageCommandFactory() {
        languageMap = new HashMap<>();
        languageMap.put(Language.CPP, this::buildCOrCppCommand);
        languageMap.put(Language.C, this::buildCOrCppCommand);
        languageMap.put(Language.RUST, this::buildCOrCppCommand);
        languageMap.put(Language.GO, this::buildCOrCppCommand);
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
    public String[] getCommand(@NotNull Language language, @NotNull Path workDir) {
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
        //todo
        command.addAll(Arrays.asList("java" ,"-XX:+PerfDisableSharedMem","-XX:+UseG1GC","-XX:MaxRAMPercentage=75.0" ,"-cp", workDir.toString(), "Main"));
        return command.toArray(new String[0]);
    }

    // 构建 Python 命令
    private String[] buildPythonCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.addAll(Arrays.asList("python3","-OO", "-u", workDir.resolve("solution.py").toString()));
        return command.toArray(new String[0]);
    }

    // 构建 C/C++/Rust 命令
    private String[] buildCOrCppCommand(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.add(workDir.resolve("solution").toString());
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