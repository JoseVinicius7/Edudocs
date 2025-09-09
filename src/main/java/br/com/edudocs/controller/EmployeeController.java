package br.com.edudocs.controller;

import br.com.edudocs.api.EmployeeApi;
import br.com.edudocs.api.model.EmployeeRequestDTO;
import br.com.edudocs.api.model.EmployeeResponseDTO;
import br.com.edudocs.service.impl.EmployeeServiceImpl;
import br.com.edudocs.utils.BaseLogger.BaseControllerLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmployeeController extends BaseControllerLogger implements EmployeeApi {

    private final EmployeeServiceImpl employeeService;

    /**
     * POST /api/employees: Cria um funcionário
     *
     * @param employeeRequestDTO (required)
     * @return Funcionário criado (status code 201)
     */
    @Override
    public ResponseEntity<EmployeeResponseDTO> createEmployee(EmployeeRequestDTO employeeRequestDTO) {
        String method = "EmployeeController.create";
        return logExecution(method, () -> {
            logRequest(method, String.format("name=%s - contractType=%s - schoolId=%s",
                    employeeRequestDTO.getName(), employeeRequestDTO.getContractType(), employeeRequestDTO.getSchoolId()));
            EmployeeResponseDTO saved = employeeService.registerEmployee(employeeRequestDTO);
            logResponse(method, HttpStatus.CREATED, "name=" + saved.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        });
    }

    /**
     * GET /api/employees : Lista todos os funcionários
     *
     * @return Lista de funcionários (status code 200)
     */
    @Override
    public ResponseEntity<List<EmployeeResponseDTO>> findAllEmployees() {

        String method = "EmployeeController.findAll";
        return logExecution(method, () -> {
            List<EmployeeResponseDTO> list = employeeService.listEmployees();
            logResponse(method, HttpStatus.OK, "total=" + list.size());
            return ResponseEntity.ok(list);
        });
    }
}
