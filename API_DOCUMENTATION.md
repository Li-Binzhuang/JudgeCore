# JudgeCore 代码测试 API 文档

## 概述

本API提供类似力扣平台的代码自测功能，支持多种编程语言的代码执行和测试用例验证。

## 基础信息

- **Base URL**: `http://localhost:8080/api/judge`
- **Content-Type**: `application/json`
- **请求方式**: POST

## 接口列表

### 1. 批量测试用例执行

执行代码并运行所有测试用例，返回每个测试用例的详细结果。

**接口地址**: `/test`

**请求方法**: POST

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| code | string | 是 | 用户提交的源代码，最大65536字符 |
| language | string | 是 | 编程语言类型，支持：JAVA, PYTHON, CPP, C, RUST, GO, PHP |
| testCases | array | 否 | 测试用例列表，如果为空则仅执行代码 |
| timeLimit | long | 否 | 时间限制（毫秒），默认1000ms，最小100ms |
| memoryLimit | long | 否 | 内存限制（KB），默认4096KB，最小1024KB |
| showDetail | boolean | 否 | 是否显示详细输出，默认false |

**testCases 数组元素结构**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | string | 否 | 测试用例ID |
| input | string | 否 | 测试输入 |
| expectedOutput | string | 否 | 预期输出 |
| description | string | 否 | 测试用例描述 |

**请求示例**:

```json
{
  "code": "public class Main { public static void main(String[] args) { int a = Integer.parseInt(args[0]); int b = Integer.parseInt(args[1]); System.out.println(a + b); } }",
  "language": "JAVA",
  "testCases": [
    {
      "id": "case1",
      "input": "1 2",
      "expectedOutput": "3",
      "description": "简单加法测试"
    },
    {
      "id": "case2",
      "input": "3 4",
      "expectedOutput": "7",
      "description": "另一个加法测试"
    }
  ],
  "timeLimit": 1000,
  "memoryLimit": 4096,
  "showDetail": true
}
```

**响应参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| status | string | 测试状态 |
| message | string | 响应消息 |
| passedCount | integer | 通过的测试用例数量 |
| totalCount | integer | 总测试用例数量 |
| caseResults | array | 所有测试用例的结果列表 |
| totalExecutionTime | long | 总执行时间（毫秒） |
| maxMemoryUsed | long | 最大内存使用（KB） |

**caseResults 数组元素结构**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| caseId | string | 测试用例ID |
| index | integer | 测试用例索引 |
| description | string | 测试用例描述 |
| input | string | 测试输入 |
| expectedOutput | string | 预期输出 |
| actualOutput | string | 实际输出 |
| status | string | 测试结果状态 |
| executionTime | long | 执行时间（毫秒） |
| memoryUsed | long | 内存使用（KB） |
| errorMessage | string | 错误信息（如果有） |

**响应示例 - 全部通过**:

```json
{
  "status": "ALL_PASSED",
  "message": "All test cases passed",
  "passedCount": 2,
  "totalCount": 2,
  "caseResults": [
    {
      "caseId": "case1",
      "index": 0,
      "description": "简单加法测试",
      "input": "1 2",
      "expectedOutput": "3",
      "actualOutput": "3",
      "status": "ALL_PASSED",
      "executionTime": 50,
      "memoryUsed": 1024,
      "errorMessage": null
    },
    {
      "caseId": "case2",
      "index": 1,
      "description": "另一个加法测试",
      "input": "3 4",
      "expectedOutput": "7",
      "actualOutput": "7",
      "status": "ALL_PASSED",
      "executionTime": 45,
      "memoryUsed": 1024,
      "errorMessage": null
    }
  ],
  "totalExecutionTime": 95,
  "maxMemoryUsed": 1024
}
```

**响应示例 - 部分通过**:

```json
{
  "status": "PARTIAL_PASSED",
  "message": "1/2 test cases passed",
  "passedCount": 1,
  "totalCount": 2,
  "caseResults": [
    {
      "caseId": "case1",
      "index": 0,
      "description": "简单加法测试",
      "input": "1 2",
      "expectedOutput": "3",
      "actualOutput": "3",
      "status": "ALL_PASSED",
      "executionTime": 50,
      "memoryUsed": 1024,
      "errorMessage": null
    },
    {
      "caseId": "case2",
      "index": 1,
      "description": "另一个加法测试",
      "input": "3 4",
      "expectedOutput": "7",
      "actualOutput": "8",
      "status": "ALL_FAILED",
      "executionTime": 45,
      "memoryUsed": 1024,
      "errorMessage": "Output mismatch"
    }
  ],
  "totalExecutionTime": 95,
  "maxMemoryUsed": 1024
}
```

