package bookingservice.mapper;

import bookingservice.config.MapperConfig;
import bookingservice.dto.booking.BookingDto;
import bookingservice.dto.booking.BookingRequestDto;
import bookingservice.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface BookingMapper {

    @Mapping(source = "accommodationId", target = "accommodation.id")
    Booking toModel(BookingRequestDto requestDto);

    @Mapping(source = "accommodationId", target = "accommodation.id")
    @Mapping(source = "userId", target = "user.id")
    Booking toModel(BookingDto dto);

    @Mapping(source = "accommodation.id", target = "accommodationId")
    @Mapping(source = "user.id", target = "userId")
    BookingDto toDto(Booking booking);
}
