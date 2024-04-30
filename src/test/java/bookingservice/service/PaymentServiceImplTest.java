package bookingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Pageable.unpaged;

import bookingservice.dto.payment.PaymentDto;
import bookingservice.exception.EntityNotFoundException;
import bookingservice.mapper.PaymentMapper;
import bookingservice.model.Accommodation;
import bookingservice.model.Booking;
import bookingservice.model.Payment;
import bookingservice.model.Role;
import bookingservice.model.User;
import bookingservice.repository.PaymentRepository;
import bookingservice.service.impl.PaymentServiceImpl;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class PaymentServiceImplTest {
    public static final long USER_ID = 1L;
    public static final long BOOKING_ID = 1L;
    public static final long PAYMENT_ID = 1L;
    public static final String URL = "https://checkout.stripe.com/c/pay/cs_test_a1HtKE"
            + "xKKHk1mxKCaqNaj40DkOI9jzLtnshjeFuzjfZovyn7R0vUaOXQxN#fidkdWxOYHwnPyd1blpx"
            + "YHZxWjA0SnBGX0s1N0NmUz1WYWFoTTNLX1EwR2dJVGpxa1FnPHdKSkNHdDxrSG9zXUh%2FSnR"
            + "BZ2NJSWhnQFNTdzNvPXNWSX03YGpXMzx%2FbkRcYEg9V2NGTlw9RjVJNTV8cXU9c2EyNScpJ2N"
            + "3amhWYHdzYHcnP3F3cGApJ2 lkfGpwcVF8dWAnPyd2bGtiaWBabHFgaCcpJ2BrZGdpYFVpZGZg"
            + "bWppYWB3dic%2FcXdwYHgl";
    public static final String SESSION_ID
            = "cs_test_a1HtKExKKHk1mxKCaqNaj40DkOI9jzLtnshjeFuzjfZovyn7R0vUaOXQxN";
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    public void getPaymentsForUser_UserHasRoleAdmin_ShouldReturnPaymentDtoList() {
        Payment payment = createPayment();
        List<Payment> paymentList = List.of(payment);
        Page<Payment> payments = new PageImpl<>(paymentList, unpaged(), paymentList.size());
        PaymentDto expected = createPaymentDto();
        User user = createUser();
        user.getRoles().add(createRoleAdmin());

        when(authentication.getPrincipal()).thenReturn(user);
        when(paymentMapper.toDto(payment)).thenReturn(expected);
        when(paymentRepository.findAllByBookingUserId(USER_ID, unpaged())).thenReturn(payments);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        List<PaymentDto> actual = paymentService.getPaymentsForUser(USER_ID, unpaged());
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    @Test
    public void getPaymentsForUser_UserRequestsHisPayments_ShouldReturnPaymentDtoList() {
        Payment payment = createPayment();
        List<Payment> paymentList = List.of(payment);
        Page<Payment> payments = new PageImpl<>(paymentList, unpaged(), paymentList.size());
        PaymentDto expected = createPaymentDto();

        when(paymentMapper.toDto(payment)).thenReturn(expected);
        when(paymentRepository.findAllByBookingUserId(USER_ID, unpaged())).thenReturn(payments);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(createUser());
        SecurityContextHolder.setContext(securityContext);

        List<PaymentDto> actual = paymentService.getPaymentsForUser(USER_ID, unpaged());
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    @Test
    public void getPaymentsForUser_UserRequestsPaymentsThatNotHisOwn_Exception() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(createUser());
        SecurityContextHolder.setContext(securityContext);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> paymentService.getPaymentsForUser(Long.MAX_VALUE, unpaged()))
                .withMessage("No payments found for user with id " + Long.MAX_VALUE);

    }

    private User createUser() {
        Set<Role> roles = new HashSet<>();
        roles.add(createRoleUser());

        User user = new User();
        user.setId(USER_ID);
        user.setEmail("admin@gmail.com");
        user.setFirstName("Jhon");
        user.setLastName("Doe");
        user.setPassword("@g_sJ'#_$ks%1Nq");
        user.setRoles(roles);
        return user;
    }

    private Role createRoleUser() {
        Role roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setName(Role.RoleName.USER);
        return roleUser;
    }

    private Role createRoleAdmin() {
        Role roleAdmin = new Role();
        roleAdmin.setId(2L);
        roleAdmin.setName(Role.RoleName.ADMIN);
        return roleAdmin;
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

    private PaymentDto createPaymentDto() {
        try {
            return new PaymentDto(
                    PAYMENT_ID,
                    Payment.Status.PENDING,
                    BOOKING_ID,
                    new URL(URL),
                    SESSION_ID,
                    BigDecimal.valueOf(130.00)
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Payment createPayment() {
        Payment payment = new Payment();
        payment.setId(PAYMENT_ID);
        payment.setStatus(Payment.Status.PENDING);
        payment.setBooking(createBooking());
        payment.setSessionId(SESSION_ID);
        payment.setAmountToPay(BigDecimal.valueOf(130.00));
        try {
            payment.setSessionUrl(new URL(URL));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return payment;
    }
}