### 2. 单个测试用例执行

仅执行第一个测试用例，用于调试或单独运行某个测试。

**接口地址**: `/test/single`

**请求方法**: POST

**请求参数**: 同批量测试接口，但仅使用第一个测试用例

**请求示例**:

```json
{
  "code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello World\"); } }",
  "language": "JAVA",
  "testCases": [
    {
      "id": "hello",
      "input": "",
      "expectedOutput": "Hello World",
      "description": "Hello World测试"
    }
  ],
  "timeLimit": 1000,
  "memoryLimit": 4096,
  "showDetail": true
}
```

**响应示例**:

```json
{
  "status": "ALL_PASSED",
  "message": "All test cases passed",
  "passedCount": 1,
  "totalCount": 1,
  "caseResults": [
    {
      "caseId": "hello",
      "index": 0,
      "description": "Hello World测试",
      "input": "",
      "expectedOutput": "Hello World",
      "actualOutput": "Hello World",
      "status": "ALL_PASSED",
      "executionTime": 50,
      "memoryUsed": 1024,
      "errorMessage": null
    }
  ],
  "totalExecutionTime": 50,
  "maxMemoryUsed": 1024
}
```

### 3. 仅执行代码（无测试用例）

当不提供测试用例时，系统仅执行代码并返回执行结果。

**请求示例**:

```json
{
  "code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello from JudgeCore\"); } }",
  "language": "JAVA",
  "timeLimit": 1000,
  "memoryLimit": 4096
}
```

**响应示例**:

```json
{
  "status": "EXECUTED_ONLY",
  "message": "Code executed successfully",
  "passedCount": 0,
  "totalCount": 0,
  "caseResults": null,
  "totalExecutionTime": 50,
  "maxMemoryUsed": 1024
}
```

## 测试状态说明

| 状态码 | 说明 |
|--------|------|
| ALL_PASSED | 所有测试通过 |
| PARTIAL_PASSED | 部分测试通过 |
| ALL_FAILED | 所有测试失败 |
| COMPILE_ERROR | 编译错误 |
| TIME_LIMIT_EXCEEDED | 执行超时 |
| MEMORY_LIMIT_EXCEEDED | 内存超限 |
| RUNTIME_ERROR | 运行时错误 |
| SYSTEM_ERROR | 系统错误 |
| EXECUTED_ONLY | 无需测试（仅执行代码） |

## 错误响应示例

**编译错误**:

```json
{
  "status": "COMPILE_ERROR",
  "message": "Syntax error on token \"invalid\", delete this token",
  "passedCount": 0,
  "totalCount": 0,
  "caseResults": null,
  "totalExecutionTime": 0,
  "maxMemoryUsed": 0
}
```

**系统错误**:

```json
{
  "status": "SYSTEM_ERROR",
  "message": "Internal server error: Something went wrong",
  "passedCount": 0,
  "totalCount": 0,
  "caseResults": null,
  "totalExecutionTime": 0,
  "maxMemoryUsed": 0
}
```

## 其他接口

### 健康检查

**接口地址**: `/health`

**请求方法**: GET

**响应**: `OK`

### 获取支持的编程语言列表

**接口地址**: `/languages`

**请求方法**: GET

**响应示例**:

```json
["JAVA", "PYTHON", "CPP", "C", "RUST", "GO", "PHP"]
```

## 使用建议

1. **代码格式**: 确保代码符合所选编程语言的语法规范
2. **输入输出**: 程序应从标准输入读取数据，并输出到标准输出
3. **时间限制**: 复杂算法可能需要增加时间限制
4. **内存限制**: 处理大数据时注意内存使用
5. **showDetail**: 开发调试时建议设置为true，生产环境可设为false减少数据传输量

## APIFox 测试说明

1. 导入本文档到 APIFox
2. 配置 Base URL 为 `http://localhost:8080/api/judge`
3. 使用请求示例进行测试
4. 查看响应结果进行调试
