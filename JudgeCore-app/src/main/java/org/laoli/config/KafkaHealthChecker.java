package org.laoli.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @Description Kafka 健康检查器（应用启动时自动检查）
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Component
@ConditionalOnExpression("'${spring.kafka.bootstrap-servers:192.168.10.232:9092}' != 'none'")
public class KafkaHealthChecker implements CommandLineRunner {
    
    @Value("${spring.kafka.bootstrap-servers:192.168.10.232:9092}")
    private String bootstrapServers;
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public KafkaHealthChecker(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @Override
    public void run(String... args) {
        log.info("==========================================");
        log.info("开始检查 Kafka 连接状态...");
        log.info("Kafka 服务器地址: {}", bootstrapServers);
        log.info("==========================================");
        
        // 方法1: 使用 AdminClient 检查连接
        checkKafkaWithAdminClient();
        
        // 方法2: 尝试发送测试消息
        checkKafkaWithProducer();
        
        log.info("==========================================");
    }
    
    /**
     * 使用 AdminClient 检查 Kafka 连接
     */
    private void checkKafkaWithAdminClient() {
        try {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
            props.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);
            // 解决主机名解析问题
            props.put("client.dns.lookup", "use_all_dns_ips");
            props.put("metadata.max.age.ms", "300000");
            
            try (AdminClient adminClient = AdminClient.create(props)) {
                ListTopicsResult topicsResult = adminClient.listTopics();
                int topicCount = topicsResult.names().get(5, TimeUnit.SECONDS).size();
                
                log.info("✅ Kafka 连接成功！");
                log.info("   服务器地址: {}", bootstrapServers);
                log.info("   可用主题数量: {}", topicCount);
                
            } catch (Exception e) {
                log.error("❌ Kafka AdminClient 连接失败: {}", e.getMessage());
                log.error("   错误类型: {}", e.getClass().getSimpleName());
                if (e.getCause() != null) {
                    log.error("   原因: {}", e.getCause().getMessage());
                }
                // 检查是否是主机名解析问题
                Throwable cause = e;
                while (cause != null) {
                    if (cause instanceof java.net.UnknownHostException) {
                        log.error("   解决方案: Kafka 服务器返回的主机名无法解析");
                        log.error("   建议: 1) 在 Kafka 服务器配置中使用 IP 地址作为 advertised.listeners");
                        log.error("         2) 或在客户端机器的 /etc/hosts 中添加主机名映射");
                        log.error("         3) 或使用 IP 地址作为 bootstrap-servers");
                        break;
                    }
                    cause = cause.getCause();
                }
            }
        } catch (Exception e) {
            log.error("❌ 创建 Kafka AdminClient 失败: {}", e.getMessage());
        }
    }
    
    /**
     * 使用 Producer 检查 Kafka 连接
     */
    private void checkKafkaWithProducer() {
        try {
            String testTopic = "judge-health-check";
            String testMessage = "health-check-" + System.currentTimeMillis();
            
            // 尝试发送消息（使用默认主题或健康检查主题）
            kafkaTemplate.send(testTopic, testMessage).get(5, TimeUnit.SECONDS);
            
            log.info("✅ Kafka Producer 测试成功！");
            log.info("   测试主题: {}", testTopic);
            log.info("   测试消息: {}", testMessage);
            
        } catch (org.apache.kafka.common.errors.TimeoutException e) {
            log.warn("⚠️  Kafka Producer 连接超时（可能主题不存在，但连接正常）");
            log.warn("   提示: 如果主题不存在，这是正常现象");
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("❌ Kafka Producer 连接超时: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Kafka Producer 连接失败: {}", e.getMessage());
            log.error("   错误类型: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.error("   原因: {}", e.getCause().getMessage());
            }
            // 检查是否是主机名解析问题
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof java.net.UnknownHostException) {
                    log.error("   解决方案: Kafka 服务器返回的主机名无法解析");
                    log.error("   建议: 1) 在 Kafka 服务器配置中使用 IP 地址作为 advertised.listeners");
                    log.error("         2) 或在客户端机器的 /etc/hosts 中添加主机名映射");
                    log.error("         3) 或使用 IP 地址作为 bootstrap-servers");
                    break;
                }
                cause = cause.getCause();
            }
        }
    }
}
