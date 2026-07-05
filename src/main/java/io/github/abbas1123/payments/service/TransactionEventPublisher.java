package io.github.abbas1123.payments.service;

import io.github.abbas1123.payments.dto.TransferResponse;
import io.github.abbas1123.payments.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TransactionEventPublisher {

    public static final String TOPIC = "transaction-events";

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public TransactionEventPublisher(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCompleted(TransferResponse transfer) {
        TransactionEvent event = new TransactionEvent(
                transfer.transactionId(),
                transfer.fromAccountId(),
                transfer.toAccountId(),
                transfer.amount(),
                transfer.commission(),
                transfer.status(),
                Instant.now());

        // Key by source account so events for one account stay ordered within a partition.
        kafkaTemplate.send(TOPIC, String.valueOf(event.fromAccountId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish transaction event {}", event.transactionId(), ex);
                    } else {
                        log.debug("Published transaction event {} to partition {}",
                                event.transactionId(), result.getRecordMetadata().partition());
                    }
                });
    }
}
