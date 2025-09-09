package br.com.edudocs.service;

import br.com.edudocs.api.model.EnrollmentRequestDTO;
import br.com.edudocs.api.model.EnrollmentResponseDTO;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponseDTO createEnrollment(EnrollmentRequestDTO dto);

    List<EnrollmentResponseDTO> findAllEnrollments(Long studentId, Long schoolId, String status);

    EnrollmentResponseDTO updateEnrollment(Long id, EnrollmentRequestDTO dto);

    void deleteEnrollment(Long id);
}
