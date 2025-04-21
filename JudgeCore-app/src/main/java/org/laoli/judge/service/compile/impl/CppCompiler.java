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
 * @Description C++语言编译器实现
 * @Author laoli
 * @Date 2025/4/20 12:16
 */
@Component("CPP")
public class CppCompiler implements Compiler {
    @Override
    public JudgeResult compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("solution.cpp");
        Files.write(sourceFile, sourceCode.getBytes());

        // 编译C++代码
        ProcessBuilder pb = new ProcessBuilder(
                "g++","-std=c++17", "-O3", "-march=native","-flto", "-Wall" , "-o", "cpp_solution",
                sourceFile.toString());

        return preCompile(workDir, pb);
    }
}
