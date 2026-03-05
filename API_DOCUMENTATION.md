# JudgeCore HTTP 接口文档

## 基础信息

- Base URL: `http://localhost:8080/api/judge`
- Content-Type: `application/json`
- 支持语言：`JAVA` `PYTHON` `CPP` `C` `RUST` `GO` `PHP`

## 接口总览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/` | 标准判题（LeetCode 风格，返回首个关键信息） |
| POST | `/test` | 批量测试，返回每个用例结果 |
| POST | `/test/single` | 单测模式，仅执行 `testCases` 第一个用例 |
| GET | `/health` | 健康检查 |
| GET | `/languages` | 获取支持语言列表 |

## 1) POST `/`

用于标准判题。请求体字段名是 `cases`（不是 `testCases`）。

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| code | string | 是 | 源代码 |
| language | string | 是 | 编程语言 |
| cases | array | 是 | 测试用例列表 |
| timeLimit | long | 是 | 时间限制（毫秒） |
| memoryLimit | long | 是 | 内存限制（KB） |

`cases[]` 元素：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| input | string | 否 | 输入 |
| expectedOutput | string | 否 | 期望输出 |

请求示例：

```json
{
  "code": "public class Main { public static void main(String[] args){ System.out.println(3); } }",
  "language": "JAVA",
  "timeLimit": 1000,
  "memoryLimit": 4096,
  "cases": [
    { "input": "", "expectedOutput": "3" }
  ]
}
```

成功响应（200）：

```json
{
  "status": "ACCEPTED",
  "message": "All test cases passed",
  "executionTime": 25,
  "memoryUsed": 1024,
  "errorDetail": null,
  "caseInfo": {
    "input": "",
    "expectedOutput": "3",
    "actualOutput": "3"
  }
}
```

错误响应（常见）：
- 400：语言不支持（`status=SYSTEM_ERROR`）
- 500：服务内部错误（`status=SYSTEM_ERROR`）

## 2) POST `/test`

批量测试接口。请求体字段名是 `testCases`。

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| code | string | 是 | 源代码（最大 65536 字符） |
| language | string | 是 | 编程语言 |
| testCases | array | 否 | 测试用例列表；为空则仅执行代码 |
| timeLimit | long | 否 | 时间限制；缺省或 <100 按 1000 处理 |
| memoryLimit | long | 否 | 内存限制；缺省或 <1024 按 4096 处理 |
| showDetail | boolean | 否 | 是否返回输入/输出细节，默认 false |

`testCases[]` 元素：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | string | 否 | 用例 ID |
| input | string | 否 | 输入 |
| expectedOutput | string | 否 | 期望输出（为空时仅执行不比对） |
| description | string | 否 | 说明 |

响应核心字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| status | string | `ALL_PASSED` / `PARTIAL_PASSED` / `ALL_FAILED` / `COMPILE_ERROR` / `TIME_LIMIT_EXCEEDED` / `MEMORY_LIMIT_EXCEEDED` / `RUNTIME_ERROR` / `SYSTEM_ERROR` / `EXECUTED_ONLY` |
| message | string | 状态描述 |
| passedCount | int | 通过数 |
| totalCount | int | 总数 |
| caseResults | array/null | 用例结果列表 |
| totalExecutionTime | long | 总耗时（ms） |
| maxMemoryUsed | long | 峰值内存（KB） |

请求示例：

```json
{
  "code": "public class Main { public static void main(String[] args){ java.util.Scanner sc = new java.util.Scanner(System.in); int a=sc.nextInt(),b=sc.nextInt(); System.out.println(a+b); } }",
  "language": "JAVA",
  "timeLimit": 1000,
  "memoryLimit": 4096,
  "showDetail": true,
  "testCases": [
    { "id": "c1", "input": "1 2", "expectedOutput": "3", "description": "加法1" },
    { "id": "c2", "input": "3 4", "expectedOutput": "7", "description": "加法2" }
  ]
}
```

## 3) POST `/test/single`

- 入参同 `/test`
- 仅取 `testCases` 第一个元素执行
- 若 `testCases` 为空，则行为与“仅执行代码”一致（`EXECUTED_ONLY`）

## 4) GET `/health`

响应示例：

```text
OK
```

## 5) GET `/languages`

响应示例：

```json
["JAVA", "PYTHON", "CPP", "C", "RUST", "GO", "PHP"]
```
