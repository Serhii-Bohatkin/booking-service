package bookingservice.service;

import bookingservice.dto.payment.PaymentDto;
import bookingservice.dto.payment.PaymentRequestDto;
import bookingservice.dto.payment.PaymentResponseCancelDto;
import bookingservice.dto.payment.PaymentResponseWithoutUrlDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    List<PaymentDto> getPaymentsForUser(Long id, Pageable pageable);

    PaymentDto initiatePaymentSession(PaymentRequestDto paymentRequestDto);

    PaymentResponseWithoutUrlDto handleSuccessfulPayment(String sessionId);

    PaymentResponseCancelDto handleCanceledPayment(String sessionId);

    void checkExpiredPayment();
}
