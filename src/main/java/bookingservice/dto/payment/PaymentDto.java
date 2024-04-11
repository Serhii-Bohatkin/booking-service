package bookingservice.dto.payment;

import bookingservice.model.Payment;
import java.math.BigDecimal;
import java.net.URL;

public record PaymentDto(
        Long id,
        Payment.Status status,
        Long bookingId,
        URL sessionUrl,
        String sessionId,
        BigDecimal amountToPay
) {
}
