package com.mathias.kafka.schema.producer.controller;

import com.mathias.kafka.schema.User;
import com.mathias.kafka.schema.producer.dto.UserCreateRequest;
import com.mathias.kafka.schema.producer.dto.UserResponse;
import com.mathias.kafka.schema.producer.entities.GeneralResponse;
import com.mathias.kafka.schema.producer.helper.CommonUtils;
import com.mathias.kafka.schema.producer.mapper.UserMapper;
import com.mathias.kafka.schema.producer.service.UserProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserEventController {

  private final UserProducerService userProducerService;
  private final UserMapper userMapper;

  @PostMapping
  public GeneralResponse<UserResponse> produceEvent(@RequestBody UserCreateRequest dto) {
    User avroUser = userProducerService.publishUser(dto);
    UserResponse response = userMapper.toResponse(avroUser);
    return CommonUtils.buildGeneralResponse(response);
  }
}
