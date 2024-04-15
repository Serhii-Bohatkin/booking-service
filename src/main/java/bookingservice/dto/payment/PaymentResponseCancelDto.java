package bookingservice.dto.payment;

import java.net.URL;

public record PaymentResponseCancelDto(
        Long id,
        String message,
        URL sessionUrl
) {
}
