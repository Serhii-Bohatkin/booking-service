package bookingservice.service.impl;

import bookingservice.dto.payment.PaymentDto;
import bookingservice.dto.payment.PaymentRequestDto;
import bookingservice.dto.payment.PaymentResponseCancelDto;
import bookingservice.dto.payment.PaymentResponseWithoutUrlDto;
import bookingservice.exception.EntityNotFoundException;
import bookingservice.exception.PaymentException;
import bookingservice.mapper.PaymentMapper;
import bookingservice.model.Booking;
import bookingservice.model.Payment;
import bookingservice.model.Role;
import bookingservice.model.User;
import bookingservice.repository.BookingRepository;
import bookingservice.repository.PaymentRepository;
import bookingservice.service.BookingService;
import bookingservice.service.NotificationService;
import bookingservice.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final Long DEFAULT_QUANTITY = 1L;
    private static final String PRODUCT_DATA_NAME = "Payment for booking";
    private static final String PAYMENT_CURRENCY_USD = "usd";
    private static final BigDecimal PRICE_CORRECTION = BigDecimal.valueOf(100L);
    private static final String CANCEL_URL = "http://localhost:8080/payments"
            + "/cancel?session_id={CHECKOUT_SESSION_ID}";
    private static final String SUCCESS_URL = "http://localhost:8080/payments"
            + "/success?session_id={CHECKOUT_SESSION_ID}";
    private static final long AMOUNT_DAY = 1L;
    private static final String PAYMENT_IS_PAID_STATUS = "paid";

    private final BookingService bookingService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final BookingRepository bookingRepository;
    private final NotificationService telegramNotificationService;
    @Value("${stripe.secret.key}")
    private String stripe;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripe;
    }

    @Transactional
    @Override
    public PaymentDto initiatePaymentSession(PaymentRequestDto paymentRequestDto) {
        Session session;
        try {
            session = Session.create(getSessionCreateParams(paymentRequestDto));
            return paymentMapper.toDto(savePayment(session, paymentRequestDto));
        } catch (StripeException e) {
            throw new PaymentException("Cant pay for booking!", e);
        }
    }

    @Override
    public List<PaymentDto> getPaymentsForUser(Long id, Pageable pageable) {
        boolean isUserHasRoleAdmin = getCurrentUser().getRoles().stream()
                .anyMatch(role -> Role.RoleName.ADMIN.equals(role.getName()));
        if (isUserHasRoleAdmin || getCurrentUser().getId().equals(id)) {
            return paymentRepository.findAllByBookingUserId(id, Pageable.unpaged()).stream()
                    .map(paymentMapper::toDto)
                    .toList();
        }
        throw new EntityNotFoundException("No payments found for user with id " + id);
    }

    @Transactional
    @Override
    public PaymentResponseWithoutUrlDto handleSuccessfulPayment(String sessionId) {
        if (!isPaymentPaid(sessionId)) {
            throw new PaymentException("Payment with session id " + sessionId + " not paid");
        }
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() ->
                        new PaymentException("Payment with sessionId: " + sessionId
                                + " not found!"));
        Booking booking = payment.getBooking();
        if (payment.getStatus().equals(Payment.Status.PENDING)
                && booking.getStatus().equals(Booking.Status.PENDING)) {
            booking.setStatus(Booking.Status.CONFIRMED);
            payment.setStatus(Payment.Status.PAID);
            bookingRepository.save(booking);
            paymentRepository.save(payment);
        }
        PaymentResponseWithoutUrlDto dtoWithoutUrl = paymentMapper.toDtoWithoutUrl(payment);
        telegramNotificationService.sendSuccessfulPaymentMessage(payment);
        return dtoWithoutUrl;
    }

    @Override
    public PaymentResponseCancelDto handleCanceledPayment(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            LocalDateTime sessionExpirationTime = Instant.ofEpochSecond(session.getExpiresAt())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                    () -> new EntityNotFoundException("Payment not found"));
            telegramNotificationService.sendPaymentCancelledMessage(payment.getId());
            return new PaymentResponseCancelDto(payment.getId(),
                    "Payment cancelled. You can try again until: " + sessionExpirationTime,
                    payment.getSessionUrl());
        } catch (StripeException e) {
            throw new PaymentException("Cant find session: " + sessionId, e);
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 60000)
    @Override
    public void checkExpiredPayment() {
        List<Payment> payments = paymentRepository.findByStatus(Payment.Status.PENDING);
        for (Payment payment : payments) {
            Session session = null;
            try {
                session = Session.retrieve(payment.getSessionId());
                Long expiresAt = session.getExpiresAt();
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(expiresAt),
                        ZoneId.systemDefault());
                if (dateTime.isBefore(LocalDateTime.now())) {
                    Booking booking = getBookingById(payment);
                    booking.setStatus(Booking.Status.CANCELED);
                    bookingRepository.save(booking);
                    payment.setStatus(Payment.Status.EXPIRED);
                    paymentRepository.save(payment);
                }
            } catch (StripeException e) {
                throw new PaymentException("Cant retrieve the session", e);
            }
        }
    }

    private Booking getBookingById(Payment payment) {
        return bookingRepository.findById(payment.getBooking().getId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find booking with id "
                        + payment.getBooking().getId())
        );
    }

    private boolean isPaymentPaid(String sessionId) {
        Session session = null;
        String status = "";
        try {
            session = Session.retrieve(sessionId);
            status = session.getPaymentStatus();
        } catch (StripeException e) {
            throw new PaymentException("Cant find session: " + sessionId, e);
        }
        return PAYMENT_IS_PAID_STATUS.equals(status);
    }

    private SessionCreateParams getSessionCreateParams(PaymentRequestDto paymentRequestDto) {
        return SessionCreateParams.builder()
                .setCustomerEmail(getCurrentUser().getEmail())
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(SUCCESS_URL)
                .setCancelUrl(CANCEL_URL)
                .setExpiresAt(
                        LocalDateTime.now()
                                .plusDays(AMOUNT_DAY)
                                .atZone(ZoneId.systemDefault())
                                .toEpochSecond())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(DEFAULT_QUANTITY)
                                .setPriceData(getPriceData(paymentRequestDto))
                                .build())
                .build();
    }

    private SessionCreateParams.LineItem.PriceData getPriceData(
            PaymentRequestDto paymentRequestDto) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(PAYMENT_CURRENCY_USD)
                .setUnitAmountDecimal(getTotalPrice(paymentRequestDto))
                .setProductData(
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(PRODUCT_DATA_NAME)
                                .setDescription(
                                        getBookingDescription(paymentRequestDto))
                                .build()
                )
                .build();
    }

    private BigDecimal getTotalPrice(PaymentRequestDto paymentRequestDto) {
        Booking booking = findBookingById(paymentRequestDto.bookingId());
        long daysBetween = ChronoUnit.DAYS.between(
                booking.getCheckInDate(),
                booking.getCheckOutDate());

        return booking.getAccommodation().getDailyRate()
                .multiply(BigDecimal.valueOf(daysBetween))
                .multiply(PRICE_CORRECTION);
    }

    public String getBookingDescription(PaymentRequestDto payment) {
        Booking booking = findBookingById(payment.bookingId());
        return "Payment booking at address: " + booking.getAccommodation().getLocation();
    }

    private Booking findBookingById(Long id) {
        bookingService.getAllForCurrentUser(Pageable.unpaged()).stream()
                .filter(booking -> booking.id().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Booking with id: " + id + "not found!"));
        return bookingRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Booking with id: " + id + "not found!"));
    }

    private Payment savePayment(Session session,
                                PaymentRequestDto paymentRequestDto) {
        Payment payment = new Payment();
        try {
            payment.setSessionUrl(new URL(session.getUrl()));
        } catch (MalformedURLException e) {
            throw new PaymentException("Invalid URL " + session.getUrl(), e);
        }
        payment.setStatus(Payment.Status.PENDING);
        payment.setBooking(findBookingById(paymentRequestDto.bookingId()));
        payment.setSessionId(session.getId());
        payment.setAmountToPay(BigDecimal.valueOf(session.getAmountSubtotal())
                .divide(PRICE_CORRECTION, RoundingMode.valueOf(3)));
        return paymentRepository.save(payment);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
