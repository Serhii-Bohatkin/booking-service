package bookingservice.mapper;

import bookingservice.config.MapperConfig;
import bookingservice.dto.accommodation.AccommodationDto;
import bookingservice.dto.accommodation.AccommodationRequestDto;
import bookingservice.model.Accommodation;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface AccommodationMapper {
    AccommodationDto toDto(Accommodation accommodation);

    Accommodation toModel(AccommodationRequestDto requestDto);
}
