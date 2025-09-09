package br.com.edudocs.service.impl;

import br.com.edudocs.api.model.EmployeeRequestDTO;
import br.com.edudocs.api.model.EmployeeResponseDTO;
import br.com.edudocs.api.model.RoleDTO;
import br.com.edudocs.entity.EmployeeEntity;
import br.com.edudocs.entity.EmployeeRoleEntity;
import br.com.edudocs.entity.RoleEntity;
import br.com.edudocs.exception.BadRequestException;
import br.com.edudocs.mapper.EmployeeMapper;
import br.com.edudocs.mapper.RoleMapper;
import br.com.edudocs.repository.EmployeeRepository;
import br.com.edudocs.repository.RoleRepository;
import br.com.edudocs.service.EmployeeService;
import br.com.edudocs.utils.BaseLogger.BaseServiceLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementação do {@link EmployeeService} para gerenciar operações de funcionários.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *     <li>Criar, listar e atualizar funcionários.</li>
 *     <li>Gerenciar roles associadas a cada funcionário.</li>
 *     <li>Padronizar ‘logs’ de execução com medição de tempo e informações detalhadas.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final EmployeeMapper employeeMapper;
    private final RoleMapper roleMapper;
    private final BaseServiceLogger logger;

    /**
     * Registra um novo funcionário.
     *
     * @param employeeRequestDTO DTO com os dados do funcionário e roles
     * @return DTO de resposta com o funcionário criado
     * @throws BadRequestException se dados obrigatórios estiverem ausentes
     */
    @Override
    public EmployeeResponseDTO registerEmployee(EmployeeRequestDTO employeeRequestDTO) {
        String method = "EmployeeService.registerEmployee";

        return logger.logExecution(method, () -> {
            log.info("{} - starting registration for employee: {}", method, employeeRequestDTO.getName());

            validateEmployeeDTO(employeeRequestDTO, method);

            EmployeeEntity employeeEntity = employeeMapper.toEntity(employeeRequestDTO);

            Set<EmployeeRoleEntity> resolvedRoles = resolveRoles(employeeEntity, employeeRequestDTO.getRoles(), method);
            employeeEntity.setRoles(resolvedRoles);

            log.info("{} - persisting employee with {} roles", method, resolvedRoles.size());
            EmployeeEntity saved = employeeRepository.save(employeeEntity);
            log.info("{} - employee saved successfully - id={}", method, saved.getId());

            return employeeMapper.toResponseDto(saved);
        });
    }

    /**
     * Lista todos os funcionários cadastrados.
     *
     * @return lista de DTOs de funcionários
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> listEmployees() {
        String method = "EmployeeService.listEmployees";

        return logger.logExecution(method, () -> {
            log.info("{} - fetching all employees", method);

            List<EmployeeEntity> employees = employeeRepository.findAll();
            log.info("{} - total employees found: {}", method, employees.size());

            List<EmployeeResponseDTO> responseList = employees.stream()
                    .peek(e -> logEmployeeRoles(e, method))
                    .map(employeeMapper::toResponseDto)
                    .collect(Collectors.toList());

            log.info("{} - returning {} employee DTOs", method, responseList.size());
            return responseList;
        });
    }

    /**
     * Valida os dados obrigatórios do DTO do funcionário.
     */
    private void validateEmployeeDTO(EmployeeRequestDTO dto, String method) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            log.warn("{} - registration failed: employee name is missing", method);
            throw new BadRequestException("Employee name is required.");
        }
        if (dto.getContractType() == null || dto.getContractType().trim().isEmpty()) {
            log.warn("{} - registration failed: contract type is missing", method);
            throw new BadRequestException("Contract type is required.");
        }
        if (dto.getWorkload() == null || dto.getWorkload() <= 0) {
            log.warn("{} - registration failed: invalid workload", method);
            throw new BadRequestException("Workload must be greater than 0.");
        }
        if (dto.getAttendance() == null || dto.getAttendance() < 0) {
            log.warn("{} - registration failed: invalid attendance", method);
            throw new BadRequestException("Attendance must be 0 or greater.");
        }
    }

    /**
     * Resolve roles para o funcionário: reutiliza roles existentes ou cria novas.
     */
    private Set<EmployeeRoleEntity> resolveRoles(EmployeeEntity employee, List<RoleDTO> roleDTOs, String method) {
        return Optional.ofNullable(roleDTOs).orElse(List.of()).stream()
                .filter(r -> r != null && r.getName() != null && !r.getName().trim().isEmpty())
                .map(r -> {
                    RoleEntity roleEntity = roleRepository.findByName(r.getName().trim())
                            .orElseGet(() -> {
                                RoleEntity newRole = roleMapper.toEntity(r);
                                RoleEntity savedRole = roleRepository.save(newRole);
                                log.info("{} - created new role: {}", method, savedRole.getName());
                                return savedRole;
                            });
                    return EmployeeRoleEntity.builder()
                            .employee(employee)
                            .role(roleEntity)
                            .build();
                })
                .collect(Collectors.toSet());
    }

    /**
     * Loga roles de um funcionário individual.
     */
    private void logEmployeeRoles(EmployeeEntity employee, String method) {
        Set<EmployeeRoleEntity> roles = employee.getRoles();
        if (roles == null || roles.isEmpty()) {
            log.info("{} - employee id={} has no roles", method, employee.getId());
        } else {
            log.info("{} - employee id={} roles: {}", method, employee.getId(),
                    roles.stream().map(r -> r.getRole().getName()).collect(Collectors.joining(", ")));
        }
    }
}
