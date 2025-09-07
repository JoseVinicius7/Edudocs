package br.com.edudocs.service;

import br.com.edudocs.api.model.SchoolRequestDTO;
import br.com.edudocs.api.model.SchoolResponseDTO;
import br.com.edudocs.entity.SchoolEntity;
import java.util.List;
import java.util.Optional;

public interface SchoolService {
    
    /**
     * Cria uma nova escola
     * @param school entidade escola a ser criada
     * @return escola criada
     */
    SchoolResponseDTO createSchool(SchoolRequestDTO school);
    
    /**
     * Busca uma escola pelo ID
     * @param id identificador da escola
     * @return Optional contendo a escola se encontrada
     */
    Optional<SchoolResponseDTO> findSchoolById(Long id);
    
    /**
     * Lista todas as escolas
     * @return lista de escolas
     */
    List<SchoolResponseDTO> findAllSchools();
    
    /**
     * Atualiza uma escola existente
     * @param id identificador da escola
     * @param school dados atualizados da escola
     * @return escola atualizada
     */
    SchoolResponseDTO updateSchool(Long id, SchoolRequestDTO school);
    
    /**
     * Remove uma escola
     * @param id identificador da escola
     */
    void deleteSchool(Long id);
    
    /**
     * Busca escolas por zona
     * @param zone zona da escola
     * @return lista de escolas na zona especificada
     */
    List<SchoolResponseDTO> findSchoolsByZone(String zone);
    
    /**
     * Busca escola pelo nome
     * @param name nome da escola
     * @return Optional contendo a escola se encontrada
     */
    List<SchoolResponseDTO> findSchoolByName(String name);
}
