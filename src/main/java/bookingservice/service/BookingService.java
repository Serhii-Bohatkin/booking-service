package bookingservice.service;

import bookingservice.dto.booking.BookingDto;
import bookingservice.dto.booking.BookingRequestDto;
import bookingservice.model.Booking;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingDto create(BookingRequestDto requestDto);

    List<BookingDto> getAllByUserIdAndStatus(Long userId, Booking.Status status, Pageable pageable);

    List<BookingDto> getAllForCurrentUser(Pageable pageable);

    BookingDto getById(Long id);

    BookingDto updateBooking(Long id, BookingRequestDto requestDto);

    void delete(Long id);

    void checkExpiredBooking();
}
