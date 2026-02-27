# JudgeCore 功能文档

## 1. 项目概述

### 1.1 项目简介

JudgeCore 是一个类似力扣（LeetCode）平台的在线代码评测系统后端服务，提供多语言代码执行和测试用例验证功能。该系统支持 7 种主流编程语言，能够安全地执行用户提交的代码并返回详细的测试结果。

### 1.2 技术栈

| 技术组件    | 版本   | 说明         |
| ----------- | ------ | ------------ |
| Spring Boot | 3.4.4  | Web 框架     |
| Java        | 17     | 运行环境     |
| gRPC        | 1.60.1 | 远程过程调用 |
| Maven       | -      | 项目构建工具 |
| Firejail    | -      | 沙箱安全隔离 |

### 1.3 项目模块

```
JudgeCore/
├── JudgeCore-app/           # 主应用模块
│   ├── src/main/java/
│   │   ├── org/laoli/
│   │   │   ├── api/        # gRPC API接口
│   │   │   ├── config/     # 配置类
│   │   │   ├── judge/      # 核心评测逻辑
│   │   │   │   ├── controller/  # HTTP控制器
│   │   │   │   ├── model/      # 数据模型
│   │   │   │   ├── service/    # 业务服务
│   │   │   │   └── util/       # 工具类
│   │   │   └── Application.java # 启动类
│   │   └── proto/          # gRPC协议定义
│   └── src/main/resources/ # 配置文件
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── application-test.yml
└── pom.xml
```

---

## 2. 功能特性

### 2.1 核心功能

#### 2.1.1 代码评测服务

提供完整的代码执行和评测能力：

- **代码编译**：支持编译型语言（JAVA、CPP、C、RUST、GO）的编译
- **代码执行**：安全执行用户代码
- **输出比对**：比较实际输出与预期输出
- **性能监控**：记录执行时间和内存使用

#### 2.1.2 支持的编程语言

| 语言   | 类型   | 编译/执行方式 | 文件要求       |
| ------ | ------ | ------------- | -------------- |
| JAVA   | 编译型 | javac + java  | Main.java      |
| C      | 编译型 | gcc           | 标准 C 代码    |
| CPP    | 编译型 | g++           | 标准 C++代码   |
| RUST   | 编译型 | rustc         | 标准 Rust 代码 |
| GO     | 编译型 | go build      | 标准 Go 代码   |
| PYTHON | 解释型 | python3       | solution.py    |
| PHP    | 解释型 | php           | 标准 PHP 代码  |

#### 2.1.3 测试用例管理

- 支持批量测试用例执行
- 支持单个测试用例调试
- 可配置的测试输入和预期输出
- 详细的测试结果反馈

### 2.2 API 接口

#### 2.2.1 代码评测接口

**接口地址**: `POST /api/judge`

| 参数        | 类型   | 必填 | 说明                          |
| ----------- | ------ | ---- | ----------------------------- |
| code        | string | 是   | 用户提交的源代码              |
| language    | string | 是   | 编程语言类型                  |
| timeLimit   | long   | 否   | 时间限制（毫秒），默认 1000ms |
| memoryLimit | long   | 否   | 内存限制（KB），默认 4096KB   |
| cases       | array  | 是   | 测试用例列表                  |

#### 2.2.2 批量测试接口

**接口地址**: `POST /api/judge/test`

执行代码并运行所有测试用例，返回每个测试用例的详细结果。

#### 2.2.3 单个测试接口

**接口地址**: `POST /api/judge/test/single`

仅执行第一个测试用例，用于调试。

#### 2.2.4 健康检查接口

**接口地址**: `GET /api/judge/health`

返回服务健康状态。

#### 2.2.5 支持语言查询

**接口地址**: `GET /api/judge/languages`

返回系统支持的所有编程语言列表。

### 2.3 安全机制

#### 2.3.1 沙箱隔离

使用 Firejail 实现进程隔离：

```yaml
sandbox:
  enabled: true
  command: firejail
  common-options:
    quiet: true # 安静模式
    seccomp: true # 系统调用过滤
    net-none: true # 禁用网络
    no-groups: true # 禁用组权限
    no-new-privs: true # 禁止权限升级
    caps-drop: all # 丢弃所有能力
```

#### 2.3.2 Java 安全策略

针对 Java 代码使用自定义安全策略：

