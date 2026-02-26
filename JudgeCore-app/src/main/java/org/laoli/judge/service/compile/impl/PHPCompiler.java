package org.laoli.judge.service.compile.impl;

import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.service.compile.Compiler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.laoli.judge.service.compile.BuildFile.preCompile;

/**
 * @Description PHP语言编译器实现
 * @Author laoli
 * @Date 2025/4/20 14:50
 */
@Component("PHP")
public class PHPCompiler implements Compiler {
    @Override
    public JudgeResult compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("solution.php");
        Files.write(sourceFile, sourceCode.getBytes());
        // 编译PHP代码
        ProcessBuilder pb = new ProcessBuilder("php", "-l", sourceFile.toString());
        return preCompile(workDir, pb);
    }
}
