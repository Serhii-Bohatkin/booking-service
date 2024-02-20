package bookingservice.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "accommodations")
@SQLDelete(sql = "UPDATE accommodations SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@RequiredArgsConstructor
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)", unique = true)
    private Type type;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String size;

    @ElementCollection
    @CollectionTable(name = "accommodations_amenities")
    private List<String> amenities = new ArrayList<>();

    @Column(name = "daily_rate", nullable = false)
    private BigDecimal dailyRate;

    @Column(nullable = false)
    private Integer availability;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public enum Type {
        HOUSE, APARTMENT, CONDO, VACATION_HOME
    }
}
