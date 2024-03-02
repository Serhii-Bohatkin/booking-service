package bookingservice.dto.booking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BookingRequestDto(
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate,
        @Min(1) Long accommodationId
) {
}
