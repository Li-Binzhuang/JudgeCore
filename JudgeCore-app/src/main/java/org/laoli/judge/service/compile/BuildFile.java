package org.laoli.judge.service.compile;

import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.model.enums.SimpleResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

/**
 * @Description 创建编译文件
 * @Author laoli
 * @Date 2025/4/20 12:19
 */
public class BuildFile {
    public static JudgeResult preCompile(Path workDir, ProcessBuilder pb) throws IOException, InterruptedException {
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 获取编译输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            return JudgeResult.builder()
                    .status(SimpleResult.COMPILATION_ERROR)
                    .message(output.toString())
                    .build();
        }
        return JudgeResult.builder()
                    .status(SimpleResult.ACCEPTED)
                    .build();
    }
}
