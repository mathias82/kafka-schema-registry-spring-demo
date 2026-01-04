package com.mathias.kafka.schema.producer.config;

import com.mathias.kafka.schema.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;


@Configuration
public class KafkaCloudConfig {

    @Bean
    public KafkaTemplate<String, User> kafkaTemplate(ProducerFactory<String, User> pf) {
        return new KafkaTemplate<>(pf);
    }

}
