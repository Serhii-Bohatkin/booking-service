package bookingservice.service;

import bookingservice.dto.accommodation.AccommodationDto;
import bookingservice.dto.accommodation.AccommodationRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface AccommodationService {
    AccommodationDto create(AccommodationRequestDto requestDto);

    List<AccommodationDto> getAll(Pageable pageable);

    AccommodationDto getById(Long id);

    AccommodationDto updateById(Long id, AccommodationRequestDto requestDto);

    void deleteById(Long id);
}
