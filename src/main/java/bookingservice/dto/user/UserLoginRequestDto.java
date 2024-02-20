package bookingservice.dto.user;

import bookingservice.validation.Email;
import bookingservice.validation.Password;

public record UserLoginRequestDto(
        @Email String email,
        @Password String password
) {
}
