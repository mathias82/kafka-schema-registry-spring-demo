package com.mathias.kafka.schema.producer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathias.kafka.schema.User;
import com.mathias.kafka.schema.producer.dto.UserCreateRequest;
import com.mathias.kafka.schema.producer.dto.UserResponse;
import com.mathias.kafka.schema.producer.exception.RestExceptionHandler;
import com.mathias.kafka.schema.producer.mapper.UserMapper;
import com.mathias.kafka.schema.producer.service.UserProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserEventControllerTest {

    @Mock
    UserMapper userMapper;

    @Mock
    UserProducerService userProducerService;

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
        var dto = getUserRequest("ada@acme.com");
        var avro = getAvro();
        var resp = getUserResponse();

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
    void send_invalidUser_returns400_andDoesNotPublish() throws Exception {
        var dto = getUserRequest(null);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        verifyNoInteractions(userProducerService, userMapper);
    }

    private UserCreateRequest getUserRequest(String email) {
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

    private UserResponse getUserResponse() {
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
