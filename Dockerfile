FROM ubuntu:22.04

LABEL maintainer="laoli <3180628481@qq.com>"
LABEL description="JudgeCore - Online Code Judge System"

ENV DEBIAN_FRONTEND=noninteractive \
    JAVA_HOME=/opt/java/openjdk \
    PATH=/opt/java/openjdk/bin:/usr/local/go/bin:$PATH \
    GOROOT=/usr/local/go \
    GOPATH=/root/go \
    PYTHONUNBUFFERED=1

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    python3 \
    python3-pip \
    gcc \
    g++ \
    make \
    curl \
    wget \
    git \
    firejail \
    php \
    rustc \
    cargo \
    golang-go \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

RUN pip3 install  --no-cache-dir protobuf==3.20.3

RUN useradd -m -s /bin/bash judgeuser

WORKDIR /app

COPY JudgeCore-app/target/JudgeCore-app.jar app.jar

RUN mkdir -p /app/workdir /app/logs && \
    chown -R judgeuser:judgeuser /app

USER judgeuser

EXPOSE 8080 9000

ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC" \
    SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
