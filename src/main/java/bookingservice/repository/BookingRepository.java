package bookingservice.repository;

import bookingservice.model.Booking;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b "
            + "FROM Booking b "
            + "WHERE (b.checkInDate BETWEEN :checkInDate AND :checkOutDate "
            + "OR b.checkOutDate BETWEEN :checkInDate AND :checkOutDate) "
            + "AND b.accommodation.id = :accommodationId")
    List<Booking> findAllByCheckOutDateBetween(
            Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate);

    Page<Booking> findAllByUserIdAndStatus(Long userId, Booking.Status status, Pageable pageable);

    Page<Booking> findAllByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "accommodation")
    @Override
    Optional<Booking> findById(Long id);

    List<Booking> findByStatusAndCheckOutDateLessThanEqual(
            Booking.Status status, LocalDate checkOutDate);
}