```bash
-Djava.security.manager -Djava.security.policy==<<ALL PERMISSIONS DENIED>>
```

### 2.4 性能优化

#### 2.4.1 线程池配置

| 参数             | 默认值           | 说明             |
| ---------------- | ---------------- | ---------------- |
| core-pool-size   | 20               | 核心线程数       |
| max-pool-size    | 50               | 最大线程数       |
| keep-alive-time  | 5000ms           | 空闲线程存活时间 |
| block-queue-size | 5000             | 阻塞队列大小     |
| policy           | CallerRunsPolicy | 拒绝策略         |

#### 2.4.2 JVM 优化

- `-XX:+PerfDisableSharedMem`：禁用性能统计共享内存
- `-XX:+UseG1GC`：使用 G1 垃圾收集器
- `-XX:MaxRAMPercentage=75.0`：最大堆内存占比

### 2.5 gRPC 服务

服务端口：9000

提供远程评测能力，支持微服务架构部署。

---

## 3. 配置说明

### 3.1 配置文件结构

```yaml
# 基础配置
spring:
  profiles:
    active: dev # 环境：dev, test, prod
  main:
    web-application-type: servlet

# 沙箱配置
sandbox:
  enabled: true # 是否启用沙箱
  command: firejail # 沙箱命令
  common-options: # 沙箱选项
    quiet: true
    seccomp: true
    net-none: true
    no-groups: true
    no-new-privs: true
    caps-drop: all

# 线程池配置
thread:
  pool:
    executor:
      config:
        core-pool-size: 20
        max-pool-size: 50
        keep-alive-time: 5000
        block-queue-size: 5000
        policy: CallerRunsPolicy

# gRPC配置
grpc:
  server:
    port: 9000

# HTTP服务器配置
server:
  port: 8080
```

### 3.2 环境配置

| 环境 | 配置文件             | 用途     |
| ---- | -------------------- | -------- |
| dev  | application-dev.yml  | 开发环境 |
| test | application-test.yml | 测试环境 |
| prod | application-prod.yml | 生产环境 |

---

## 4. 部署要求

### 4.1 系统要求

- 操作系统：Linux（推荐 Ubuntu/CentOS）
- Java：JDK 17+
- 内存：建议 4GB+
- 磁盘：建议 20GB+

### 4.2 运行时依赖

- Firejail（沙箱）
- 各语言运行时：
  - Java JDK
  - Python 3
  - GCC/G++
  - Rust
  - Go
  - PHP

---

## 5. 使用示例

### 5.1 代码评测请求示例

```json
{
  "code": "public class Main { public static void main(String[] args) { int a = Integer.parseInt(args[0]); int b = Integer.parseInt(args[1]); System.out.println(a + b); } }",
  "language": "JAVA",
  "timeLimit": 1000,
  "memoryLimit": 4096,
  "cases": [
    {
      "input": "1 2",
      "expectedOutput": "3"
    },
    {
      "input": "3 4",
      "expectedOutput": "7"
    }
  ]
}
```

### 5.2 响应示例

```json
{
  "status": "ALL_PASSED",
  "message": "All test cases passed",
  "executionTime": 95,
  "memoryUsed": 1024,
  "caseInfo": {
    "input": "1 2",
": "3",
    "actualOutput": "3"
  }
}
```

---

## 6. 状态码说明 "expectedOutput

| 状态码                | 说明         |
| --------------------- | ------------ |
| ALL_PASSED            | 所有测试通过 |
| PARTIAL_PASSED        | 部分测试通过 |
| ALL_FAILED            | 所有测试失败 |
| COMPILE_ERROR         | 编译错误     |
| RUNTIME_ERROR         | 运行时错误   |
| TIME_LIMIT_EXCEEDED   | 超时         |
| MEMORY_LIMIT_EXCEEDED | 内存超限     |
| SYSTEM_ERROR          | 系统错误     |

---

## 7. 扩展说明

### 7.1 扩展新语言

要支持新的编程语言，需要：

1. 在 `Language` 枚举中添加新语言
2. 实现对应的 `Compiler` 接口
3. 在 `LanguageCommandFactory` 中添加执行命令
4. 更新 API 文档

### 7.2 性能调优

- 根据服务器配置调整线程池参数
- 调整 JVM 堆内存大小
- 考虑使用 Docker 容器化部署实现资源隔离
