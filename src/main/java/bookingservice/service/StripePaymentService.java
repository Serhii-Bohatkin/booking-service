package bookingservice.service;

import bookingservice.dto.payment.PaymentSessionDto;

public interface StripePaymentService {
    PaymentSessionDto createPaymentSession(Long bookingId);
}
