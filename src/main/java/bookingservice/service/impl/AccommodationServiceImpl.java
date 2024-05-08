package bookingservice.service.impl;

import bookingservice.dto.accommodation.AccommodationDto;
import bookingservice.dto.accommodation.AccommodationRequestDto;
import bookingservice.exception.EntityNotFoundException;
import bookingservice.mapper.AccommodationMapper;
import bookingservice.model.Accommodation;
import bookingservice.repository.AccommodationRepository;
import bookingservice.service.AccommodationService;
import bookingservice.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final NotificationService notificationService;

    @Override
    public AccommodationDto create(AccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationMapper.toModel(requestDto);
        AccommodationDto dto = accommodationMapper.toDto(
                accommodationRepository.save(accommodation));
        notificationService.sendCreatedAccommodationMessage(dto);
        return dto;
    }

    @Override
    public List<AccommodationDto> getAll(Pageable pageable) {
        return accommodationRepository.findAll(pageable)
                .stream()
                .map(accommodationMapper::toDto)
                .toList();
    }

    @Override
    public AccommodationDto getById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Accommodation with id " + id + " not found")
        );
        return accommodationMapper.toDto(accommodation);
    }

    @Override
    public AccommodationDto updateById(Long id, AccommodationRequestDto requestDto) {
        getById(id);
        Accommodation accommodation = accommodationMapper.toModel(requestDto);
        accommodation.setId(id);
        return accommodationMapper.toDto(accommodationRepository.save(accommodation));
    }

    @Override
    public void deleteById(Long id) {
        getById(id);
        notificationService.sendDeletedAccommodationMessage(id);
        accommodationRepository.deleteById(id);
    }
}
