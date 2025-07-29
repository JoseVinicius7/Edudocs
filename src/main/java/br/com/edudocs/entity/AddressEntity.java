package br.com.edudocs.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "address", schema = "public")
@SequenceGenerator(name = "address_seq", sequenceName = "address_id_seq", allocationSize = 1)
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq")
    @Column(name = "id", columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "street", nullable = false, length = 255)
    private String street;

    @Column(name = "number", length = 20)
    private String number;

    @Column(name = "complement", length = 255)
    private String complement;

    @Column(name = "neighborhood", length = 255)
    private String neighborhood;

    @Column(name = "city", nullable = false, length = 255)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;
}
