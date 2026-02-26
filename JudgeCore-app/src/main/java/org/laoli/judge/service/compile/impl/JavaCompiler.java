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
 *@description Java语言编译器实现
 *@author laoli
 *@create 2025/4/20 12:09
 */
@Component("JAVA")
public class JavaCompiler implements Compiler {

    @Override
    public JudgeResult compile(String sourceCode, Path workDir) throws IOException, InterruptedException {
        // 创建源代码文件
        Path sourceFile = workDir.resolve("Main.java");
        Files.write(sourceFile, sourceCode.getBytes());

        // 编译Java代码
        ProcessBuilder pb = new ProcessBuilder("javac", sourceFile.toString());
        return preCompile(workDir, pb);
    }
}
