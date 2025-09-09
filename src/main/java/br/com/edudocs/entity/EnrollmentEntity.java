package br.com.edudocs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@ToString(exclude = {"student", "school"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "enrollment", schema = "public",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "status"}, name = "uk_student_status_active"))
@SequenceGenerator(name = "enrollment_seq", sequenceName = "enrollment_id_seq", allocationSize = 1)
@NoArgsConstructor
public class EnrollmentEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "enrollment_seq")
    @Column(name = "id", columnDefinition = "BIGINT")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private SchoolEntity school;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    @NotBlank(message = "Grade/Class is required.")
    @Column(name = "grade_class", nullable = false, length = 50)
    private String gradeClass;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @PrePersist
    @PreUpdate
    private void normalize() {
        this.gradeClass = normalizeText(this.gradeClass);
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim().replaceAll("\\s+", " ");
        return trimmed.isEmpty() ? null : trimmed;
    }
}
