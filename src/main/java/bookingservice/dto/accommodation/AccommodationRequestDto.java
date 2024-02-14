package bookingservice.dto.accommodation;

import bookingservice.model.Accommodation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record AccommodationRequestDto(
        @NotBlank
        Accommodation.Type type,
        @NotBlank
        String location,
        @NotBlank
        String size,
        @NotNull
        List<String> amenities,
        @Min(1)
        BigDecimal dailyRate,
        @Min(0)
        Integer availability) {
}
