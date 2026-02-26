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

    private static final Pattern LINUX_RSS_PATTERN = Pattern.compile("RssAnon:\\s+(\\d+)\\s+kB");
    private static final Pattern LINUX_STATM_PATTERN = Pattern.compile("(\\d+)\\s+(\\d+)");
    private static final Pattern MAC_PS_PATTERN = Pattern.compile("(\\d+)\\s+(\\d+)");

    private ProcessUtils() {
    }

    public static long estimateMemoryUsage(long pid) {
        if (pid <= 0) {
            return 0;
        }

        if (IS_LINUX) {
            return estimateLinuxMemory(pid);
        } else if (IS_MAC) {
            return estimateMacMemory(pid);
        } else if (IS_WINDOWS) {
            return estimateWindowsMemory(pid);
        }

        log.debug("Unsupported OS for memory estimation: {}", OS);
        return 0;
    }

    private static long estimateLinuxMemory(long pid) {
        long maxMemory = 0;

        Path statusPath = Paths.get("/proc", String.valueOf(pid), "status");
        if (Files.exists(statusPath)) {
            try {
                maxMemory = readLinuxRssFromStatus(statusPath);
                if (maxMemory > 0) {
                    return maxMemory * 1024;
                }
            } catch (Exception e) {
                log.debug("Failed to read memory from /proc/{}/status: {}", pid, e.getMessage());
            }
        }

        Path statmPath = Paths.get("/proc", String.valueOf(pid), "statm");
        if (Files.exists(statmPath)) {
            try {
                maxMemory = readLinuxRssFromStatm(statmPath);
            } catch (Exception e) {
                log.debug("Failed to read memory from /proc/{}/statm: {}", pid, e.getMessage());
            }
        }

        return maxMemory * 1024;
    }

    private static long readLinuxRssFromStatus(Path statusPath) throws IOException {
        long maxRss = 0;
        for (int i = 0; i < 5; i++) {
            try {
                String content = Files.readString(statusPath);
                Matcher matcher = LINUX_RSS_PATTERN.matcher(content);
                long currentRss = 0;
                while (matcher.find()) {
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
        return maxRss;
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
        return maxRss;
    }

    private static long estimateMacMemory(long pid) {
        for (int i = 0; i < 5; i++) {
            try {
                ProcessBuilder pb = new ProcessBuilder("ps", "-o", "rss=", "-p", String.valueOf(pid));
                pb.redirectErrorStream(true);
                Process process = pb.start();

                String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                process.waitFor(1, TimeUnit.SECONDS);

                if (!output.isEmpty()) {
                    long rss = Long.parseLong(output.trim());
                    if (rss > 0) {
                        return rss * 1024;
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
                long maxMemory = 0;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.matches("\\d+")) {
                        maxMemory = Math.max(maxMemory, Long.parseLong(line));
                    }
                }
                process.waitFor(1, TimeUnit.SECONDS);

                if (maxMemory > 0) {
                    return maxMemory;
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
