package bookingservice.service.impl;

import bookingservice.dto.booking.BookingDto;
import bookingservice.dto.booking.BookingRequestDto;
import bookingservice.exception.BookingException;
import bookingservice.exception.EntityNotFoundException;
import bookingservice.mapper.BookingMapper;
import bookingservice.model.Booking;
import bookingservice.model.User;
import bookingservice.repository.BookingRepository;
import bookingservice.service.AccommodationService;
import bookingservice.service.BookingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final AccommodationService accommodationService;

    @Override
    public BookingDto create(BookingRequestDto requestDto) {
        if (!checkAvailableAccommodation(requestDto)) {
            throw new BookingException("Unsuccessful booking, all accommodation are occupied");
        }
        Booking booking = bookingMapper.toModel(requestDto);
        booking.setUser(getCurrentUser());
        booking.setStatus(Booking.Status.PENDING);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public List<BookingDto> getAllByUserIdAndStatus(
            Long userId,
            Booking.Status status,
            Pageable pageable
    ) {
        if (userId == null && status == null) {
            return bookingRepository.findAll(pageable).stream()
                    .map(bookingMapper::toDto)
                    .toList();
        }
        if (status == null) {
            return bookingRepository.findAllByUserId(userId, pageable).stream()
                    .map(bookingMapper::toDto)
                    .toList();
        }
        return bookingRepository.findAllByUserIdAndStatus(userId, status, pageable).stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> getAllForCurrentUser(Pageable pageable) {
        return bookingRepository.findAllByUserId(getCurrentUser().getId(), pageable)
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public BookingDto getById(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(
                        "Booking with id " + id + " not found!"));
        return bookingMapper.toDto(booking);
    }

    @Override
    public BookingDto updateBooking(Long id, BookingRequestDto requestDto) {
        Booking booking = bookingMapper.toModel(getBookingDtoForCurrentUserById(id));
        if (!checkAvailableAccommodation(requestDto)) {
            throw new BookingException("Unsuccessful booking, all accommodation are occupied");
        }
        booking.setCheckInDate(requestDto.checkInDate());
        booking.setCheckOutDate(requestDto.checkOutDate());
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public void cancelBooking(Long id) {
        Booking booking = bookingMapper.toModel(getBookingDtoForCurrentUserById(id));
        if (!new BookingPredicate().test(booking)) {
            throw new BookingException("The booking has already been canceled or has expired");
        }
        booking.setStatus(Booking.Status.CANCELED);
        bookingRepository.save(booking);
    }

    @Override
    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }

    private BookingDto getBookingDtoForCurrentUserById(Long id) {
        return getAllForCurrentUser(Pageable.unpaged()).stream()
                .filter(bookingDto -> bookingDto.id().equals(id))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Booking with id " + id + " not found!"));
    }

    private boolean checkAvailableAccommodation(BookingRequestDto requestDto) {
        BookingPredicate bookingPredicate = new BookingPredicate();
        long numberOfExistingBookings = bookingRepository.findAllByCheckOutDateBetween(
                        requestDto.accommodationId(),
                        requestDto.checkInDate(),
                        requestDto.checkOutDate()
                )
                .stream()
                .filter(bookingPredicate)
                .count();
        Integer availability = accommodationService.getById(requestDto.accommodationId())
                .availability();
        return availability - numberOfExistingBookings >= 1;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
