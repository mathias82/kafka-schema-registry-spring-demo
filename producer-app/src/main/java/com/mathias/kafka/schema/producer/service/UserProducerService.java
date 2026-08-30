package com.mathias.kafka.schema.producer.service;

import com.mathias.kafka.schema.User;
import com.mathias.kafka.schema.producer.dto.UserCreateRequest;
import com.mathias.kafka.schema.producer.kafka.UserKafkaProducer;
import com.mathias.kafka.schema.producer.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProducerService {

    private final UserKafkaProducer userKafkaProducer;
    private final UserMapper userMapper;

    /**
     * Maps the validated request to the generated Avro record and publishes it to Kafka.
     *
     * @param userCreateRequest validated incoming user creation request
     * @return the mapped Avro {@link User}
     */
    public User publishUser(UserCreateRequest userCreateRequest) {
        User user = userMapper.toAvro(userCreateRequest);
        userKafkaProducer.publish(user);
        return user;
    }
}
