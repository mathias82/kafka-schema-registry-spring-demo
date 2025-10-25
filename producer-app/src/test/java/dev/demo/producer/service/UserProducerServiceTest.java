package dev.demo.producer.service;

import dev.demo.avro.User;
import dev.demo.producer.component.Validation;
import dev.demo.producer.dto.UserCreateRequest;
import dev.demo.producer.kafka.UserKafkaProducer;
import dev.demo.producer.mapper.UserMapper;
import org.apache.avro.AvroRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProducerServiceTest {

    @Mock
    Validation validation;

    @Mock
    UserKafkaProducer userKafkaProducer;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserProducerService userProducerService;

    @Test
    void publishUser_success() {

        UserCreateRequest dto = new UserCreateRequest();
        User avroUser = new User();

        when(userMapper.toAvro(dto)).thenReturn(avroUser);

        // when
        User result = userProducerService.publishUser(dto);

        // then
        assertEquals(avroUser, result);
        verify(validation).validateAvroRecord(avroUser);
        verify(userKafkaProducer).publish(avroUser);
    }

    @Test
    void publishUser_avroViolation_throwsIllegalArgument() {

        UserCreateRequest dto = new UserCreateRequest();
        when(userMapper.toAvro(dto)).thenThrow(new AvroRuntimeException("bad schema"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userProducerService.publishUser(dto));

        assertTrue(ex.getMessage().contains("Record violates Avro schema"));
    }
}
