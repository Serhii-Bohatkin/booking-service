package bookingservice.dto.booking;

import bookingservice.model.Booking;
import java.time.LocalDate;

public record BookingDto(
        Long id,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Long accommodationId,
        Long userId,
        Booking.Status status
) {
}
