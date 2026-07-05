package io.github.abbas1123.payments.service;

import io.github.abbas1123.payments.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Downstream consumer of the transaction event stream.
 * Stands in for a real notification channel (push/SMS/e-mail):
 * the point of the demo is the decoupling through Kafka.
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = TransactionEventPublisher.TOPIC, groupId = "notification-service")
    public void onTransactionEvent(TransactionEvent event) {
        log.info("Notification: account {} sent {} (commission {}) to account {} — tx {} [{}]",
                event.fromAccountId(), event.amount(), event.commission(),
                event.toAccountId(), event.transactionId(), event.status());
    }
}
