package bookingservice.repository;

import bookingservice.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByBookingUserId(Long userId, Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findByStatus(Payment.Status status);
}
