package bookingservice.mapper;

import bookingservice.config.MapperConfig;
import bookingservice.dto.payment.PaymentDto;
import bookingservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(source = "booking.id", target = "bookingId")
    PaymentDto toDto(Payment payment);
}
