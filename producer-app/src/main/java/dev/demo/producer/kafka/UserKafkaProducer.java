package dev.demo.producer.kafka;

import dev.demo.avro.User;
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
     *
     * @param user the User record to be sent
     */
    public void publish(User user) {
        log.info("Publishing User event [{}] to Kafka topic [{}]", user, topic);
        kafkaTemplate.send(topic,  user)
                .whenComplete((result, ex) -> handleSendResult(user, result, ex));
    }

    private void handleSendResult(User user, SendResult<String, User> result, Throwable ex) {
        if (ex != null) {
            log.error("Failed to publish User [{}] to Kafka topic [{}]: {}",  topic, ex.getMessage(), ex);
            return;
        }

        if (result == null || result.getRecordMetadata() == null) {
            log.info("Kafka send result was null for user [{}]", user.getId());
            return;
        }

        RecordMetadata metadata = result.getRecordMetadata();
        log.info("Published User [{}] → topic [{}], partition [{}], offset [{}]",
                user.getId(),
                metadata.topic(),
                metadata.partition(),
                metadata.offset());
    }



}
