package bookingservice.service;

import static org.mockito.Mockito.verify;

import bookingservice.config.BotConfig;
import bookingservice.dto.accommodation.AccommodationDto;
import bookingservice.dto.booking.BookingDto;
import bookingservice.model.Accommodation;
import bookingservice.model.Booking;
import bookingservice.model.Payment;
import bookingservice.model.User;
import bookingservice.service.impl.TelegramNotificationServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TelegramNotificationServiceImplTest {
    @Mock
    private BotConfig botConfig;
    @InjectMocks
    private TelegramNotificationServiceImpl notificationService;

    @Test
    public void sendSuccessBookingMessage_Ok() {
        BookingDto bookingDto = new BookingDto(
                1L,
                LocalDate.of(2024, 4, 22),
                LocalDate.of(2024, 4, 23),
                1L,
                1L,
                Booking.Status.CONFIRMED);

        notificationService.sendSuccessBookingMessage(bookingDto);
        verify(botConfig).getAllowedChatIds();
    }

    @Test
    public void sendCanceledBookingMessage_Ok() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setAccommodation(new Accommodation());
        booking.setUser(new User());
        booking.setStatus(Booking.Status.CONFIRMED);

        notificationService.sendCanceledBookingMessage(booking);
        verify(botConfig).getAllowedChatIds();
    }

    @Test
    public void sendCreatedAccommodationMessage_Ok() {
        AccommodationDto accommodationDto = new AccommodationDto(
                1L,
                Accommodation.Type.HOUSE,
                "Lviv, Shevchenko St. 12",
                "Studio, 1 Bedroom",
                List.of("golf courses", "health club facilities"),
                BigDecimal.valueOf(15),
                2
        );
        notificationService.sendCreatedAccommodationMessage(accommodationDto);
        verify(botConfig).getAllowedChatIds();
    }

    @Test
    public void sendDeletedAccommodationMessage_Ok() {
        notificationService.sendDeletedAccommodationMessage(1L);
        verify(botConfig).getAllowedChatIds();
    }

    @Test
    public void sendSuccessfulPaymentMessage_Ok() {
        Payment payment = new Payment();
        payment.setBooking(new Booking());

        notificationService.sendSuccessfulPaymentMessage(payment);
        verify(botConfig).getAllowedChatIds();
    }

    @Test
    public void sendSuccessfulPaymentMessage_sendPaymentCancelledMessage() {
        notificationService.sendPaymentCancelledMessage(1L);
        verify(botConfig).getAllowedChatIds();
    }

    @Test
    public void sendNotExpiredBookingMessage_Ok() {
        notificationService.sendNotExpiredBookingMessage();
        verify(botConfig).getAllowedChatIds();
    }
}
