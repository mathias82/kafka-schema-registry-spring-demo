package com.mathias.kafka.schema.producer.service;

import com.mathias.kafka.schema.User;
import com.mathias.kafka.schema.producer.component.Validation;
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

    private final Validation validation;
    private final UserKafkaProducer userKafkaProducer;
    private final UserMapper userMapper;

    /**
     * Publishes a newly created {@link User} to Kafka.
     *
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Maps the incoming {@link UserCreateRequest} DTO to an Avro {@link User} object.</li>
     *   <li>Validates the user using custom domain validation rules.</li>
     *   <li>Publishes the user to Kafka via {@code userKafkaProducer}.</li>
     *   <li>Returns the mapped {@link User} object for further processing.</li>
     * </ol>
     *
     * @param dto the incoming user creation request
     * @return the mapped and published {@link User}
     *
     * @throws IllegalArgumentException if the user violates the Avro schema
     *         (wrapped {@link AvroRuntimeException})
     */
    public User publishUser(UserCreateRequest dto) {
        try {
            User user = userMapper.toAvro(dto);
            validation.validateAvroRecord(user);
            userKafkaProducer.publish(user);
            return user;
        } catch (AvroRuntimeException e) {
            throw new IllegalArgumentException(
                    "Record violates Avro schema: dev.demo.avro.User: " + e.getMessage(), e);
        }
    }

}
