package dev.demo.consumer.mapper;

import dev.demo.avro.User;
import dev.demo.consumer.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId",   source = "id", qualifiedByName = "toStringSafe")
    @Mapping(target = "active",   source = "isActive")
    UserEntity toEntity(User avro);

    @Named("toStringSafe")
    static String toStringSafe(CharSequence cs) {
        return cs == null ? null : cs.toString();
    }
}
