package bookingservice.dto.payment;

import bookingservice.model.Payment;
import java.math.BigDecimal;

public record PaymentResponseWithoutUrlDto(
        Long id,
        Payment.Status status,
        Long bookingId,
        String sessionId,
        BigDecimal amountToPay
) {
}
