package bookingservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import bookingservice.model.Accommodation;
import bookingservice.model.Booking;
import bookingservice.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookingRepositoryTest {
    public static final String SQL_SCRIPT_BEFORE_TEST =
            "classpath:database/add-data-for-booking-test.sql";
    public static final String SQL_SCRIPT_AFTER_TEST =
            "classpath:database/delete-data-for-booking-test.sql";
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void findAllByCheckOutDateBetween_ExistingBookingData_ShouldReturnOneBooking() {
        Booking expected = createBooking();
        LocalDate in = LocalDate.of(2024, 4, 22);
        LocalDate out = LocalDate.of(2024, 4, 23);
        List<Booking> actual = bookingRepository.findAllByCheckOutDateBetween(1L, in, out);
        assertThat(actual).hasSize(1);
        EqualsBuilder.reflectionEquals(actual.get(0), expected);
    }

    @Test
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void findAllByCheckOutDateBetween_NonExistingBookingData_ShouldReturnEmptyList() {
        LocalDate in = LocalDate.of(2023, 4, 22);
        LocalDate out = LocalDate.of(2023, 4, 23);
        List<Booking> actual = bookingRepository.findAllByCheckOutDateBetween(1L, in, out);
        assertThat(actual).isEmpty();
    }

    private Booking createBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setCheckInDate(LocalDate.of(2024, 4, 22));
        booking.setCheckOutDate(LocalDate.of(2024, 4, 23));
        booking.setAccommodation(createAccommodation());
        booking.setUser(createUser());
        booking.setStatus(Booking.Status.CONFIRMED);
        return booking;
    }

    private Accommodation createAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.HOUSE);
        accommodation.setLocation("Lviv, Shevchenko St. 12");
        accommodation.setSize("Studio, 1 Bedroom");
        accommodation.setDailyRate(BigDecimal.valueOf(15));
        accommodation.setAvailability(10);
        return accommodation;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@gmail.com");
        user.setFirstName("Jhon");
        user.setLastName("Doe");
        user.setPassword("@g_sJ'#_$ks%1Nq");
        return user;
    }
}
