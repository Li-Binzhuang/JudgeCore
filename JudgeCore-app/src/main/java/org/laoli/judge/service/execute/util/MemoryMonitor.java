package org.laoli.judge.service.execute.util;

import lombok.extern.slf4j.Slf4j;
import org.laoli.judge.service.execute.util.PlatformDetector.Platform;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description 跨平台内存监控工具
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
public class MemoryMonitor {
    
    private static final Platform platform = PlatformDetector.getCurrentPlatform();
    
    /**
     * 估算进程内存使用（KB）
     * @param pid 进程ID
     * @return 内存使用量（KB），如果无法获取则返回0
     */
    public static double estimateMemoryUsage(Long pid) {
        if (pid == null) {
            return 0;
        }
        
        try {
            return switch (platform) {
                case LINUX -> estimateMemoryLinux(pid);
                case MACOS -> estimateMemoryMacOS(pid);
                case WINDOWS -> estimateMemoryWindows(pid);
                default -> estimateMemoryFallback(pid);
            };
        } catch (Exception e) {
            log.debug("Error estimating memory usage for PID {}: {}", pid, e.getMessage());
            return 0;
        }
    }
    
    /**
     * Linux 内存监控（使用 /proc/[pid]/statm）
     */
    private static double estimateMemoryLinux(Long pid) {
        long residentPages = 0;
        try {
            Path statmPath = Paths.get("/proc", String.valueOf(pid), "statm");
            // 采样多次取最大值
            for (int i = 0; i < 20; i++) {
                if (Files.exists(statmPath)) {
                    List<String> lines = Files.readAllLines(statmPath);
                    if (!lines.isEmpty()) {
                        String[] stats = lines.get(0).split("\\s+");
                        if (stats.length > 1) {
                            long pages = Long.parseLong(stats[1]);
                            residentPages = Math.max(pages, residentPages);
                        }
                    }
                }
                Thread.sleep(50); // 等待50ms后再次采样
            }
            // 页大小通常是4KB，转换为KB
            return residentPages * 4;
        } catch (Exception e) {
            log.debug("Error reading Linux memory usage: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * macOS 内存监控（使用 ps 命令）
     */
    private static double estimateMemoryMacOS(Long pid) {
        try {
            Process process = new ProcessBuilder("ps", "-o", "rss=", "-p", String.valueOf(pid)).start();
            process.waitFor(1, TimeUnit.SECONDS);
            
            String output = new String(process.getInputStream().readAllBytes()).trim();
            if (!output.isEmpty()) {
                // RSS 单位是 KB
                return Double.parseDouble(output);
            }
        } catch (Exception e) {
            log.debug("Error reading macOS memory usage: {}", e.getMessage());
        }
        return 0;
    }
    
    /**
     * Windows 内存监控（使用 wmic 或 tasklist）
     */
    private static double estimateMemoryWindows(Long pid) {
        try {
            // 使用 wmic 获取内存使用（单位：KB）
            Process process = new ProcessBuilder(
                "wmic", "process", "where", "ProcessId=" + pid,
                "get", "WorkingSetSize", "/format:value"
            ).start();
            
            process.waitFor(1, TimeUnit.SECONDS);
            String output = new String(process.getInputStream().readAllBytes());
            
            // 解析输出，查找 WorkingSetSize=xxx
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.startsWith("WorkingSetSize=")) {
                    String value = line.substring("WorkingSetSize=".length()).trim();
                    if (!value.isEmpty()) {
                        // WorkingSetSize 单位是字节，转换为KB
                        return Long.parseLong(value) / 1024.0;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error reading Windows memory usage: {}", e.getMessage());
        }
        return 0;
    }
    
    /**
     * 回退方案：尝试使用通用的系统命令获取内存信息
     * 按优先级尝试多种方法：
     * 1. ps 命令（Unix-like 系统）
     * 2. tasklist 命令（Windows）
     * 3. 如果都失败，返回 0
     */
    private static double estimateMemoryFallback(Long pid) {
        // 方法1: 尝试使用通用的 ps 命令（大多数 Unix-like 系统支持）
        try {
            Process process = new ProcessBuilder("ps", "-o", "rss=", "-p", String.valueOf(pid)).start();
            boolean completed = process.waitFor(1, TimeUnit.SECONDS);
            
            if (completed && process.exitValue() == 0) {
                String output = new String(process.getInputStream().readAllBytes()).trim();
                if (!output.isEmpty() && !output.matches(".*[^0-9.].*")) {
                    // RSS 单位是 KB，确保输出是纯数字
                    try {
                        double memory = Double.parseDouble(output);
                        log.debug("Fallback: Successfully retrieved memory using ps command: {} KB", memory);
                        return memory;
                    } catch (NumberFormatException e) {
                        log.debug("Fallback: ps output is not a valid number: {}", output);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Fallback: ps command failed: {}", e.getMessage());
        }
        
        // 方法2: 尝试使用 Windows 的 tasklist 命令（如果系统可能是 Windows）
        try {
            Process process = new ProcessBuilder(
                "tasklist", "/FI", "PID eq " + pid, "/FO", "CSV", "/NH"
            ).start();
            boolean completed = process.waitFor(1, TimeUnit.SECONDS);
            
            if (completed && process.exitValue() == 0) {
                String output = new String(process.getInputStream().readAllBytes());
                if (output == null || output.trim().isEmpty()) {
                    log.debug("Fallback: tasklist returned empty output");
                    return 0;
                }
                // tasklist CSV 格式: "进程名","PID","会话名","会话#","内存使用"
                String[] lines = output.split("\n");
                for (String line : lines) {
                    if (line.contains(String.valueOf(pid))) {
                        String[] parts = line.split(",");
                        if (parts.length >= 5) {
                            // 内存使用在最后一列，格式如 "1,234 K" 或 "1,234,567 K"
                            String memoryStr = parts[4].replace("\"", "").replace(" K", "").replace(",", "").trim();
                            try {
                                double memory = Double.parseDouble(memoryStr);
                                log.debug("Fallback: Successfully retrieved memory using tasklist: {} KB", memory);
                                return memory;
                            } catch (NumberFormatException e) {
                                log.debug("Fallback: tasklist memory value is not a valid number: {}", memoryStr);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Fallback: tasklist command failed: {}", e.getMessage());
        }
        
        // 如果所有方法都失败，记录警告并返回 0
        log.warn("Fallback: Unable to estimate memory usage for PID {} on platform {}. " +
                "Memory monitoring will be unavailable. This may affect memory limit enforcement.", 
                pid, platform);
        return 0;
    }
}
