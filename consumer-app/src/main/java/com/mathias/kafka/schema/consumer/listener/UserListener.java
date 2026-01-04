package com.mathias.kafka.schema.consumer.listener;

import com.mathias.kafka.schema.User;
import com.mathias.kafka.schema.consumer.mapper.UserMapper;
import com.mathias.kafka.schema.consumer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserListener {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @KafkaListener(topics = "${app.topic}", groupId = "user-svc")
  public void onMessage(ConsumerRecord<String, User> rec) {
    log.info("Received key=" + rec.key() + " value=" + rec.value());
    userRepository.save(userMapper.toEntity(rec.value()));
    log.info("Successfully saved");
  }
}

