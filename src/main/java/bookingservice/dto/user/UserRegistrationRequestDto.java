package bookingservice.dto.user;

import bookingservice.validation.FieldsMatch;
import bookingservice.validation.Password;
import jakarta.validation.constraints.Email;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Accessors(chain = true)
@FieldsMatch(field = "password", fieldMatch = "repeatPassword",
        message = "Password and repeatPassword fields are not matching")
public record UserRegistrationRequestDto(
        @Email String email,
        @Password String password,
        String repeatPassword,
        @Length(min = 3, max = 20) String firstName,
        @Length(min = 3, max = 20) String lastName
) {
}
