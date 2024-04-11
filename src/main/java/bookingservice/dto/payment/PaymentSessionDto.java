package bookingservice.dto.payment;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PaymentSessionDto {
    private Long paymentId;
    private String sessionId;
    private String sessionUrl;
    private Long amount;
}
