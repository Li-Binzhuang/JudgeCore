package org.laoli.judge.service.execute;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.service.execute.util.PlatformDetector;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;

/**
 * @Description 语言指令工厂，用于生成每种语言的执行命令（跨平台支持）
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

    // 存储每种语言对应的命令模板（带 Firejail）
    private final Map<Language, CommandTemplate> languageMapWithFirejail;
    // 存储每种语言对应的命令模板（不带 Firejail，跨平台）
    private final Map<Language, CommandTemplate> languageMapPlain;

    public LanguageCommandFactory() {
        // 初始化带 Firejail 的命令映射
        languageMapWithFirejail = new HashMap<>();
        languageMapWithFirejail.put(Language.CPP, this::buildCppCommandWithFirejail);
        languageMapWithFirejail.put(Language.C, this::buildCCommandWithFirejail);
        languageMapWithFirejail.put(Language.RUST, this::buildRustCommandWithFirejail);
        languageMapWithFirejail.put(Language.GO, this::buildGoCommandWithFirejail);
        languageMapWithFirejail.put(Language.JAVA, this::buildJavaCommandWithFirejail);
        languageMapWithFirejail.put(Language.PYTHON, this::buildPythonCommandWithFirejail);
        languageMapWithFirejail.put(Language.PHP, this::buildPhpCommandWithFirejail);
        
        // 初始化不带 Firejail 的命令映射（跨平台）
        languageMapPlain = new HashMap<>();
        languageMapPlain.put(Language.CPP, this::buildCppCommandPlain);
        languageMapPlain.put(Language.C, this::buildCCommandPlain);
        languageMapPlain.put(Language.RUST, this::buildRustCommandPlain);
        languageMapPlain.put(Language.GO, this::buildGoCommandPlain);
        languageMapPlain.put(Language.JAVA, this::buildJavaCommandPlain);
        languageMapPlain.put(Language.PYTHON, this::buildPythonCommandPlain);
        languageMapPlain.put(Language.PHP, this::buildPhpCommandPlain);
    }

    /**
     * 根据语言和工作目录生成命令（自动选择是否使用 Firejail）
     *
     * @param language 语言名称（如 JAVA、PYTHON 等）
     * @param workDir  工作目录路径
     * @return 生成的命令数组
     */
    public String[] getCommand(Language language, Path workDir) {
        return getCommand(language, workDir, PlatformDetector.isFirejailAvailable());
    }
    
    /**
     * 根据语言和工作目录生成命令（指定是否使用 Firejail）
     *
     * @param language 语言名称
     * @param workDir  工作目录路径
     * @param useFirejail 是否使用 Firejail
     * @return 生成的命令数组
     */
    public String[] getCommand(Language language, Path workDir, boolean useFirejail) {
        Map<Language, CommandTemplate> commandMap = useFirejail ? languageMapWithFirejail : languageMapPlain;
        CommandTemplate commandBuilder = commandMap.get(language);
        if (commandBuilder == null) {
            log.warn("Unsupported language: {}", language);
            return new String[0];
        }
        return commandBuilder.build(workDir);
    }

    // 定义命令构建器接口
    @FunctionalInterface
    private interface CommandTemplate {
        String[] build(Path workDir);
    }

    // ========== 带 Firejail 的命令构建方法 ==========
    
    private String[] buildJavaCommandWithFirejail(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.addAll(Arrays.asList("--env=JAVA_TOOL_OPTIONS=\'-Djava.security.manager -Djava.security.policy==<<ALL PERMISSIONS DENIED>>\'",
                "java", "-XX:+PerfDisableSharedMem", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0",
                "-cp", workDir.toString(), "Main"));
        return command.toArray(new String[0]);
    }

    private String[] buildPythonCommandWithFirejail(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.addAll(Arrays.asList("--read-only=/usr/lib", "--env=PYTHONSAFE=1",
                "python3", "-OO", "-u", workDir.resolve("solution.py").toString()));
        return command.toArray(new String[0]);
    }

    private String[] buildCCommandWithFirejail(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.add(workDir.resolve("c_solution").toString());
        return command.toArray(new String[0]);
    }

    private String[] buildCppCommandWithFirejail(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.add(workDir.resolve("cpp_solution").toString());
        return command.toArray(new String[0]);
    }

    private String[] buildRustCommandWithFirejail(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.add(workDir.resolve("rust_solution").toString());
        return command.toArray(new String[0]);
    }

    private String[] buildGoCommandWithFirejail(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.add(workDir.resolve("go_solution").toString());
        return command.toArray(new String[0]);
    }

    private String[] buildPhpCommandWithFirejail(Path workDir) {
        List<String> command = new ArrayList<>();
        command.add(FIREJAIL_CMD);
        command.addAll(SANDBOX_COMMON_OPTIONS);
        command.add("--private=" + workDir.toString());
        command.addAll(Arrays.asList("php", workDir.resolve("solution.php").toString()));
        return command.toArray(new String[0]);
    }

    // ========== 不带 Firejail 的跨平台命令构建方法 ==========
    
    private String[] buildJavaCommandPlain(Path workDir) {
        return new String[]{"java", "-XX:+PerfDisableSharedMem", "-XX:+UseG1GC",
                "-XX:MaxRAMPercentage=75.0", "-cp", workDir.toString(), "Main"};
    }

    private String[] buildPythonCommandPlain(Path workDir) {
        return new String[]{"python3", "-OO", "-u", workDir.resolve("solution.py").toString()};
    }

    private String[] buildCCommandPlain(Path workDir) {
        return new String[]{workDir.resolve("c_solution").toString()};
    }

    private String[] buildCppCommandPlain(Path workDir) {
        return new String[]{workDir.resolve("cpp_solution").toString()};
    }

    private String[] buildRustCommandPlain(Path workDir) {
        return new String[]{workDir.resolve("rust_solution").toString()};
    }

    private String[] buildGoCommandPlain(Path workDir) {
        return new String[]{workDir.resolve("go_solution").toString()};
    }

    private String[] buildPhpCommandPlain(Path workDir) {
        return new String[]{"php", workDir.resolve("solution.php").toString()};
    }
}