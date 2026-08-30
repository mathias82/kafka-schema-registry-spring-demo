package com.mathias.kafka.schema.producer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {

    @NotBlank
    private String id;

    @NotBlank
    @Email
    private String email;

    private String phone;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private String createdAt;

    @Min(0)
    private Integer age;
}
