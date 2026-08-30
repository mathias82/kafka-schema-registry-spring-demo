package com.mathias.kafka.schema.consumer.listener;

import com.mathias.kafka.schema.User;
import com.mathias.kafka.schema.consumer.entities.UserEntity;
import com.mathias.kafka.schema.consumer.mapper.UserMapper;
import com.mathias.kafka.schema.consumer.repository.UserRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserListenerTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserListener listener;

    @Test
    void onMessage_reusesExistingDatabaseId_forDuplicateUserEvents() {
        User avroUser = User.newBuilder()
                .setId("u-100")
                .setEmail("ada@acme.com")
                .setPhone("2101234567")
                .setFirstName("Ada")
                .setLastName("Lovelace")
                .setIsActive(true)
                .setCreatedAt("2025-10-19T21:00:00Z")
                .setAge(28)
                .build();

        UserEntity mapped = new UserEntity();
        mapped.setUserId("u-100");
        mapped.setEmail("ada@acme.com");

        UserEntity existing = new UserEntity();
        existing.setId(42L);
        existing.setUserId("u-100");

        when(userMapper.toEntity(avroUser)).thenReturn(mapped);
        when(userRepository.findByUserId("u-100")).thenReturn(Optional.of(existing));
        when(userRepository.save(mapped)).thenAnswer(invocation -> invocation.getArgument(0));

        listener.onMessage(new ConsumerRecord<>("users.v1", 0, 7L, "u-100", avroUser));

        assertEquals(42L, mapped.getId());
        verify(userRepository).save(mapped);
    }
}
