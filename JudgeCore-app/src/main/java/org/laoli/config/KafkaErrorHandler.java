package org.laoli.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.KafkaException;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.util.backoff.FixedBackOff;

/**
 * @Description Kafka 错误处理器（处理所有类型的错误，包括 rebalance 错误）
 * @Author laoli
 * @Date 2025/12/28
 */
@Slf4j
public class KafkaErrorHandler extends DefaultErrorHandler {
    
    public KafkaErrorHandler() {
        // 使用 FixedBackOff：重试间隔 1 秒，最多重试 3 次
        super(new FixedBackOff(1000L, 3L));
    }
    
    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer, 
            MessageListenerContainer container, boolean committed) {
        // 处理非消息错误（如 rebalance 错误、连接错误等）
        if (thrownException instanceof KafkaException) {
            log.warn("Kafka infrastructure error (rebalance, connection, etc.): {}", 
                    thrownException.getMessage());
            // 对于 KafkaException（rebalance 错误），不调用父类方法
            // 因为 DefaultErrorHandler 会抛出 IllegalStateException
            // 直接返回，让容器继续运行并尝试自动恢复
            return;
        } else {
            log.error("Unexpected error in Kafka listener container", thrownException);
            // 对于其他异常，可以尝试调用父类方法
            try {
                super.handleOtherException(thrownException, consumer, container, committed);
            } catch (IllegalStateException e) {
                // 如果父类也抛出异常，记录日志但不重新抛出
                log.warn("Parent error handler cannot process exception: {}", e.getMessage());
            }
        }
    }
}

