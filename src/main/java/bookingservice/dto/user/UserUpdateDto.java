package bookingservice.dto.user;

import org.hibernate.validator.constraints.Length;

public record UserUpdateDto(
        @Length(min = 3, max = 20) String firstName,
        @Length(min = 3, max = 20) String lastName
) {
}
