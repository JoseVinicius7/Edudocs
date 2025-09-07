package br.com.edudocs.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "school", schema = "public")
@SequenceGenerator(name = "school_seq", sequenceName = "school_id_seq", allocationSize = 1)
public class SchoolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "school_seq")
    @Column(name = "id", columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "zone", nullable = false)
    private String zone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", foreignKey = @ForeignKey(name = "fk_school_address"))
    private AddressEntity address;
}
