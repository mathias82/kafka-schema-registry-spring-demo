package com.mathias.kafka.schema.producer.mapper;

import com.mathias.kafka.schema.User;
import com.mathias.kafka.schema.producer.dto.UserCreateRequest;
import com.mathias.kafka.schema.producer.dto.UserResponse;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "defaultCreatedAt")
    User toAvro(UserCreateRequest dto);

    @Mapping(target = "id",        source = "id",        qualifiedByName = "toStringSafe")
    @Mapping(target = "email",     source = "email",     qualifiedByName = "toStringSafe")
    @Mapping(target = "phone",     source = "phone",     qualifiedByName = "toStringSafe")
    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "toStringSafe")
    @Mapping(target = "lastName",  source = "lastName",  qualifiedByName = "toStringSafe")
    UserResponse toResponse(User user);

    @Named("toStringSafe")
    static String toStringSafe(CharSequence cs) {
        return StringUtils.isEmpty(cs) ? null : cs.toString();
    }

    @Named("defaultCreatedAt")
    static String defaultCreatedAt(String value) {
        return StringUtils.isBlank(value)
                ? OffsetDateTime.now(ZoneOffset.UTC).toString()
                : value;
    }
}
