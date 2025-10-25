package dev.demo.producer.config;

import org.springframework.context.annotation.*;
import org.springframework.kafka.core.*;
import dev.demo.avro.User;


@Configuration
public class KafkaCloudConfig {

    @Bean
    public KafkaTemplate<String, User> kafkaTemplate(ProducerFactory<String, User> pf) {
        return new KafkaTemplate<>(pf);
    }

}
