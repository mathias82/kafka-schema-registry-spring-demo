package com.mathias.kafka.schema.consumer.repository;

import com.mathias.kafka.schema.consumer.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
}
