package org.laoli.judge.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ProcessUtils {

    private static final String OS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    private static final boolean IS_LINUX = OS.contains("linux");
    private static final boolean IS_MAC = OS.contains("mac") || OS.contains("darwin");
    private static final boolean IS_WINDOWS = OS.contains("windows");

    // Linux /proc/[pid]/status RssAnon 单位是 kB
    private static final Pattern LINUX_RSS_PATTERN = Pattern.compile("RssAnon:\\s+(\\d+)\\s+kB");
    // Linux /proc/[pid]/statm 第二列是 RSS (页), 假设页大小为 4KB
    private static final Pattern LINUX_STATM_PATTERN = Pattern.compile("(\\d+)\\s+(\\d+)");
    private ProcessUtils() {
    }

    /**
     * 估算进程内存占用
     * @param pid 进程ID
     * @return 内存占用大小，单位统一为 KB (千字节)
     */
    public static long estimateMemoryUsage(long pid) {
        if (pid <= 0) {
            return 0;
        }
        long memoryUsed = 0L;
        if (IS_LINUX) {
            memoryUsed = estimateLinuxMemory(pid);
        } else if (IS_MAC) {
            memoryUsed = estimateMacMemory(pid);
        } else if (IS_WINDOWS) {
            memoryUsed = estimateWindowsMemory(pid);
        }

        // 【修改点 1】移除了原有的 ">> 2" 操作。
        // 原有逻辑会导致 Linux/macOS (KB) 被除以 4，而 Windows (Bytes) 也被除以 4，单位不统一且数值错误。
        // 现在确保所有分支内部已经转换为 KB，直接返回即可。
        return memoryUsed;
    }

    private static long estimateLinuxMemory(long pid) {
        long maxMemory = 0;

        Path statusPath = Paths.get("/proc", String.valueOf(pid), "status");
        if (Files.exists(statusPath)) {
            try {
                maxMemory = readLinuxRssFromStatus(statusPath);
                if (maxMemory > 0) {
                    return maxMemory; // 单位已经是 KB
                }
            } catch (Exception e) {
                log.debug("Failed to read memory from /proc/{}/status: {}", pid, e.getMessage());
            }
        }

        Path statmPath = Paths.get("/proc", String.valueOf(pid), "statm");
        if (Files.exists(statmPath)) {
            try {
                maxMemory = readLinuxRssFromStatm(statmPath);
                // readLinuxRssFromStatm 内部已将页转换为 KB
            } catch (Exception e) {
                log.debug("Failed to read memory from /proc/{}/statm: {}", pid, e.getMessage());
            }
        }

        return maxMemory;
    }

    private static long readLinuxRssFromStatus(Path statusPath) throws IOException {
        long maxRss = 0;
        for (int i = 0; i < 5; i++) {
            try {
                String content = Files.readString(statusPath);
                Matcher matcher = LINUX_RSS_PATTERN.matcher(content);
                long currentRss = 0;
                while (matcher.find()) {
                    // 文件单位是 kB，直接解析
                    currentRss = Math.max(currentRss, Long.parseLong(matcher.group(1)));
                }
                if (currentRss > 0) {
                    maxRss = Math.max(maxRss, currentRss);
                    break;
                }
                if (i < 4) {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return maxRss; // 单位: KB
    }

    private static long readLinuxRssFromStatm(Path statmPath) throws IOException {
        long maxRss = 0;
        for (int i = 0; i < 5; i++) {
            try {
                String content = Files.readString(statmPath);
                Matcher matcher = LINUX_STATM_PATTERN.matcher(content);
                if (matcher.find()) {
                    long residentPages = Long.parseLong(matcher.group(2));
                    if (residentPages > 0) {
                        // 【修改点 2】确认单位转换
                        // statm 的单位是 Page。在绝大多数判题环境 (x86_64) 中，Page Size = 4KB。
                        // 所以 residentPages * 4 得到的是 KB。
                        maxRss = Math.max(maxRss, residentPages * 4);
                        break;
                    }
                }
                if (i < 4) {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return maxRss; // 单位: KB
    }

    private static long estimateMacMemory(long pid) {
        for (int i = 0; i < 5; i++) {
            try {
                // macOS ps -o rss= 默认输出单位是 KB
                ProcessBuilder pb = new ProcessBuilder("ps", "-o", "rss=", "-p", String.valueOf(pid));
                pb.redirectErrorStream(true);
                Process process = pb.start();

                String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                process.waitFor(1, TimeUnit.SECONDS);

                if (!output.isEmpty()) {
                    long rss = Long.parseLong(output.trim());
                    if (rss > 0) {
                        return rss; // 单位: KB
                    }
                }

                if (i < 4) {
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                log.debug("Failed to read memory for PID {} on macOS: {}", pid, e.getMessage());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return 0;
    }

    private static long estimateWindowsMemory(long pid) {
        for (int i = 0; i < 5; i++) {
            try {
                ProcessBuilder pb = new ProcessBuilder("wmic", "process", "where", "ProcessId=" + pid, "get",
                        "WorkingSetSize");
                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                long maxMemoryBytes = 0;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.matches("\\d+")) {
                        // WMIC 返回的是 Bytes
                        long val = Long.parseLong(line);
                        maxMemoryBytes = Math.max(maxMemoryBytes, val);
                    }
                }
                process.waitFor(1, TimeUnit.SECONDS);

                if (maxMemoryBytes > 0) {
                    // 【修改点 3】将 Windows 的 Bytes 转换为 KB
                    return maxMemoryBytes / 1024;
                }

                if (i < 4) {
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                log.debug("Failed to read memory for PID {} on Windows: {}", pid, e.getMessage());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return 0;
    }

    public static String normalizeOutput(String output) {
        if (output == null) {
            return "";
        }
        String result = output.trim();
        result = result.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        return result;
    }

    public static String readInputStream(java.io.InputStream inputStream) throws java.io.IOException {
        StringBuilder builder = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            builder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
        }
        return builder.toString();
    }
}