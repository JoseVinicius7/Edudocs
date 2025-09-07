package br.com.edudocs.controller;

import br.com.edudocs.api.SchoolApi;
import br.com.edudocs.api.model.SchoolRequestDTO;
import br.com.edudocs.api.model.SchoolResponseDTO;
import br.com.edudocs.service.SchoolService;
import br.com.edudocs.utils.BaseLogger.BaseControllerLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador responsável por gerenciar operações relacionadas a escolas.
 *
 * <p>Funcionalidades:</p>
 * <ul>
 *   <li>Criação de novas escolas.</li>
 *   <li>Busca por ID, nome ou zona.</li>
 *   <li>Atualização e exclusão de escolas.</li>
 *   <li>Listagem de todas as escolas.</li>
 * </ul>
 *
 * <p>Padronização de logs:</p>
 * <ul>
 *   <li>Início e fim do metodo com medição de tempo.</li>
 *   <li>Request: campos relevantes do payload ou parâmetros.</li>
 *   <li>Response: status HTTP e identificadores.</li>
 * </ul>
 */

@RequiredArgsConstructor
@Validated
@RestController
public class SchoolController extends BaseControllerLogger implements SchoolApi {

    private final SchoolService schoolService;

    /**
     * Cria uma nova escola.
     *
     * <p>Regras:</p>
     * <ul>
     *   <li>Valida os dados enviados no payload.</li>
     *   <li>Retorna status 201 (Created) ao sucesso.</li>
     * </ul>
     *
     * @param school dados da escola a serem criados
     * @return {@link ResponseEntity} contendo a escola criada
     */
    @Override
    public ResponseEntity<SchoolResponseDTO> create(@Valid SchoolRequestDTO school) {
        String method = "SchoolController.create";

        return logExecution(method, () -> {
            logRequest(method, String.format("name=%s - zone=%s", school.getName(), school.getZone()));

            SchoolResponseDTO created = schoolService.createSchool(school);

            logResponse(method, HttpStatus.CREATED, "id=" + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        });
    }

    /**
     * Busca uma escola pelo seu ID.
     *
     * <p>Regras:</p>
     * <ul>
     *   <li>Se encontrada, retorna 200 (OK) com os dados da escola.</li>
     *   <li>Se não encontrada, retorna 404 (Not Found).</li>
     * </ul>
     *
     * @param id identificador único da escola
     * @return {@link ResponseEntity} com a escola encontrada ou status 404
     */
    @Override
    public ResponseEntity<SchoolResponseDTO> findById(Long id) {
        String method = "SchoolController.findById";

        return logExecution(method, () -> {
            logRequest(method, "id=" + id);

            return schoolService.findSchoolById(id)
                    .map(entity -> {
                        logResponse(method, HttpStatus.OK, "id=" + entity.getId());
                        return ResponseEntity.ok(entity);
                    })
                    .orElseGet(() -> {
                        logResponse(method, HttpStatus.NOT_FOUND, "no entity found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        });
    }

    /**
     * Lista todas as escolas cadastradas.
     *
     * <p>Regras:</p>
     * <ul>
     *   <li>Retorna sempre status 200 (OK).</li>
     *   <li>Se não houver registros, retorna lista vazia.</li>
     * </ul>
     *
     * @return {@link ResponseEntity} contendo a lista de escolas
     */
    @Override
    public ResponseEntity<List<SchoolResponseDTO>> findAll() {
        String method = "SchoolController.findAll";

        return logExecution(method, () -> {
            List<SchoolResponseDTO> schools = schoolService.findAllSchools();
            logResponse(method, HttpStatus.OK, "total=" + schools.size());
            return ResponseEntity.ok(schools);
        });
    }

    /**
     * Atualiza os dados de uma escola existente.
     *
     * <p>Regras:</p>
     * <ul>
     *   <li>Valida os dados enviados no payload.</li>
     *   <li>Se a escola existir, retorna 200 (OK) com os dados atualizados.</li>
     *   <li>Se não existir, retorna 404 (Not Found).</li>
     * </ul>
     *
     * @param id identificador único da escola a ser atualizada
     * @param school dados atualizados da escola
     * @return {@link ResponseEntity} com os dados atualizados ou status 404
     */
    @Override
    public ResponseEntity<SchoolResponseDTO> update(Long id, @Valid SchoolRequestDTO school) {
        String method = "SchoolController.update";

        return logExecution(method, () -> {
            logRequest(method, String.format("id=%d - name=%s - zone=%s", id, school.getName(), school.getZone()));

            try {
                SchoolResponseDTO updated = schoolService.updateSchool(id, school);
                logResponse(method, HttpStatus.OK, "id=" + updated.getId());
                return ResponseEntity.ok(updated);
            } catch (RuntimeException ex) {
                logResponse(method, HttpStatus.NOT_FOUND, "reason=" + ex.getClass().getSimpleName());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        });
    }

    /**
     * Remove uma escola pelo seu ID.
     *
     * <p>Regras:</p>
     * <ul>
     *   <li>Se a escola existir, retorna status 204 (No Content).</li>
     *   <li>Se não existir, retorna status 404 (Not Found).</li>
     * </ul>
     *
     * @param id identificador único da escola
     * @return {@link ResponseEntity} sem conteúdo em caso de sucesso
     */
    @Override
    public ResponseEntity<Void> delete(Long id) {
        String method = "SchoolController.delete";

        return logExecution(method, () -> {
            logRequest(method, "id=" + id);

            schoolService.deleteSchool(id);

            logResponse(method, HttpStatus.NO_CONTENT, "deleted");
            return ResponseEntity.noContent().build();
        });
    }

    /**
     * Busca escolas filtrando pela zona (rural ou urbana).
     *
     * <p>Regras:</p>
     * <ul>
     *   <li>Retorna 200 (OK) com a lista de escolas filtradas.</li>
     *   <li>Se não houver registros, retorna lista vazia.</li>
     * </ul>
     *
     * @param zone tipo da zona (rural ou urbana)
     * @return {@link ResponseEntity} contendo a lista de escolas encontradas
     */
    @Override
    public ResponseEntity<List<SchoolResponseDTO>> findByZone(String zone) {
        String method = "SchoolController.findByZone";

        return logExecution(method, () -> {
            logRequest(method, "zone=" + zone);

            List<SchoolResponseDTO> schools = schoolService.findSchoolsByZone(zone);
            logResponse(method, HttpStatus.OK, "total=" + schools.size());
            return ResponseEntity.ok(schools);
        });
    }

    /**
     * Busca uma escola pelo nome exato.
     *
     * <p>Regras:</p>
     * <ul>
     *   <li>Se encontrada, retorna 200 (OK) com os dados da escola.</li>
     *   <li>Se não encontrada, retorna 404 (Not Found).</li>
     * </ul>
     *
     * @param name nome da escola a ser buscada
     * @return {@link ResponseEntity} com a escola encontrada ou status 404
     */
    @Override
    public ResponseEntity<List<SchoolResponseDTO>> findByName(String name) {
        String method = "SchoolController.findByName";

        return logExecution(method, () -> {
            logRequest(method, "name=" + name);

            List<SchoolResponseDTO> schools = schoolService.findSchoolByName(name);
            logResponse(method, schools.isEmpty() ? HttpStatus.NOT_FOUND : HttpStatus.OK,
                    schools.isEmpty() ? "no entity found" : "total=" + schools.size());
            return ResponseEntity.ok(schools);
        });
    }
}
