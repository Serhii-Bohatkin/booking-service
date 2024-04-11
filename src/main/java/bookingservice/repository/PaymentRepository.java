package bookingservice.repository;

import bookingservice.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p "
            + "JOIN Booking b ON p.booking.id = b.id "
            + "WHERE b.user.id = :userId")
    Page<Payment> findPaymentsByUserId(Long userId, Pageable pageable);
}
