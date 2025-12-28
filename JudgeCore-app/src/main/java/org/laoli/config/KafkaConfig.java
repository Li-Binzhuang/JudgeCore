package org.laoli.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description Kafka 配置类（条件启用：当 bootstrap-servers 不是 "none" 时启用）
 * @Author laoli
 * @Date 2025/4/21
 */
@Slf4j
@Configuration
@ConditionalOnExpression("'${spring.kafka.bootstrap-servers:192.168.10.232:9092}' != 'none'")
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 解决主机名解析问题：优先使用 IP 地址
        configProps.put(ProducerConfig.CLIENT_DNS_LOOKUP_CONFIG, "use_all_dns_ips");
        // 增加元数据刷新间隔，减少连接尝试
        configProps.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 300000);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Value("${spring.kafka.consumer.group-id:judge-group}")
    private String groupId;
    
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // 解决主机名解析问题：优先使用 IP 地址
        props.put(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG, "use_all_dns_ips");
        // 增加元数据刷新间隔，减少连接尝试
        props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 300000);
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // 配置错误处理器：处理所有类型的错误（包括 rebalance 错误）
        factory.setCommonErrorHandler(new KafkaErrorHandler());
        
        return factory;
    }
}
