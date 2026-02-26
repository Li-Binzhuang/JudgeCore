package org.laoli.judge.service.compile.impl;

import org.laoli.judge.model.aggregate.JudgeResult;
import org.laoli.judge.service.compile.Compiler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import static org.laoli.judge.service.compile.BuildFile.preCompile;

/**
 * @Description Python语言编译器实现
 * @Author laoli
 * @Date 2025/4/20 12:15
 */
@Component("PYTHON")
public class PythonCompiler implements Compiler {

    @Override
    public JudgeResult compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("solution.py");
        Files.write(sourceFile, sourceCode.getBytes());

        // 检查Python语法
        ProcessBuilder pb = new ProcessBuilder("python3", "-m", "py_compile", sourceFile.toString());
        return preCompile(workDir, pb);
    }
}
