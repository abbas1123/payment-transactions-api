package io.github.abbas1123.payments.config;

import io.github.abbas1123.payments.service.TransactionEventPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionEventsTopic() {
        return TopicBuilder.name(TransactionEventPublisher.TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
