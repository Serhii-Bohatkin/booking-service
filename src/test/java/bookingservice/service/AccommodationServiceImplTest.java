package bookingservice.service;

import bookingservice.dto.accommodation.AccommodationDto;
import bookingservice.dto.accommodation.AccommodationRequestDto;
import bookingservice.exception.EntityNotFoundException;
import bookingservice.mapper.AccommodationMapper;
import bookingservice.model.Accommodation;
import bookingservice.repository.AccommodationRepository;
import bookingservice.service.impl.AccommodationServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccommodationServiceImplTest {
    public static final long ACCOMMODATION_ID = 1L;
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private AccommodationMapper accommodationMapper;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    @Test
    public void create_ValidDto_ShouldReturnAccommodationDto() {
        Accommodation accommodation = createAccommodation();
        AccommodationRequestDto requestDto = createAccommodationRequestDto();
        AccommodationDto expected = createAccommodationDto();

        when(accommodationMapper.toModel(requestDto)).thenReturn(accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        AccommodationDto actual = accommodationService.create(requestDto);
        assertThat(actual).isEqualTo(expected);
        Mockito.verify(notificationService).sendCreatedAccommodationMessage(expected);
    }

    @Test
    public void getAll_ShouldReturnListOfAccommodation() {
        Accommodation accommodation = createAccommodation();
        List<Accommodation> accommodations = List.of(accommodation);
        Page<Accommodation> accommodationPage = new PageImpl<>(
                accommodations, Pageable.unpaged(), accommodations.size());
        AccommodationDto expected = createAccommodationDto();

        when(accommodationRepository.findAll(Pageable.unpaged())).thenReturn(accommodationPage);
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        List<AccommodationDto> actual = accommodationService.getAll(Pageable.unpaged());
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    @Test
    public void getById_ExistingId_ShouldReturnAccommodationDto() {
        Accommodation accommodation = createAccommodation();
        AccommodationDto expected = createAccommodationDto();

        when(accommodationRepository.findById(ACCOMMODATION_ID)).thenReturn(Optional.of(accommodation));
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        AccommodationDto actual = accommodationService.getById(ACCOMMODATION_ID);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getById_NonExistingId_ShouldThrowException() {
        when(accommodationRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> accommodationService.getById(Long.MAX_VALUE))
                .withMessage("Accommodation with id " + Long.MAX_VALUE + "not found");
    }

    @Test
    public void updateById_ExistingId_ShouldReturnAccommodationDto() {
        Accommodation accommodation = createAccommodation();
        AccommodationDto expected = createAccommodationDto();
        AccommodationRequestDto requestDto = createAccommodationRequestDto();

        when(accommodationRepository.findById(ACCOMMODATION_ID)).thenReturn(Optional.of(accommodation));
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);
        when(accommodationMapper.toModel(requestDto)).thenReturn(accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);

        AccommodationDto actual = accommodationService.updateById(ACCOMMODATION_ID, requestDto);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void updateById_NonExistingId_ShouldThrowException() {
        when(accommodationRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> accommodationService.getById(Long.MAX_VALUE))
                .withMessage("Accommodation with id " + Long.MAX_VALUE + "not found");
    }

    @Test
    public void deleteById_ExistingId_Success() {
        Accommodation accommodation = createAccommodation();

        when(accommodationRepository.findById(ACCOMMODATION_ID)).thenReturn(Optional.of(accommodation));
        when(accommodationMapper.toDto(accommodation)).thenReturn(createAccommodationDto());

        accommodationService.deleteById(ACCOMMODATION_ID);
        verify(notificationService).sendDeletedAccommodationMessage(ACCOMMODATION_ID);
        verify(accommodationRepository).deleteById(ACCOMMODATION_ID);
    }

    @Test
    public void deleteById_NonExistingId_ShouldThrowException() {
        when(accommodationRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> accommodationService.getById(Long.MAX_VALUE))
                .withMessage("Accommodation with id " + Long.MAX_VALUE + "not found");
    }

    private AccommodationDto createAccommodationDto() {
        return new AccommodationDto(
                ACCOMMODATION_ID,
                Accommodation.Type.HOUSE,
                "Lviv, Shevchenko St. 12",
                "Studio, 1 Bedroom",
                List.of("golf courses", "health club facilities"),
                BigDecimal.valueOf(15),
                2
        );
    }

    private AccommodationRequestDto createAccommodationRequestDto() {
        return new AccommodationRequestDto(
                Accommodation.Type.HOUSE,
                "Lviv, Shevchenko St. 12",
                "Studio, 1 Bedroom",
                List.of("golf courses", "health club facilities"),
                BigDecimal.valueOf(15),
                2
        );
    }

    private Accommodation createAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(ACCOMMODATION_ID);
        accommodation.setType(Accommodation.Type.HOUSE);
        accommodation.setLocation("Lviv, Shevchenko St. 12");
        accommodation.setSize("Studio, 1 Bedroom");
        accommodation.setAmenities(List.of("golf courses", "health club facilities"));
        accommodation.setDailyRate(BigDecimal.valueOf(15));
        accommodation.setAvailability(2);
        return accommodation;
    }
}
