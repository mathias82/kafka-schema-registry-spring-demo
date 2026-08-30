package com.mathias.kafka.schema.consumer.listener;

import com.mathias.kafka.schema.User;
import com.mathias.kafka.schema.consumer.entities.UserEntity;
import com.mathias.kafka.schema.consumer.mapper.UserMapper;
import com.mathias.kafka.schema.consumer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserListener {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  @KafkaListener(topics = "${app.topic}", groupId = "${spring.kafka.consumer.group-id}")
  public void onMessage(ConsumerRecord<String, User> record) {
    User user = record.value();
    log.info("Received User [{}] from topic [{}], partition [{}], offset [{}]",
            user.getId(), record.topic(), record.partition(), record.offset());

    UserEntity entity = userMapper.toEntity(user);
    userRepository.findByUserId(entity.getUserId())
            .ifPresent(existing -> entity.setId(existing.getId()));

    UserEntity saved = userRepository.save(entity);
    log.info("Persisted User [{}] as database id [{}]", saved.getUserId(), saved.getId());
  }
}
