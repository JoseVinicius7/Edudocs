package br.com.edudocs.service;

import br.com.edudocs.api.model.EmployeeRequestDTO;
import br.com.edudocs.api.model.EmployeeResponseDTO;
import br.com.edudocs.entity.EmployeeEntity;

import java.util.List;

public interface EmployeeService {
    EmployeeResponseDTO registerEmployee(EmployeeRequestDTO employeeRequestDTO);

    List<EmployeeResponseDTO> listEmployees();
}
