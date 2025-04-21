package org.laoli.judge.service.compile.impl;

import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.service.compile.Compiler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.laoli.judge.service.compile.BuildFile.preCompile;

/**
 * @Description Kotlin编译器实现
 * @Author laoli
 * @Date 2025/4/20 14:53
 */
@Component("KOTLIN")
public class KotlinCompiler implements Compiler {

    @Override
    public JudgeResult compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("Main.kt");
        Files.write(sourceFile, sourceCode.getBytes());

        // 编译 Kotlin 代码
        ProcessBuilder pb = new ProcessBuilder("kotlinc", sourceFile.toString(), "-include-runtime", "-d", "Main.jar");
        return preCompile(workDir, pb);
    }
}
