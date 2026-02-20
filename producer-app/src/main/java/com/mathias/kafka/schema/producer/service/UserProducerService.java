package com.mathias.kafka.schema.producer.service;

import com.mathias.kafka.schema.User;
import com.mathias.kafka.schema.producer.dto.UserCreateRequest;
import com.mathias.kafka.schema.producer.kafka.UserKafkaProducer;
import com.mathias.kafka.schema.producer.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.AvroRuntimeException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProducerService {

    private final UserKafkaProducer userKafkaProducer;
    private final UserMapper userMapper;

    /**
     * Publishes a newly created {@link User} to Kafka.
     *
     * <p>This method performs the following steps:
     * @param userCreateRequest the incoming user creation requestA\zs
     * @return the mapped and published {@link User}
     *
     * @throws IllegalArgumentException if the user violates the Avro schema
     *         (wrapped {@link AvroRuntimeException})
     */
    public User publishUser(UserCreateRequest userCreateRequest) {
        User user = userMapper.toAvro(userCreateRequest);
        userKafkaProducer.publish(user);
        return user;
    }

}
