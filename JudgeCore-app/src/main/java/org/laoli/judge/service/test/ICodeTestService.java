package org.laoli.judge.service.test;

import org.laoli.judge.model.dto.CodeTestRequest;
import org.laoli.judge.model.dto.CodeTestResponse;

/**
 * 代码测试服务接口
 * 提供类似LeetCode的代码测试功能
 *
 * 功能特性:
 * 1. 支持有输入/输出的测试用例验证
 * 2. 支持无输入仅执行代码的场景
 * 3. 批量运行所有预设测试用例
 * 4. 返回每个测试用例的详细执行结果
 *
 * @author laoli
 * @date 2025/02/26
 */
public interface ICodeTestService {

    /**
     * 执行代码测试
     *
     * 处理流程:
     * 1. 验证输入参数
     * 2. 编译用户代码
     * 3. 执行每个测试用例
     * 4. 比对预期输出与实际输出
     * 5. 汇总并返回结果
     *
     * @param request 代码测试请求
     * @return 代码测试响应，包含每个测试用例的执行结果
     */
    CodeTestResponse executeTest(CodeTestRequest request);

    /**
     * 执行单个测试用例
     * 用于调试或单独运行某个测试
     *
     * @param request 代码测试请求 (仅使用第一个测试用例)
     * @return 单个测试用例的执行结果
     */
    CodeTestResponse executeSingleTest(CodeTestRequest request);
}
