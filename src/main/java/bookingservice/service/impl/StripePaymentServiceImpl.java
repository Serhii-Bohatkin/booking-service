package bookingservice.service.impl;

import bookingservice.dto.payment.PaymentSessionDto;
import bookingservice.exception.EntityNotFoundException;
import bookingservice.exception.PaymentException;
import bookingservice.model.Accommodation;
import bookingservice.model.Booking;
import bookingservice.repository.BookingRepository;
import bookingservice.service.StripePaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StripePaymentServiceImpl implements StripePaymentService {
    protected static final BigDecimal NUMBER_OF_CENTS_IN_A_DOLLAR = BigDecimal.valueOf(100);
    private static final String CURRENCY_USD = "usd";
    private static final String SESSION_URL = "https://example.com/redirect";
    private final BookingRepository bookingRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Override
    public PaymentSessionDto createPaymentSession(Long bookingId) {
        Stripe.apiKey = stripeSecretKey;
        BigDecimal totalBookingAmount = calculateTotalBookingAmount(bookingId);
        try {
            PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                    .setCurrency(CURRENCY_USD)
                    .setAmount(totalBookingAmount.multiply(NUMBER_OF_CENTS_IN_A_DOLLAR).longValue())
                    .build();

            PaymentIntent intent = PaymentIntent.create(createParams);

            return new PaymentSessionDto()
                    .setSessionId(intent.getClientSecret())
                    .setSessionUrl(SESSION_URL)
                    .setAmount(intent.getAmount());

        } catch (StripeException e) {
            throw new PaymentException("Error while interacting with Stripe API", e);
        }
    }

    private BigDecimal calculateTotalBookingAmount(Long bookingId) {
        Booking booking = getBookingByBookingId(bookingId);
        Accommodation bookingAccommodation = booking.getAccommodation();
        long daysBetween = ChronoUnit.DAYS.between(
                booking.getCheckInDate(), booking.getCheckOutDate());
        return bookingAccommodation.getDailyRate().multiply(BigDecimal.valueOf(daysBetween));
    }

    private Booking getBookingByBookingId(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(
                () -> new EntityNotFoundException("Can't find booking by bookingID: " + bookingId));
    }
}
