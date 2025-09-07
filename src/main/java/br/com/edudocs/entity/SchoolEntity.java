package br.com.edudocs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString(exclude = "address")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "school", schema = "public")
@SequenceGenerator(name = "school_seq", sequenceName = "school_id_seq", allocationSize = 1)
@NoArgsConstructor
public class SchoolEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "school_seq")
    @Column(name = "id", columnDefinition = "BIGINT")
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome da escola é obrigatório.")
    @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres.")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 50, message = "A zona deve ter no máximo 50 caracteres.")
    @Column(name = "zone", length = 50)
    private String zone;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "address_id", foreignKey = @ForeignKey(name = "fk_school_address"))
    private AddressEntity address;

    @PrePersist
    @PreUpdate
    private void normalize() {
        this.name = normalizeText(this.name);
        this.zone = normalizeUpper(this.zone);
        // address normalização é responsabilidade da própria AddressEntity
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim().replaceAll("\\s+", " ");
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeUpper(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? null : normalized.toUpperCase();
    }
}
