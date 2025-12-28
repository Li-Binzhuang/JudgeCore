package org.laoli.judge.service.execute.impl;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.entity.TestCase;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.service.execute.util.PlatformDetector;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * @Description 跨平台执行器，根据平台选择合适的资源限制方式
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Component
public class CrossPlatformExecutor extends BaseExecutor {
    
    @Override
    public CaseResult execute(TestCase testCase, Path workDir, String[] command, 
                             long timeLimit, double memoryLimit) throws IOException, InterruptedException {
        
        // 根据平台调整命令
        String[] adjustedCommand = adjustCommandForPlatform(command, workDir, timeLimit, memoryLimit);
        
        // 调用基类执行
        return super.execute(testCase, workDir, adjustedCommand, timeLimit, memoryLimit);
    }
    
    /**
     * 根据平台调整命令，添加资源限制
     */
    private String[] adjustCommandForPlatform(String[] originalCommand, Path workDir, 
                                             long timeLimit, double memoryLimit) {
        PlatformDetector.Platform platform = PlatformDetector.getCurrentPlatform();
        
        return switch (platform) {
            case LINUX -> adjustCommandLinux(originalCommand, workDir, timeLimit, memoryLimit);
            case MACOS -> adjustCommandMacOS(originalCommand, workDir, timeLimit, memoryLimit);
            case WINDOWS -> adjustCommandWindows(originalCommand, workDir, timeLimit, memoryLimit);
            default -> originalCommand; // 未知平台，使用原始命令
        };
    }
    
    /**
     * Linux 平台：优先使用 prlimit，如果没有则使用 ulimit
     */
    private String[] adjustCommandLinux(String[] originalCommand, Path workDir, 
                                       long timeLimit, double memoryLimit) {
        // 检查是否有 prlimit 命令
        if (isCommandAvailable("prlimit")) {
            // 使用 prlimit 设置资源限制
            // prlimit --as=内存限制(KB) --cpu=时间限制(秒) -- 原命令
            long timeLimitSeconds = (timeLimit + 999) / 1000; // 向上取整到秒
            long memoryLimitBytes = (long)(memoryLimit * 1024); // 转换为字节
            
            String[] newCommand = new String[originalCommand.length + 6];
            newCommand[0] = "prlimit";
            newCommand[1] = "--as=" + memoryLimitBytes;
            newCommand[2] = "--cpu=" + timeLimitSeconds;
            newCommand[3] = "--nofile=64"; // 限制文件描述符
            newCommand[4] = "--nproc=1"; // 限制进程数
            newCommand[5] = "--";
            System.arraycopy(originalCommand, 0, newCommand, 6, originalCommand.length);
            return newCommand;
        } else {
            // 回退到使用 timeout 命令（仅限时，不限内存）
            log.warn("prlimit not available, using timeout for time limit only");
            String[] newCommand = new String[originalCommand.length + 3];
            newCommand[0] = "timeout";
            newCommand[1] = String.valueOf((timeLimit + 999) / 1000) + "s";
            newCommand[2] = "--";
            System.arraycopy(originalCommand, 0, newCommand, 3, originalCommand.length);
            return newCommand;
        }
    }
    
    /**
     * macOS 平台：使用 sandbox-exec 或 ulimit
     */
    private String[] adjustCommandMacOS(String[] originalCommand, Path workDir, 
                                        long timeLimit, double memoryLimit) {
        // macOS 可以使用 sandbox-exec，但配置较复杂
        // 这里使用 gtimeout（如果安装了 coreutils）或简单的执行
        if (isCommandAvailable("gtimeout")) {
            String[] newCommand = new String[originalCommand.length + 3];
            newCommand[0] = "gtimeout";
            newCommand[1] = String.valueOf((timeLimit + 999) / 1000) + "s";
            newCommand[2] = "--";
            System.arraycopy(originalCommand, 0, newCommand, 3, originalCommand.length);
            return newCommand;
        } else {
            // macOS 没有原生的 timeout，只能依赖 Java 层面的超时控制
            log.warn("gtimeout not available on macOS, relying on Java-level timeout");
            return originalCommand;
        }
    }
    
    /**
     * Windows 平台：使用 PowerShell 或基础执行
     */
    private String[] adjustCommandWindows(String[] originalCommand, Path workDir, 
                                          long timeLimit, double memoryLimit) {
        // Windows 资源限制较复杂，需要 Job Objects（需要 JNI）
        // 这里先使用基础执行，依赖 Java 层面的超时控制
        log.warn("Windows platform detected, limited resource control available");
        return originalCommand;
    }
    
    /**
     * 检查命令是否可用
     */
    private boolean isCommandAvailable(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version")
                    .redirectErrorStream(true)
                    .start();
            boolean completed = process.waitFor(1, TimeUnit.SECONDS);
            if (completed) {
                return process.exitValue() == 0;
            }
            process.destroyForcibly();
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
