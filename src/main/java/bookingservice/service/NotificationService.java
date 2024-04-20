package bookingservice.service;

import bookingservice.dto.accommodation.AccommodationDto;
import bookingservice.dto.booking.BookingDto;
import bookingservice.model.Booking;
import bookingservice.model.Payment;

public interface NotificationService {
    void sendSuccessBookingMessage(BookingDto bookingDto);

    void sendCanceledBookingMessage(Booking booking);

    void sendCreatedAccommodationMessage(AccommodationDto accommodationDto);

    void sendDeletedAccommodationMessage(Long id);

    void sendSuccessfulPaymentMessage(Payment payment);

    void sendPaymentCancelledMessage(Long id);

    void sendNotExpiredBookingMessage();
}
