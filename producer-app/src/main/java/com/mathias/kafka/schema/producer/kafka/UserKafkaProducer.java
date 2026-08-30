package com.mathias.kafka.schema.producer.kafka;

import com.mathias.kafka.schema.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserKafkaProducer {

    private final KafkaTemplate<String, User> kafkaTemplate;

    @Value("${app.topic}")
    private String topic;

    /**
     * Publishes a User Avro record to the configured Kafka topic.
     * The logical user id is used as the Kafka key so events for the same user
     * are routed consistently to the same partition.
     *
     * @param user the User record to be sent
     */
    public void publish(User user) {
        String key = user.getId();
        log.info("Publishing User event id=[{}] to Kafka topic [{}]", key, topic);

        kafkaTemplate.send(topic, key, user)
                .whenComplete((result, ex) -> handleSendResult(user, result, ex));
    }

    private void handleSendResult(User user, SendResult<String, User> result, Throwable ex) {
        if (ex != null) {
            log.error("Failed to publish User [{}] to Kafka topic [{}]", user.getId(), topic, ex);
            return;
        }

        if (result == null || result.getRecordMetadata() == null) {
            log.warn("Kafka send completed without record metadata for User [{}]", user.getId());
            return;
        }

        RecordMetadata metadata = result.getRecordMetadata();
        log.info("Published User [{}] -> topic [{}], partition [{}], offset [{}]",
                user.getId(),
                metadata.topic(),
                metadata.partition(),
                metadata.offset());
    }
}
