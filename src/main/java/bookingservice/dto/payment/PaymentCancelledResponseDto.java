package bookingservice.dto.payment;

import bookingservice.model.Payment;

public record PaymentCancelledResponseDto(
        Long id,
        Payment.Status status
) {
}
