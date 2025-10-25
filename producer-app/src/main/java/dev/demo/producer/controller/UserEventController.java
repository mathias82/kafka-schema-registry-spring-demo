package dev.demo.producer.controller;

import dev.demo.avro.User;
import dev.demo.producer.dto.UserCreateRequest;
import dev.demo.producer.dto.UserResponse;
import dev.demo.producer.entities.GeneralResponse;
import dev.demo.producer.helper.CommonUtils;
import dev.demo.producer.mapper.UserMapper;
import dev.demo.producer.service.UserProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
