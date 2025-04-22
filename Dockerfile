# 第一阶段：构建项目
FROM ubuntu:22.04 AS builder

# 设置语言环境
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
ENV DEBIAN_FRONTEND=noninteractive

# 更新系统并安装基础工具
RUN apt-get update && apt-get install -y \
    maven \
    curl \
    wget \
    git \
    build-essential \
    firejail \
    python3 \
    python3-pip \
    openjdk-17-jdk \
    golang \
    php-cli \
    rustc \
    cargo \
    kotlin

# 安装 C++ 和 C 编译器（通过 GCC）
RUN apt-get update && apt-get install -y gcc g++ && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . /app
RUN mvn clean package

# 第二阶段：运行项目
FROM ubuntu:22.04

# 设置语言环境
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
ENV DEBIAN_FRONTEND=noninteractive

# 安装运行所需的依赖
RUN apt-get update && \
    apt-get install -y \
    openjdk-17-jre \
    python3 \
    python3-pip \
    gcc \
    g++ \
    golang \
    php-cli \
    rustc \
    cargo \
    kotlin && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/JudgeCore-app/target/JudgeCore-app.jar  /app/JudgeCore-app.jar

EXPOSE 9000
CMD ["java", "-jar", "JudgeCore-app.jar"]