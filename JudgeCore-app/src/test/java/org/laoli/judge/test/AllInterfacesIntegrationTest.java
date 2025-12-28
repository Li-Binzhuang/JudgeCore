package org.laoli.judge.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description 所有接口集成测试
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("所有接口集成测试")
public class AllInterfacesIntegrationTest {
    
    @Test
    @DisplayName("测试所有接口连接情况汇总")
    public void testAllInterfacesSummary() {
        log.info("==========================================");
        log.info("接口连接测试汇总");
        log.info("==========================================");
        log.info("1. 基础判题服务: 运行 CrossPlatformJudgeServiceTest");
        log.info("2. HTTP REST API: 运行 HttpInterfaceTest");
        log.info("3. gRPC 接口: 运行 GrpcInterfaceTest");
        log.info("4. Dubbo 接口: 运行 DubboInterfaceTest");
        log.info("5. Kafka 接口: 运行 KafkaInterfaceTest");
        log.info("6. RocketMQ 接口: 运行 RocketMQInterfaceTest");
        log.info("==========================================");
        log.info("运行所有测试: mvn test");
        log.info("或单独运行: mvn test -Dtest=*InterfaceTest");
        log.info("==========================================");
    }
}
