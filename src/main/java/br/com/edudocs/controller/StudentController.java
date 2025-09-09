package br.com.edudocs.controller;

import br.com.edudocs.api.StudentApi;
import br.com.edudocs.api.model.StudentRequestDTO;
import br.com.edudocs.api.model.StudentResponseDTO;
import br.com.edudocs.service.StudentService;
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
 * Controlador responsável por gerenciar operações relacionadas a estudantes.
 *
 * <p>Funcionalidades:</p>
 * <ul>
 *   <li>Criação de novos estudantes.</li>
 *   <li>Busca por ‘ID’ ou nome.</li>
 *   <li>Atualização e exclusão de estudantes.</li>
 *   <li>Listagem de todos os estudantes.</li>
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
public class StudentController extends BaseControllerLogger implements StudentApi {

    private final StudentService studentService;

    /**
     * Cria um estudante.
     *
     * @param student dados do estudante a serem criados
     * @return {@link ResponseEntity} contendo o estudante criado
     */
    @Override
    public ResponseEntity<StudentResponseDTO> createStudent(@Valid @RequestBody StudentRequestDTO student) {
        String method = "StudentController.createStudent";

        return logExecution(method, () -> {
            logRequest(method, String.format("name=%s - birthDate=%s - gender=%s",
                    student.getName(), student.getBirthDate(), student.getGender()));

            StudentResponseDTO created = studentService.createStudent(student);

            logResponse(method, HttpStatus.CREATED, "id=" + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        });
    }

    /**
     * Busca um estudante pelo seu ‘ID’.
     *
     * @param id identificador único do estudante
     * @return {@link ResponseEntity} com o estudante encontrado ou status 404
     */
    @Override
    public ResponseEntity<StudentResponseDTO> findStudentById(@PathVariable("id") Long id) {
        String method = "StudentController.findStudentById";

        return logExecution(method, () -> {
            logRequest(method, "id=" + id);

            StudentResponseDTO student = studentService.getStudentById(id);

            logResponse(method, HttpStatus.OK, "id=" + student.getId());
            return ResponseEntity.ok(student);
        });
    }

    /**
     * Lista todos os estudantes cadastrados.
     *
     * @return {@link ResponseEntity} contendo a lista de estudantes
     */
    @Override
    public ResponseEntity<List<StudentResponseDTO>> findAllStudents() {
        String method = "StudentController.findAll";

        return logExecution(method, () -> {
            List<StudentResponseDTO> students = studentService.getAllStudents();
            logResponse(method, HttpStatus.OK, "total=" + students.size());
            return ResponseEntity.ok(students);
        });
    }

    /**
     * Atualiza os dados de um estudante existente.
     *
     * @param id identificador único do estudante
     * @param student dados atualizados do estudante
     * @return {@link ResponseEntity} com os dados atualizados
     */
    @Override
    public ResponseEntity<StudentResponseDTO> updateStudent(@PathVariable("id") Long id,
                                                     @Valid @RequestBody StudentRequestDTO student) {
        String method = "StudentController.update";

        return logExecution(method, () -> {
            logRequest(method, String.format("id=%d - name=%s - birthDate=%s - gender=%s",
                    id, student.getName(), student.getBirthDate(), student.getGender()));

            StudentResponseDTO updated = studentService.updateStudent(id, student);

            logResponse(method, HttpStatus.OK, "id=" + updated.getId());
            return ResponseEntity.ok(updated);
        });
    }

    /**
     * Remove um estudante pelo seu ‘ID’.
     *
     * @param id identificador único do estudante
     * @return {@link ResponseEntity} sem conteúdo em caso de sucesso
     */
    @Override
    public ResponseEntity<Void> deleteStudent(@PathVariable("id") Long id) {
        String method = "StudentController.delete";

        return logExecution(method, () -> {
            logRequest(method, "id=" + id);

            studentService.deleteStudent(id);

            logResponse(method, HttpStatus.NO_CONTENT, "deleted");
            return ResponseEntity.noContent().build();
        });
    }

    /**
     * Busca estudantes pelo nome exato.
     *
     * @param status nome do estudante
     * @return {@link ResponseEntity} com os estudantes encontrados ou status 404
     */
    @Override
    public ResponseEntity<List<StudentResponseDTO>> findStudentsByStatus(@RequestParam("status") String status) {
        String method = "StudentController.findStudentsByStatus";

        return logExecution(method, () -> {
            logRequest(method, "status=" + status);

            List<StudentResponseDTO> students = studentService.getAllStudents().stream()
                    .filter(s -> s.getStatus().getValue().equalsIgnoreCase(status))
                    .toList();

            if (students.isEmpty()) {
                logResponse(method, HttpStatus.NOT_FOUND, "no entity found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            logResponse(method, HttpStatus.OK, "total=" + students.size());
            return ResponseEntity.ok(students);
        });
    }

    /**
     * Busca estudantes pelo nome exato.
     *
     * @param name nome do estudante
     * @return {@link ResponseEntity} com os estudantes encontrados ou status 404
     */
    @Override
    public ResponseEntity<List<StudentResponseDTO>> findByStudentName(@RequestParam("name") String name) {
        String method = "StudentController.findByStudentName";

        return logExecution(method, () -> {
            logRequest(method, "name=" + name);

            List<StudentResponseDTO> students = studentService.findStudentsByName(name);

            if (students.isEmpty()) {
                logResponse(method, HttpStatus.NOT_FOUND, "no entity found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            logResponse(method, HttpStatus.OK, "total=" + students.size());
            return ResponseEntity.ok(students);
        });
    }

    /**
     * Lista alunos de uma escola específica.
     *
     * @param schoolId identificador da escola
     * @return {@link ResponseEntity} contendo a lista de alunos encontrados
     */
    @Override
    public ResponseEntity<List<StudentResponseDTO>> findStudentsBySchool(@PathVariable("schoolId") Long schoolId) {
        String method = "StudentController.findStudentsBySchool";

        return logExecution(method, () -> {
            logRequest(method, "schoolId=" + schoolId);

            List<StudentResponseDTO> students = studentService.getStudentsBySchool(schoolId);

            logResponse(method, HttpStatus.OK, "total=" + students.size());
            return ResponseEntity.ok(students);
        });
    }
}
