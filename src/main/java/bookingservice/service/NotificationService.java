package bookingservice.service;

import bookingservice.dto.accommodation.AccommodationDto;
import bookingservice.dto.booking.BookingDto;
import bookingservice.model.Booking;

public interface NotificationService {
    void sendSuccessBookingMessage(BookingDto bookingDto);

    void sendCanceledBookingMessage(Booking booking);

    void sendCreatedAccommodationMessage(AccommodationDto accommodationDto);

    void sendDeletedAccommodationMessage(Long id);
}
