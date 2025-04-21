package org.laoli.judge.service.compile;

import org.laoli.judge.model.aggregate.JudgeResult;

import java.io.IOException;
import java.nio.file.Path;

/**
 *@description 代码编译器接口
 *@author laoli
 *@create 2025/4/20 12:06
 */
public interface Compiler {
    JudgeResult compile(String sourceCode, Path workDir) throws IOException, InterruptedException;
}
