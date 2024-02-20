package bookingservice.mapper;

import bookingservice.config.MapperConfig;
import bookingservice.dto.user.UserRegistrationRequestDto;
import bookingservice.dto.user.UserResponseDto;
import bookingservice.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserResponseDto userResponseDto);

    User toModel(UserRegistrationRequestDto userRegistrationDto);

    UserResponseDto toDto(User user);
}
