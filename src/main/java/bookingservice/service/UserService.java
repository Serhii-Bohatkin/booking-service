package bookingservice.service;

import bookingservice.dto.user.UserRegistrationRequestDto;
import bookingservice.dto.user.UserResponseDto;
import bookingservice.dto.user.UserUpdateDto;
import bookingservice.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserResponseDto addRole(String email, Long roleId);

    UserResponseDto getCurrentUserInfo();

    UserResponseDto update(UserUpdateDto updateDto);
}
