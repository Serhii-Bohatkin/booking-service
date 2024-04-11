package bookingservice.dto.payment;

import jakarta.validation.constraints.NotNull;

public record PaymentCreateRequestDto(
        @NotNull Long bookingId
) {
}
