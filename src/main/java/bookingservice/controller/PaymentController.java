package bookingservice.controller;

import bookingservice.dto.payment.PaymentCancelledResponseDto;
import bookingservice.dto.payment.PaymentCreateRequestDto;
import bookingservice.dto.payment.PaymentDto;
import bookingservice.dto.payment.PaymentSessionDto;
import bookingservice.dto.payment.SuccessfulPaymentResponseDto;
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
    public List<PaymentDto> getPaymentsForUser(@RequestParam Long userId, Pageable pageable) {
        return paymentService.getPaymentsForUser(userId, pageable);
    }

    @Operation(summary = "Initialize a payment session")
    @PostMapping
    public PaymentSessionDto initiatePaymentSession(
            @RequestBody @Valid PaymentCreateRequestDto requestDto) {
        return paymentService.initiatePaymentSession(requestDto);
    }

    @Operation(summary = "Success session",
            description = "Confirm successful payment processing through Stripe redirection")
    @GetMapping("/success")
    public SuccessfulPaymentResponseDto handleSuccessfulPayment(@RequestParam Long paymentId) {
        return paymentService.handleSuccessfulPayment(paymentId);
    }

    @Operation(summary = "Cancel session",
            description = "Manage payment cancellation "
                    + "and return messages during Stripe redirection")
    @GetMapping("/cancel")
    public PaymentCancelledResponseDto handleCancelledPayment(@RequestParam Long paymentId) {
        return paymentService.handleCancelledPayment(paymentId);
    }
}
