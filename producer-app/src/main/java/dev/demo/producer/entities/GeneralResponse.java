package dev.demo.producer.entities;

import dev.demo.producer.enums.GeneralResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralResponse<T> {
    private GeneralResponseStatus status;
    private T data;
}
