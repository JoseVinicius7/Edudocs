package br.com.edudocs.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private EmployeeEntity employee;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleEntity role;


}
