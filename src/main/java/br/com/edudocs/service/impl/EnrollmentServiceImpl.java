package br.com.edudocs.service.impl;

import br.com.edudocs.api.model.EnrollmentRequestDTO;
import br.com.edudocs.api.model.EnrollmentResponseDTO;
import br.com.edudocs.entity.EnrollmentEntity;
import br.com.edudocs.entity.SchoolEntity;
import br.com.edudocs.entity.StudentEntity;
import br.com.edudocs.exception.BadRequestException;
import br.com.edudocs.exception.NotFoundException;
import br.com.edudocs.mapper.EnrollmentMapper;
import br.com.edudocs.repository.EnrollmentRepository;
import br.com.edudocs.repository.SchoolRepository;
import br.com.edudocs.repository.StudentRepository;
import br.com.edudocs.service.EnrollmentService;
import br.com.edudocs.utils.BaseLogger.BaseServiceLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final BaseServiceLogger logger;

    /**
     * Cria uma matrícula para um aluno numa escola.
     *
     * @param dto dados da matrícula
     * @return matrícula criada
     * @throws NotFoundException   se aluno ou escola não forem encontrados
     * @throws BadRequestException se já houver matrícula ativa na mesma escola
     */
    @Override
    public EnrollmentResponseDTO createEnrollment(EnrollmentRequestDTO dto) {
        String method = "EnrollmentService.createEnrollment";

        return logger.logExecution(method, () -> {
            log.info("{} - starting enrollment creation: studentId={}, schoolId={}", method, dto.getStudentId(), dto.getSchoolId());

            StudentEntity student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new NotFoundException("Student not found with id=" + dto.getStudentId()));

            SchoolEntity school = schoolRepository.findById(dto.getSchoolId())
                    .orElseThrow(() -> new NotFoundException("School not found with id=" + dto.getSchoolId()));

            enrollmentRepository.findByStudentIdAndSchoolIdAndStatus(
                            student.getId(), school.getId(), EnrollmentEntity.Status.ACTIVE)
                    .ifPresent(e -> {
                        log.warn("{} - student already has an active enrollment in this school", method);
                        throw new BadRequestException("Student already has an active enrollment in this school.");
                    });

            EnrollmentEntity enrollment = enrollmentMapper.toEntity(dto);
            enrollment.setStudent(student);
            enrollment.setSchool(school);
            enrollment.setStatus(EnrollmentEntity.Status.ACTIVE);

            EnrollmentEntity saved = enrollmentRepository.save(enrollment);
            log.info("{} - enrollment created successfully with id={}", method, saved.getId());

            return enrollmentMapper.toResponseDto(saved);
        });
    }

    /**
     * Lista todas as matrículas, aplicando filtros opcionais.
     *
     * @param studentId ID do aluno (opcional)
     * @param schoolId  ID da escola (opcional)
     * @param status    Status da matrícula (opcional: ACTIVE/INACTIVE)
     * @return lista de matrículas encontradas
     */
    @Transactional(readOnly = true)
    @Override
    public List<EnrollmentResponseDTO> findAllEnrollments(Long studentId, Long schoolId, String status) {
        String method = "EnrollmentService.findAllEnrollments";

        return logger.logExecution(method, () -> {
            List<EnrollmentEntity> entities;

            if (studentId == null && schoolId == null && status == null) {
                entities = enrollmentRepository.findAll();
            } else {
                entities = enrollmentRepository.findByFilters(studentId, schoolId, status);
            }

            log.info("{} - found {} enrollments with filters studentId={}, schoolId={}, status={}",
                    method, entities.size(), studentId, schoolId, status);

            return enrollmentMapper.toResponseDtoList(entities);
        });
    }

    /**
     * Atualiza os dados de uma matrícula existente.
     *
     * @param id  ‘ID’ da matrícula a ser atualizada
     * @param dto dados atualizados da matrícula
     * @return matrícula atualizada
     * @throws NotFoundException   se matrícula, aluno ou escola não forem encontrados
     * @throws BadRequestException se já houver matrícula ativa na mesma escola
     */
    @Override
    public EnrollmentResponseDTO updateEnrollment(Long id, EnrollmentRequestDTO dto) {
        String method = "EnrollmentService.updateEnrollment";

        return logger.logExecution(method, () -> {
            log.info("{} - updating enrollment id={}", method, id);

            EnrollmentEntity enrollment = enrollmentRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Enrollment not found with id=" + id));

            StudentEntity student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new NotFoundException("Student not found with id=" + dto.getStudentId()));

            SchoolEntity school = schoolRepository.findById(dto.getSchoolId())
                    .orElseThrow(() -> new NotFoundException("School not found with id=" + dto.getSchoolId()));

            // Verifica duplicidade ativa
            enrollmentRepository.findByStudentIdAndSchoolIdAndStatus(student.getId(), school.getId(), EnrollmentEntity.Status.ACTIVE)
                    .ifPresent(e -> {
                        if (!e.getId().equals(id)) {
                            log.warn("{} - student already has an active enrollment in this school", method);
                            throw new BadRequestException("Student already has an active enrollment in this school.");
                        }
                    });

            enrollment.setStudent(student);
            enrollment.setSchool(school);
            enrollment.setGradeClass(dto.getGradeClass());
            enrollment.setEnrollmentDate(dto.getEnrollmentDate());
            enrollment.setStatus(EnrollmentEntity.Status.valueOf(dto.getStatus().name()));

            EnrollmentEntity updated = enrollmentRepository.save(enrollment);
            log.info("{} - enrollment updated successfully with id={}", method, updated.getId());

            return enrollmentMapper.toResponseDto(updated);
        });
    }

    /**
     * Marca uma matrícula como inativa (soft delete).
     *
     * @param id ID da matrícula
     * @throws NotFoundException se a matrícula não for encontrada
     */
    @Override
    public void deleteEnrollment(Long id) {
        String method = "EnrollmentService.deleteEnrollment";

        logger.logExecution(method, () -> {
            log.info("{} - deleting enrollment id={}", method, id);

            EnrollmentEntity enrollment = enrollmentRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Enrollment not found with id=" + id));

            enrollment.setStatus(EnrollmentEntity.Status.INACTIVE);
            enrollmentRepository.save(enrollment);

            log.info("{} - enrollment marked as INACTIVE with id={}", method, id);
            return null;
        });
    }
}
