package bookingservice.dto.payment;

import bookingservice.model.Payment;

public record SuccessfulPaymentResponseDto(
        Long id,
        Payment.Status status,
        String sessionUrl
) {
}
