package org.laoli.judge.service.execute.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description 平台检测工具类
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
public class PlatformDetector {
    
    public enum Platform {
        LINUX,
        MACOS,
        WINDOWS,
        UNKNOWN
    }
    
    private static final Platform currentPlatform = detectPlatform();
    
    /**
     * 检测当前运行平台
     */
    private static Platform detectPlatform() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        
        if (osName.contains("linux")) {
            return Platform.LINUX;
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return Platform.MACOS;
        } else if (osName.contains("win")) {
            return Platform.WINDOWS;
        } else {
            log.warn("Unknown platform: {}", osName);
            return Platform.UNKNOWN;
        }
    }
    
    /**
     * 获取当前平台
     */
    public static Platform getCurrentPlatform() {
        return currentPlatform;
    }
    
    /**
     * 检查是否为 Linux
     */
    public static boolean isLinux() {
        return currentPlatform == Platform.LINUX;
    }
    
    /**
     * 检查是否为 macOS
     */
    public static boolean isMacOS() {
        return currentPlatform == Platform.MACOS;
    }
    
    /**
     * 检查是否为 Windows
     */
    public static boolean isWindows() {
        return currentPlatform == Platform.WINDOWS;
    }
    
    /**
     * 检查是否支持 Firejail（仅 Linux）
     */
    public static boolean isFirejailAvailable() {
        if (!isLinux()) {
            return false;
        }
        try {
            Process process = new ProcessBuilder("firejail", "--version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.debug("Firejail not available: {}", e.getMessage());
            return false;
        }
    }
}
