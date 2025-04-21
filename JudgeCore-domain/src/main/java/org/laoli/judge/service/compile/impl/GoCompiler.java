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
 * @Description go语言编译器实现
 * @Author laoli
 * @Date 2025/4/20 14:48
 */
@Component("GO")
public class GoCompiler implements Compiler {

    @Override
    public JudgeResult compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("solution.go");
        Files.write(sourceFile, sourceCode.getBytes());
        // 编译Go代码
        ProcessBuilder pb = new ProcessBuilder("go","build", "-o", "solution", sourceFile.toString());
        return preCompile(workDir, pb);
    }
}
