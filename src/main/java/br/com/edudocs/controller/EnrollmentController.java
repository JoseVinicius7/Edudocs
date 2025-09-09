package br.com.edudocs.controller;

import br.com.edudocs.api.EnrollmentApi;
import br.com.edudocs.api.model.EnrollmentRequestDTO;
import br.com.edudocs.api.model.EnrollmentResponseDTO;
import br.com.edudocs.service.EnrollmentService;
import br.com.edudocs.utils.BaseLogger.BaseControllerLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador responsável por gerenciar operações relacionadas a matrículas.
 *
 * <p>Funcionalidades:</p>
 * <ul>
 *   <li>Criação de novas matrículas.</li>
 *   <li>Busca por ‘ID’, aluno ou escola.</li>
 *   <li>Atualização e exclusão de matrículas.</li>
 *   <li>Listagem de todas as matrículas.</li>
 * </ul>
 *
 * <p>Padronização de ‘logs’:</p>
 * <ul>
 *   <li>Início e fim do metodo com medição de tempo.</li>
 *   <li>Request: campos relevantes do payload ou parâmetros.</li>
 *   <li>Response: status HTTP e identificadores.</li>
 * </ul>
 */
@RequiredArgsConstructor
@Validated
@RestController
public class EnrollmentController extends BaseControllerLogger implements EnrollmentApi {

    private final EnrollmentService enrollmentService;

    /**
     * Cria uma matrícula.
     *
     * @param enrollment dados da matrícula a serem criados
     * @return {@link ResponseEntity} contendo a matrícula criada
     */
    @Override
    public ResponseEntity<EnrollmentResponseDTO> createEnrollment(@Valid @RequestBody EnrollmentRequestDTO enrollment) {
        String method = "EnrollmentController.createEnrollment";

        return logExecution(method, () -> {
            logRequest(method, String.format("studentId=%d - schoolId=%d",
                    enrollment.getStudentId(), enrollment.getSchoolId()));

            EnrollmentResponseDTO created = enrollmentService.createEnrollment(enrollment);

            logResponse(method, HttpStatus.CREATED, "id=" + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        });
    }


    /**
     * Lista todas as matrículas cadastradas.
     *
     * @return {@link ResponseEntity} contendo a lista de matrículas
     */
    @Override
    public ResponseEntity<List<EnrollmentResponseDTO>> findAllEnrollments(
            @RequestParam(value = "studentId", required = false) Long studentId,
            @RequestParam(value = "schoolId", required = false) Long schoolId,
            @RequestParam(value = "status", required = false) String status
    ) {
        String method = "EnrollmentController.findAllEnrollments";

        return logExecution(method, () -> {
            logRequest(method, String.format("studentId=%s - schoolId=%s - status=%s",
                    studentId, schoolId, status));

            List<EnrollmentResponseDTO> enrollments = enrollmentService.findAllEnrollments(studentId, schoolId, status);

            logResponse(method, HttpStatus.OK, "total=" + (enrollments == null ? 0 : enrollments.size()));
            return ResponseEntity.ok(enrollments);
        });
    }


    /**
     * Atualiza os dados de uma matrícula existente.
     *
     * @param id         identificador único da matrícula a ser atualizada
     * @param enrollment dados atualizados da matrícula
     * @return {@link ResponseEntity} com os dados atualizados ou status 404
     */
    @Override
    public ResponseEntity<EnrollmentResponseDTO> updateEnrollment(
            @PathVariable("id") Long id,
            @Valid @RequestBody EnrollmentRequestDTO enrollment) {

        String method = "EnrollmentController.updateEnrollment";

        return logExecution(method, () -> {
            logRequest(method, String.format("id=%d - studentId=%d - schoolId=%d",
                    id, enrollment.getStudentId(), enrollment.getSchoolId()));

            EnrollmentResponseDTO updated = enrollmentService.updateEnrollment(id, enrollment);

            logResponse(method, HttpStatus.OK, "id=" + updated.getId());
            return ResponseEntity.ok(updated);
        });
    }

    /**
     * Remove uma matrícula pelo seu ID.
     *
     * @param id identificador único da matrícula
     * @return {@link ResponseEntity} sem conteúdo em caso de sucesso
     */
    @Override
    public ResponseEntity<Void> deleteEnrollment(@PathVariable("id") Long id) {
        String method = "EnrollmentController.deleteEnrollment";

        return logExecution(method, () -> {
            logRequest(method, "id=" + id);

            enrollmentService.deleteEnrollment(id);

            logResponse(method, HttpStatus.NO_CONTENT, "deleted");
            return ResponseEntity.noContent().build();
        });
    }
}
