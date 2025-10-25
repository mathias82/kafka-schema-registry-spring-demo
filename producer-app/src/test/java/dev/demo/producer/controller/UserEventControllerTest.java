package dev.demo.producer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.demo.avro.User;
import dev.demo.producer.component.Validation;
import dev.demo.producer.dto.UserCreateRequest;
import dev.demo.producer.dto.UserResponse;
import dev.demo.producer.exception.RestExceptionHandler;
import dev.demo.producer.mapper.UserMapper;
import dev.demo.producer.service.UserProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserEventControllerTest {
    @Mock
    KafkaTemplate<String, User> kafkaTemplate;
    @Mock
    UserMapper userMapper;
    @Mock
    UserProducerService userProducerService;
    @Mock
    Validation validation;
    @InjectMocks
    UserEventController controller;

    MockMvc mvc;
    ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void send_validUser_returns200_andSendsToKafka() throws Exception {

        var dto = getUserResponse("ada@acme.com");
        var avro = getAvro();
        var resp = getUserRequest();

        when(userProducerService.publishUser(eq(dto))).thenReturn(avro);
        when(userMapper.toResponse(eq(avro))).thenReturn(resp);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value("u-100"))
                .andExpect(jsonPath("$.data.email").value("ada@acme.com"));

        verify(userProducerService).publishUser(eq(dto));
        verify(userMapper).toResponse(eq(avro));
        verifyNoMoreInteractions(userProducerService, userMapper);
    }


    @Test
    void send_invalidUser_returns400_andDoesNotSend() throws Exception {

        var dto = getUserResponse(null);

        when(userProducerService.publishUser(eq(dto)))
                .thenThrow(new IllegalArgumentException("Record violates Avro schema: dev.demo.avro.User"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("violates Avro schema")));

        verify(userProducerService).publishUser(eq(dto));
        verifyNoInteractions(userMapper);
    }

    private UserCreateRequest getUserResponse(String email) {
        return UserCreateRequest.builder()
                .id("u-100")
                .email(email)
                .phone("2101234567")
                .firstName("Ada")
                .lastName("Lovelace")
                .isActive(true)
                .createdAt("2025-10-19T21:00:00Z")
                .age(28)
                .build();
    }

    private User getAvro() {
        return User.newBuilder()
                .setId("u-100")
                .setEmail("ada@acme.com")
                .setPhone("2101234567")
                .setFirstName("Ada")
                .setLastName("Lovelace")
                .setIsActive(true)
                .setCreatedAt("2025-10-19T21:00:00Z")
                .setAge(28)
                .build();
    }

    private UserResponse getUserRequest() {
        return UserResponse.builder()
                .id("u-100")
                .email("ada@acme.com")
                .phone("2101234567")
                .firstName("Ada")
                .lastName("Lovelace")
                .isActive(true)
                .createdAt("2025-10-19T21:00:00Z")
                .age(28)
                .build();
    }
}
