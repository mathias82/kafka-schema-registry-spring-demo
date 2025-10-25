package dev.demo.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private String id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private String createdAt;
    private Integer age;
}
