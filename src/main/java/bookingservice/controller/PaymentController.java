package bookingservice.controller;

import bookingservice.dto.payment.PaymentDto;
import bookingservice.dto.payment.PaymentRequestDto;
import bookingservice.dto.payment.PaymentResponseCancelDto;
import bookingservice.dto.payment.PaymentResponseWithoutUrlDto;
import bookingservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment management", description = "Endpoints for managing payments")
@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "Get all payments by user id")
    @GetMapping
    public List<PaymentDto> getPaymentsForUser(
            @RequestParam("user_id") Long userId, Pageable pageable) {
        return paymentService.getPaymentsForUser(userId, pageable);
    }

    @Operation(summary = "Initialize a payment session")
    @PostMapping
    public PaymentDto initiatePaymentSession(
            @RequestBody @Valid PaymentRequestDto requestDto) {
        return paymentService.initiatePaymentSession(requestDto);
    }

    @Operation(summary = "Handle successful payment")
    @GetMapping("/success")
    public PaymentResponseWithoutUrlDto handleSuccessfulPayment(
            @RequestParam("session_id") String sessionId) {
        return paymentService.handleSuccessfulPayment(sessionId);
    }

    @Operation(summary = "Handle canceled payment")
    @GetMapping("/cancel")
    public PaymentResponseCancelDto handleCancelledPayment(
            @RequestParam("session_id") String sessionId) {
        return paymentService.handleCanceledPayment(sessionId);
    }
}
