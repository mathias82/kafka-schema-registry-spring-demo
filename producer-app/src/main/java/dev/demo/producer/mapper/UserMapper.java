package dev.demo.producer.mapper;

import dev.demo.avro.User;
import dev.demo.producer.dto.UserCreateRequest;
import dev.demo.producer.dto.UserResponse;
import org.mapstruct.*;
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
        return cs == null ? null : cs.toString();
    }

    @Named("defaultCreatedAt")
    static String defaultCreatedAt(String value) {
        return (value == null || value.isBlank())
                ? OffsetDateTime.now(ZoneOffset.UTC).toString()
                : value;
    }
}
