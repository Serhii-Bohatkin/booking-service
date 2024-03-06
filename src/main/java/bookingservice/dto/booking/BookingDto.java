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
    public String toString() {
        return String.format("""
                        
                        Booking id: %s
                        Check in date: %s
                        Check out date: %s
                        Accommodation id: %s
                        User id: %s
                        Status: %s
                        """,
                id, checkInDate, checkOutDate, accommodationId, userId, status.name());
    }
}
