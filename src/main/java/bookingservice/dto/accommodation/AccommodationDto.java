package bookingservice.dto.accommodation;

import bookingservice.model.Accommodation;
import java.math.BigDecimal;
import java.util.List;

public record AccommodationDto(
        Long id,
        Accommodation.Type type,
        String location,
        String size,
        List<String> amenities,
        BigDecimal dailyRate,
        Integer availability
) {
    @Override
    public String toString() {
        return String.format("""
                        
                        Accommodation id: %s
                        Type: %s
                        Location: %s
                        Size: %s
                        Amenities: %s
                        DailyRate: %s
                        Availability: %s
                        """,
                id, type.name(), location, size, amenities, dailyRate, availability);
    }
}
