package bookingservice.repository;

import bookingservice.model.Accommodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    @Override
    Page<Accommodation> findAll(Pageable pageable);
}
