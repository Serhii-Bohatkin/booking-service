package bookingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Pageable.unpaged;

import bookingservice.dto.accommodation.AccommodationDto;
import bookingservice.dto.booking.BookingDto;
import bookingservice.dto.booking.BookingRequestDto;
import bookingservice.exception.BookingException;
import bookingservice.exception.EntityNotFoundException;
import bookingservice.mapper.BookingMapper;
import bookingservice.model.Accommodation;
import bookingservice.model.Booking;
import bookingservice.model.User;
import bookingservice.repository.BookingRepository;
import bookingservice.repository.UserRepository;
import bookingservice.service.impl.BookingServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    public static final long USER_ID = 1L;
    public static final long BOOKING_ID = 1L;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private AccommodationService accommodationService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    public void create_ValidRequestDto_ShouldReturnBookingDto() {

        BookingDto expected = createBookingDto();
        Booking booking = createBooking();
        BookingRequestDto requestDto = createBookingRequestDto();

        when(bookingMapper.toDto(booking)).thenReturn(expected);
        when(bookingRepository.findAllByCheckOutDateBetween(1L,
                LocalDate.of(2024, 4, 22),
                LocalDate.of(2024, 4, 23))).thenReturn(List.of(booking));
        when(bookingMapper.toModel(requestDto)).thenReturn(booking);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(createUser()));
        setupSecurityContext();
        when(accommodationService.getById(1L)).thenReturn(createAccommodationDto());
        when(bookingRepository.save(booking)).thenReturn(booking);
        doNothing().when(notificationService).sendSuccessBookingMessage(expected);

        BookingDto actual = bookingService.create(requestDto);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void create_NotAvailableAccommodation_ShouldThrowException() {
        when(bookingRepository.findAllByCheckOutDateBetween(1L,
                LocalDate.of(2024, 4, 22),
                LocalDate.of(2024, 4, 23))).thenReturn(List.of(createBooking()));
        when(accommodationService.getById(1L)).thenReturn(createSecondAccommodationDto());

        assertThatExceptionOfType(BookingException.class)
                .isThrownBy(() -> bookingService.create(createBookingRequestDto()))
                .withMessage("Unsuccessful booking, all accommodation are occupied");
    }

    @Test
    public void create_InvalidDatesInRequestDto_ShouldThrowException() {
        BookingRequestDto requestDto = new BookingRequestDto(
                LocalDate.of(2024, 4, 22),
                LocalDate.of(2024, 4, 22),
                1L);

        assertThatExceptionOfType(BookingException.class)
                .isThrownBy(() -> bookingService.create(requestDto))
                .withMessage("Unsuccessful booking, "
                        + "check-in date must be earlier than check-out date");
    }

    @Test
    public void getAllByUserIdAndStatus_WithUserIdAndWithStatus_ShouldReturnListOfBooking() {
        Booking booking = createBooking();
        List<Booking> bookingList = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookingList, unpaged(), bookingList.size());
        BookingDto expected = createBookingDto();

        when(bookingRepository.findAllByUserIdAndStatus(
                USER_ID, Booking.Status.CONFIRMED, unpaged())).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(expected);

        List<BookingDto> actual = bookingService.getAllByUserIdAndStatus(
                USER_ID, Booking.Status.CONFIRMED, unpaged());
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    @Test
    public void getAllByUserIdAndStatus_WithUserIdAndWithoutStatus_ShouldReturnListOfBooking() {
        Booking booking = createBooking();
        List<Booking> bookingList = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookingList, unpaged(), bookingList.size());
        BookingDto expected = createBookingDto();

        when(bookingRepository.findAllByUserId(USER_ID, unpaged())).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(expected);

        List<BookingDto> actual = bookingService.getAllByUserIdAndStatus(
                USER_ID, null, unpaged());
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    @Test
    public void getAllByUserIdAndStatus_WithoutUserIdAndWithoutStatus_ShouldReturnListOfBooking() {
        Booking booking = createBooking();
        List<Booking> bookingList = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookingList, unpaged(), bookingList.size());
        BookingDto expected = createBookingDto();

        when(bookingRepository.findAll(unpaged())).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(expected);

        List<BookingDto> actual = bookingService.getAllByUserIdAndStatus(
                null, null, unpaged());
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    @Test
    public void getAllForCurrentUser_ShouldReturnListOfBookingDto() {
        Booking booking = createBooking();
        List<Booking> bookingList = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookingList, unpaged(), bookingList.size());
        BookingDto expected = createBookingDto();

        setupSecurityContext();
        when(bookingRepository.findAllByUserId(USER_ID, unpaged())).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(expected);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(createUser()));

        List<BookingDto> actual = bookingService.getAllForCurrentUser(unpaged());
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    @Test
    public void getById_ExistBookingId_ShouldReturnBookingDto() {
        Booking booking = createBooking();
        BookingDto expected = createBookingDto();

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(expected);

        BookingDto actual = bookingService.getById(BOOKING_ID);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getById_NonExistingBookingId_ShouldThrowException() {
        when(bookingRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> bookingService.getById(Long.MAX_VALUE))
                .withMessage("Booking with id " + Long.MAX_VALUE + " not found!");
    }

    @Test
    public void updateBooking_WithExistBookingId_ShouldReturnBookingDto() {
        Booking booking = createBooking();
        List<Booking> bookingList = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookingList, unpaged(), bookingList.size());
        BookingDto expected = createBookingDto();

        setupSecurityContext();
        when(bookingRepository.findAllByUserId(USER_ID, unpaged())).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(expected);
        when(bookingRepository.findAllByCheckOutDateBetween(1L,
                LocalDate.of(2024, 4, 22),
                LocalDate.of(2024, 4, 23))).thenReturn(bookingList);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(createUser()));
        when(accommodationService.getById(1L)).thenReturn(createAccommodationDto());
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(expected);
        when(bookingMapper.toModel(expected)).thenReturn(booking);

        BookingDto actual = bookingService.updateBooking(BOOKING_ID, createBookingRequestDto());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void updateBooking_NonExistingBookingId_ShouldThrowException() {
        setupSecurityContext();
        when(bookingRepository.findAllByUserId(USER_ID, unpaged())).thenReturn(Page.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(createUser()));

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> bookingService.updateBooking(
                        Long.MAX_VALUE, createBookingRequestDto()))
                .withMessage("Booking with id " + Long.MAX_VALUE + " not found!");
    }

    @Test
    public void updateBooking_NotAvailableAccommodation_ShouldThrowException() {
        Booking booking = createBooking();
        List<Booking> bookingList = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookingList, unpaged(), bookingList.size());
        BookingDto bookingDto = createBookingDto();
        AccommodationDto accommodationDto = createSecondAccommodationDto();

        when(bookingRepository.findAllByUserId(USER_ID, unpaged())).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);
        when(accommodationService.getById(1L)).thenReturn(accommodationDto);
        setupSecurityContext();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(createUser()));
        when(bookingMapper.toModel(bookingDto)).thenReturn(booking);
        when(bookingRepository.findAllByCheckOutDateBetween(1L,
                LocalDate.of(2024, 4, 22),
                LocalDate.of(2024, 4, 23))).thenReturn(bookingList);

        assertThatExceptionOfType(BookingException.class)
                .isThrownBy(() -> bookingService.updateBooking(
                        BOOKING_ID, createBookingRequestDto()))
                .withMessage("Unsuccessful booking, all accommodation are occupied");
    }

    @Test
    public void delete_ValidId_Success() {
        BookingDto bookingDto = createBookingDto();
        Booking booking = createBooking();
        List<Booking> bookingList = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookingList, unpaged(), bookingList.size());

        setupSecurityContext();
        when(bookingMapper.toModel(bookingDto)).thenReturn(booking);
        when(bookingRepository.findAllByUserId(USER_ID, unpaged())).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(createUser()));

        bookingService.delete(BOOKING_ID);

        assertThat(Booking.Status.CANCELED).isEqualTo(booking.getStatus());
        verify(bookingRepository, times(1)).save(booking);
        verify(notificationService, times(1)).sendCanceledBookingMessage(any());
    }

    @Test
    public void delete_NonExistingId_ShouldThrowException() {
        setupSecurityContext();
        when(bookingRepository.findAllByUserId(USER_ID, unpaged())).thenReturn(Page.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(createUser()));

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> bookingService.delete(Long.MAX_VALUE))
                .withMessage("Booking with id " + Long.MAX_VALUE + " not found!");
    }

    @Test
    public void delete_BookingAlreadyCanceled_ShouldThrowException() {
        BookingDto bookingDto = createBookingDto();
        Booking booking = createBooking();
        booking.setStatus(Booking.Status.CANCELED);
        List<Booking> bookingList = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookingList, unpaged(), bookingList.size());

        setupSecurityContext();
        when(bookingMapper.toModel(bookingDto)).thenReturn(booking);
        when(bookingRepository.findAllByUserId(USER_ID, unpaged())).thenReturn(bookingPage);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(createUser()));

        assertThatExceptionOfType(BookingException.class)
                .isThrownBy(() -> bookingService.delete(BOOKING_ID))
                .withMessage("The booking with id " + BOOKING_ID
                        + "has already been canceled or has expired");
    }

    @Test
    public void checkExpiredBooking_NotExpired_sendMessageToTelegram() {
        when(bookingRepository.findByStatusAndCheckOutDateLessThanEqual(
                Booking.Status.CONFIRMED, LocalDate.now())).thenReturn(Collections.emptyList());

        bookingService.checkExpiredBooking();

        verify(notificationService).sendNotExpiredBookingMessage();
        verify(bookingRepository, never()).saveAll(Collections.emptyList());
    }

    @Test
    public void checkExpiredBooking_Expired_markBookingAsExpired() {
        Booking booking = createBooking();
        booking.setCheckOutDate(LocalDate.now().minusDays(1));

        when(bookingRepository.findByStatusAndCheckOutDateLessThanEqual(
                Booking.Status.CONFIRMED, LocalDate.now())).thenReturn(List.of(booking));

        bookingService.checkExpiredBooking();
        verify(bookingRepository).save(booking);
        verify(notificationService, never()).sendNotExpiredBookingMessage();
    }

    private Booking createBooking() {
        Booking booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setCheckInDate(LocalDate.of(2024, 4, 22));
        booking.setCheckOutDate(LocalDate.of(2024, 4, 23));
        booking.setAccommodation(createAccommodation());
        booking.setUser(createUser());
        booking.setStatus(Booking.Status.CONFIRMED);
        return booking;
    }

    private BookingRequestDto createBookingRequestDto() {
        return new BookingRequestDto(
                LocalDate.of(2024, 4, 22),
                LocalDate.of(2024, 4, 23),
                1L
        );
    }

    private BookingDto createBookingDto() {
        return new BookingDto(
                BOOKING_ID,
                LocalDate.of(2024, 4, 22),
                LocalDate.of(2024, 4, 23),
                1L,
                USER_ID,
                Booking.Status.CONFIRMED);
    }

    private AccommodationDto createAccommodationDto() {
        return new AccommodationDto(
                1L,
                Accommodation.Type.HOUSE,
                "Lviv, Shevchenko St. 12",
                "Studio, 1 Bedroom",
                List.of("golf courses", "health club facilities"),
                BigDecimal.valueOf(15),
                2
        );
    }

    private AccommodationDto createSecondAccommodationDto() {
        return new AccommodationDto(
                1L,
                Accommodation.Type.HOUSE,
                "Lviv, Shevchenko St. 12",
                "Studio, 1 Bedroom",
                List.of("golf courses", "health club facilities"),
                BigDecimal.valueOf(15),
                1
        );
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("admin@gmail.com");
        user.setFirstName("Jhon");
        user.setLastName("Doe");
        user.setPassword("@g_sJ'#_$ks%1Nq");
        return user;
    }

    private Accommodation createAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.HOUSE);
        accommodation.setLocation("Lviv, Shevchenko St. 12");
        accommodation.setSize("Studio, 1 Bedroom");
        accommodation.setAmenities(List.of("golf courses", "health club facilities"));
        accommodation.setDailyRate(BigDecimal.valueOf(15));
        accommodation.setAvailability(2);
        return accommodation;
    }

    private void setupSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
