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
import bookingservice.service.NotificationService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final AccommodationService accommodationService;
    private final NotificationService notificationService;

    @Override
    public BookingDto create(BookingRequestDto requestDto) {
        if (ChronoUnit.DAYS.between(requestDto.checkInDate(), requestDto.checkOutDate()) <= 0) {
            throw new BookingException("Unsuccessful booking, "
                    + "check-in date must be earlier than check-out date");
        }
        if (!checkAvailableAccommodation(requestDto)) {
            throw new BookingException("Unsuccessful booking, all accommodation are occupied");
        }
        Booking booking = bookingMapper.toModel(requestDto);
        booking.setUser(getCurrentUser());
        booking.setStatus(Booking.Status.PENDING);
        BookingDto bookingDto = bookingMapper.toDto(bookingRepository.save(booking));
        notificationService.sendSuccessBookingMessage(bookingDto);
        return bookingDto;
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
    public void delete(Long id) {
        Booking booking = bookingMapper.toModel(getBookingDtoForCurrentUserById(id));
        if (!new BookingPredicate().test(booking)) {
            throw new BookingException("The booking with id " + id + "has already been canceled or has expired");
        }
        booking.setStatus(Booking.Status.CANCELED);
        Booking cancelledBooking = bookingRepository.save(booking);
        notificationService.sendCanceledBookingMessage(cancelledBooking);
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Override
    public void checkExpiredBooking() {
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCheckOutDateLessThanEqual(
                Booking.Status.CONFIRMED, LocalDate.now());
        if (expiredBookings.isEmpty()) {
            notificationService.sendNotExpiredBookingMessage();
        } else {
            expiredBookings.forEach(booking -> {
                booking.setStatus(Booking.Status.EXPIRED);
                bookingRepository.save(booking);
            });
        }
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
