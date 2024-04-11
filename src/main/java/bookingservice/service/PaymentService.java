package bookingservice.service;

import bookingservice.dto.payment.PaymentCancelledResponseDto;
import bookingservice.dto.payment.PaymentCreateRequestDto;
import bookingservice.dto.payment.PaymentDto;
import bookingservice.dto.payment.PaymentSessionDto;
import bookingservice.dto.payment.SuccessfulPaymentResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    List<PaymentDto> getPaymentsForUser(Long userId, Pageable pageable);

    PaymentSessionDto initiatePaymentSession(PaymentCreateRequestDto paymentCreateRequestDto);

    SuccessfulPaymentResponseDto handleSuccessfulPayment(Long paymentId);

    PaymentCancelledResponseDto handleCancelledPayment(Long paymentId);
}
