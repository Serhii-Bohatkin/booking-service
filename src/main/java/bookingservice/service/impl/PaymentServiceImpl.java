package bookingservice.service.impl;

import static bookingservice.service.impl.StripePaymentServiceImpl.NUMBER_OF_CENTS_IN_A_DOLLAR;

import bookingservice.dto.payment.PaymentCancelledResponseDto;
import bookingservice.dto.payment.PaymentCreateRequestDto;
import bookingservice.dto.payment.PaymentDto;
import bookingservice.dto.payment.PaymentSessionDto;
import bookingservice.dto.payment.SuccessfulPaymentResponseDto;
import bookingservice.exception.EntityNotFoundException;
import bookingservice.exception.PaymentException;
import bookingservice.mapper.PaymentMapper;
import bookingservice.model.Booking;
import bookingservice.model.Payment;
import bookingservice.repository.BookingRepository;
import bookingservice.repository.PaymentRepository;
import bookingservice.service.NotificationService;
import bookingservice.service.PaymentService;
import bookingservice.service.StripePaymentService;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final BookingRepository bookingRepository;
    private final StripePaymentService stripePaymentService;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final NotificationService telegramNotificationService;

    @Override
    public List<PaymentDto> getPaymentsForUser(Long userId, Pageable pageable) {
        return paymentRepository.findPaymentsByUserId(userId, pageable).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public PaymentSessionDto initiatePaymentSession(PaymentCreateRequestDto requestDto) {
        PaymentSessionDto paymentSessionDto =
                stripePaymentService.createPaymentSession(requestDto.bookingId());
        Booking booking = getBookingByBookingId(requestDto.bookingId());
        Payment payment = createPayment(booking, paymentSessionDto);
        paymentRepository.save(payment);
        paymentSessionDto.setPaymentId(payment.getId());
        paymentSessionDto.setAmount(payment.getAmountToPay().longValue());
        return paymentSessionDto;
    }

    private Payment createPayment(Booking booking, PaymentSessionDto paymentSessionDto) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setSessionId(paymentSessionDto.getSessionId());
        payment.setAmountToPay(BigDecimal.valueOf(paymentSessionDto.getAmount())
                .divide(NUMBER_OF_CENTS_IN_A_DOLLAR));
        payment.setStatus(Payment.Status.PENDING);
        try {
            payment.setSessionUrl(new URL(paymentSessionDto.getSessionUrl()));
        } catch (MalformedURLException e) {
            throw new PaymentException("Invalid URL " + paymentSessionDto.getSessionUrl(), e);
        }
        return payment;
    }

    @Override
    public SuccessfulPaymentResponseDto handleSuccessfulPayment(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        payment.setStatus(Payment.Status.PAID);
        paymentRepository.save(payment);
        telegramNotificationService.sendSuccessfulPaymentMessage(payment);

        return new SuccessfulPaymentResponseDto(
                paymentId,
                Payment.Status.PAID,
                payment.getSessionUrl().toString());
    }

    @Override
    public PaymentCancelledResponseDto handleCancelledPayment(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        payment.setStatus(Payment.Status.CANCELED);
        paymentRepository.save(payment);
        telegramNotificationService.sendPaymentCancelledMessage(paymentId);
        return new PaymentCancelledResponseDto(paymentId, Payment.Status.CANCELED);
    }

    private Booking getBookingByBookingId(Long id) {
        return bookingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        "Can't find a booking with id: " + id));
    }

    private Payment findPaymentById(Long id) {
        return paymentRepository.findById((id)).orElseThrow(
                () -> new EntityNotFoundException(
                        "Can't find a payment with id: " + id)
        );
    }
}
