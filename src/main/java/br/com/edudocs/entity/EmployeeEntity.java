package br.com.edudocs.entity;

import br.com.edudocs.utils.enums.ContractType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "employees_seq", sequenceName = "employees_id_seq", allocationSize = 1)
public class EmployeeEntity implements Serializable {

    @Id
    @Column(name = "id", columnDefinition = "BIGINT")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employees_seq")
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 30)
    private ContractType contractType;

    @Positive
    @Column(nullable = false)
    private Integer workload; // horas semanais

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(nullable = false)
    private Double attendance; // percentual de frequência

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private SchoolEntity school;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeRoleEntity> roles = new HashSet<>();
}
