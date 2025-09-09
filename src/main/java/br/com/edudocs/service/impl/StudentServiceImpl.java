package br.com.edudocs.service.impl;

import br.com.edudocs.api.model.StudentRequestDTO;
import br.com.edudocs.api.model.StudentResponseDTO;
import br.com.edudocs.entity.SchoolEntity;
import br.com.edudocs.entity.StudentEntity;
import br.com.edudocs.exception.BadRequestException;
import br.com.edudocs.exception.NotFoundException;
import br.com.edudocs.mapper.StudentMapper;
import br.com.edudocs.repository.SchoolRepository;
import br.com.edudocs.repository.StudentRepository;
import br.com.edudocs.service.StudentService;
import br.com.edudocs.utils.BaseLogger.BaseServiceLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação do {@link StudentService} para gerenciar operações de alunos.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *     <li>Criar, atualizar e excluir alunos.</li>
 *     <li>Buscar alunos por ID ou listar todos.</li>
 *     <li>Soft delete usando status INACTIVE.</li>
 *     <li>Padronizar ‘logs’ de execução com medição de tempo e informações detalhadas.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final StudentMapper studentMapper;
    private final BaseServiceLogger logger;

    /**
     * Cria um aluno no sistema.
     *
     * @param studentRequestDTO DTO com informações do aluno
     * @return DTO de resposta do aluno criado
     */
    @Override
    public StudentResponseDTO createStudent(StudentRequestDTO studentRequestDTO) {
        String method = "StudentService.createStudent";
        log.info("{} - starting student creation: name={}", method, studentRequestDTO.getName());

        // Validação básica do DTO
        if (studentRequestDTO.getName() == null || studentRequestDTO.getName().trim().isEmpty()) {
            log.warn("{} - creation failed: student name is missing", method);
            throw new BadRequestException("Student name is required.");
        }
        if (studentRequestDTO.getBirthDate() == null) {
            log.warn("{} - creation failed: birth date is missing", method);
            throw new BadRequestException("Birth date is required.");
        }
        if (studentRequestDTO.getGender() == null) {
            log.warn("{} - creation failed: gender is missing", method);
            throw new BadRequestException("Gender is required.");
        }
        if (studentRequestDTO.getSchoolId() == null) {
            log.warn("{} - creation failed: schoolId is missing", method);
            throw new BadRequestException("School ID is required.");
        }

        // Verifica se a escola existe
        SchoolEntity school = schoolRepository.findById(studentRequestDTO.getSchoolId())
                .orElseThrow(() -> {
                    log.warn("{} - creation failed: school not found with id={}", method, studentRequestDTO.getSchoolId());
                    return new BadRequestException("School not found with id: " + studentRequestDTO.getSchoolId());
                });

        // Mapeia DTO para entity
        StudentEntity studentEntity = studentMapper.toEntity(studentRequestDTO);
        studentEntity.setSchool(school);

        StudentEntity savedStudent = studentRepository.save(studentEntity);
        log.info("{} - student created successfully with id={}", method, savedStudent.getId());

        return studentMapper.toResponseDto(savedStudent);
    }


    /**
     * Busca um aluno pelo ID.
     *
     * @param id ID do aluno
     * @return DTO de resposta do aluno encontrado
     * @throws NotFoundException se o aluno não for encontrado
     */
    @Override
    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long id) {
        String method = "StudentService.getStudentById";

        return logger.logExecution(method, () -> {
            log.info("{} - searching student by id={}", method, id);
            StudentEntity student = studentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("{} - student not found - id={}", method, id);
                        return new NotFoundException("Aluno não encontrado com ID: " + id);
                    });
            return studentMapper.toResponseDto(student);
        });
    }

    /**
     * Retorna todos os alunos cadastrados.
     *
     * @return lista de DTOs de alunos
     */
    @Override
    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents() {
        String method = "StudentService.getAllStudents";

        return logger.logExecution(method, () -> {
            List<StudentEntity> students = studentRepository.findAll();
            log.info("{} - total students found={}", method, students.size());
            return students.stream()
                    .map(studentMapper::toResponseDto)
                    .collect(Collectors.toList());
        });
    }

    /**
     * Busca estudantes cujo nome contenha o valor informado (LIKE SQL, case-insensitive).
     *
     * <p>Exemplo: nome="jo" retornaria "João", "Jonas", "Marcio João".</p>
     *
     * @param name termo a ser buscado no nome do estudante
     * @return lista de {@link StudentResponseDTO} encontrados
     */
    @Override
    @Transactional(readOnly = true)
    public List<StudentResponseDTO> findStudentsByName(String name) {
        String method = "StudentService.findStudentsByName";

        return logger.logExecution(method, () -> {
            log.info("{} - searching students with name like: {}", method, name);

            List<StudentEntity> students = studentRepository.findByNameLike(name);
            log.info("{} - total students found: {}", method, students.size());

            List<StudentResponseDTO> response = students.stream()
                    .map(studentMapper::toResponseDto)
                    .toList();

            log.info("{} - returning {} student DTOs", method, response.size());
            return response;
        });
    }


    /**
     * Lista alunos de uma escola específica.
     *
     * <p>Regras:</p>
     * <ul>
     *   <li>Se a escola não tiver alunos, retorna lista vazia.</li>
     *   <li>Alunos sem escola associada não serão considerados.</li>
     * </ul>
     *
     * @param schoolId identificador da escola
     * @return lista de {@link StudentResponseDTO} pertencentes à escola
     */
    @Transactional(readOnly = true)
    @Override
    public List<StudentResponseDTO> getStudentsBySchool(Long schoolId) {
        String method = "StudentService.getStudentsBySchool";

        return logger.logExecution(method, () -> {
            log.info("{} - fetching students for schoolId={}", method, schoolId);

            List<StudentEntity> students = studentRepository.findBySchoolId(schoolId);
            log.info("{} - total students found: {}", method, students.size());

            List<StudentResponseDTO> response = studentMapper.toResponseDtoList(students);
            log.info("{} - returning {} student DTOs", method, response.size());
            return response;
        });
    }

    /**
     * Atualiza os dados de um aluno existente.
     *
     * @param id  ID do aluno
     * @param dto DTO com os novos dados
     * @return DTO de resposta do aluno atualizado
     * @throws NotFoundException se o aluno não for encontrado
     */
    @Override
    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO dto) {
        String method = "StudentService.updateStudent";

        return logger.logExecution(method, () -> {
            log.info("{} - updating student - id={}", method, id);

            StudentEntity student = studentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("{} - student not found - id={}", method, id);
                        return new NotFoundException("Aluno não encontrado com ID: " + id);
                    });

            student.setName(dto.getName());
            student.setBirthDate(dto.getBirthDate());
            student.setGender(StudentEntity.Gender.valueOf(dto.getGender().name()));
            student.setSchool(studentMapper.mapSchoolIdToEntity(dto.getSchoolId()));

            StudentEntity updated = studentRepository.save(student);
            log.info("{} - student updated successfully - id={}", method, updated.getId());

            return studentMapper.toResponseDto(updated);
        });
    }

    /**
     * Marca um aluno como inativo (soft delete).
     *
     * @param id ID do aluno
     * @throws NotFoundException se o aluno não for encontrado
     */
    @Override
    public void deleteStudent(Long id) {
        String method = "StudentService.deleteStudent";

        logger.logExecutionVoid(method, () -> {
            log.info("{} - soft deleting student - id={}", method, id);

            StudentEntity student = studentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("{} - student not found for deletion - id={}", method, id);
                        return new NotFoundException("Aluno não encontrado com ID: " + id);
                    });

            student.setStatus(StudentEntity.StudentStatus.INACTIVE);
            studentRepository.save(student);

            log.info("{} - student marked as INACTIVE - id={}", method, id);
        });
    }
}
