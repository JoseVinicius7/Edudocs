package br.com.edudocs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "address", schema = "public")
@SequenceGenerator(name = "address_seq", sequenceName = "address_id_seq", allocationSize = 1)
public class AddressEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq")
    @Column(name = "id", columnDefinition = "BIGINT")
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O logradouro (rua) é obrigatório.")
    @Size(max = 255, message = "O logradouro deve ter no máximo 255 caracteres.")
    @Column(name = "street", nullable = false, length = 255)
    private String street;

    @Size(max = 20, message = "O número deve ter no máximo 20 caracteres.")
    @Column(name = "number", length = 20)
    private String number;

    @Size(max = 255, message = "O complemento deve ter no máximo 255 caracteres.")
    @Column(name = "complement", length = 255)
    private String complement;

    @Size(max = 255, message = "O bairro deve ter no máximo 255 caracteres.")
    @Column(name = "neighborhood", length = 255)
    private String neighborhood;

    @NotBlank(message = "A cidade é obrigatória.")
    @Size(max = 255, message = "A cidade deve ter no máximo 255 caracteres.")
    @Column(name = "city", nullable = false, length = 255)
    private String city;

    @NotBlank(message = "O estado é obrigatório.")
    @Size(max = 100, message = "O estado deve ter no máximo 100 caracteres.")
    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @NotBlank(message = "O CEP é obrigatório.")
    @Pattern(regexp = "^[0-9]{5}-?[0-9]{3}$", message = "O CEP deve estar no formato 99999-999 ou 99999999.")
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @PrePersist
    @PreUpdate
    private void normalize() {
        this.street = normalizeText(this.street);
        this.number = normalizeText(this.number);
        this.complement = normalizeText(this.complement);
        this.neighborhood = normalizeText(this.neighborhood);
        this.city = normalizeText(this.city);
        this.state = normalizeUpper(this.state);
        this.postalCode = normalizePostalCode(this.postalCode);
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

    private String normalizePostalCode(String cep) {
        if (cep == null) return null;
        String digits = cep.replaceAll("\\D", "");
        if (digits.length() == 8) {
            return digits.substring(0, 5) + "-" + digits.substring(5);
        }
        return digits.isEmpty() ? null : digits;
    }
}

