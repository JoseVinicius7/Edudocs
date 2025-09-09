package br.com.edudocs.service;

import br.com.edudocs.api.model.StudentRequestDTO;
import br.com.edudocs.api.model.StudentResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StudentService {
    StudentResponseDTO createStudent(StudentRequestDTO dto);

    StudentResponseDTO getStudentById(Long id);

    List<StudentResponseDTO> getAllStudents();

    List<StudentResponseDTO> findStudentsByName(String name);

    @Transactional(readOnly = true)
    List<StudentResponseDTO> getStudentsBySchool(Long schoolId);

    StudentResponseDTO updateStudent(Long id, StudentRequestDTO dto);

    void deleteStudent(Long id);
}
