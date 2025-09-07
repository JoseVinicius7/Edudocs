package br.com.edudocs.service.impl;

import br.com.edudocs.api.model.SchoolRequestDTO;
import br.com.edudocs.api.model.SchoolResponseDTO;
import br.com.edudocs.entity.AddressEntity;
import br.com.edudocs.entity.SchoolEntity;
import br.com.edudocs.exception.BadRequestException;
import br.com.edudocs.exception.ConflictException;
import br.com.edudocs.exception.NotFoundException;
import br.com.edudocs.mapper.AddressMapper;
import br.com.edudocs.mapper.SchoolMapper;
import br.com.edudocs.repository.AddressRepository;
import br.com.edudocs.repository.SchoolRepository;
import br.com.edudocs.service.SchoolService;
import br.com.edudocs.utils.BaseLogger.BaseServiceLogger;
import br.com.edudocs.utils.Zona;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementação do {@link SchoolService} para gerenciar operações de escola.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Criar, atualizar e excluir escolas.</li>
 *   <li>Buscar escolas por ‘ID’, nome ou zona.</li>
 *   <li>Garantir persistência correta do endereço associado.</li>
 *   <li>Padronizar ‘logs’ de execução com medição de tempo e informações detalhadas.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final AddressRepository addressRepository;
    private final BaseServiceLogger logger;
    private final SchoolMapper schoolMapper;
    private final AddressMapper addressMapper;

    /**
     * Busca uma escola pelo seu ‘ID’.
     *
     * @param id identificador da escola
     * @return {@link Optional} contendo {@link SchoolResponseDTO} se encontrada
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<SchoolResponseDTO> findSchoolById(Long id) {
        String method = "SchoolService.findSchoolById";

        return logger.logExecution(method, () -> {
            log.info("{} - searching for school - id={}", method, id);
            return schoolRepository.findById(id)
                    .map(schoolMapper::toResponseDto);
        });
    }

    /**
     * Retorna todas as escolas cadastradas.
     *
     * @return lista de {@link SchoolResponseDTO}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SchoolResponseDTO> findAllSchools() {
        String method = "SchoolService.findAllSchools";

        return logger.logExecution(method, () -> {
            List<SchoolEntity> schools = schoolRepository.findAll();
            log.info("{} - total schools found={}", method, schools.size());
            return schoolMapper.toResponseDtoList(schools);
        });
    }

    /**
     * Cria uma escola.
     *
     * @param payload dados da escola a ser criada
     * @return {@link SchoolResponseDTO} criado
     * @throws BadRequestException quando dados obrigatórios/zone são inválidos
     * @throws ConflictException quando já existe escola com o mesmo nome
     */
    @Override
    public SchoolResponseDTO createSchool(SchoolRequestDTO payload) {
        String method = "SchoolService.createSchool";

        return logger.logExecution(method, () -> {
            log.info("{} - creating school - name={} - zone={}", method, payload.getName(), payload.getZone());

            if (payload.getName() == null || payload.getName().trim().isEmpty()) {
                throw new BadRequestException("Nome da escola é obrigatório.");
            }

            String normalizedZone = payload.getZone() != null ? Zona.normalize(payload.getZone().getValue()) : null;
            if (normalizedZone == null || !Zona.isValid(normalizedZone)) {
                throw new BadRequestException("Zona inválida. Valores aceitos: RURAL, URBANA.");
            }

            if (schoolRepository.existsByName(payload.getName().trim())) {
                throw new ConflictException("Já existe uma escola com este nome.");
            }

            SchoolEntity school = schoolMapper.toEntity(payload);

            if (school.getAddress() != null && school.getAddress().getId() != null) {
                AddressEntity persistedAddress = loadExistingAddress(school.getAddress().getId(), method);
                school.setAddress(persistedAddress);
            }

            school.setZone(normalizedZone);

            SchoolEntity saved = schoolRepository.save(school);
            log.info("{} - school saved - id={}", method, saved.getId());
            return schoolMapper.toResponseDto(saved);
        });
    }


    /**
     * Atualiza os dados de uma escola existente.
     *
     * @param id      identificador da escola
     * @param payload dados atualizados
     * @return {@link SchoolResponseDTO} atualizado
     * @throws NotFoundException se a escola não for encontrada
     */
    @Override
    public SchoolResponseDTO updateSchool(Long id, SchoolRequestDTO payload) {
        String method = "SchoolService.updateSchool";

        return logger.logExecution(method, () -> {
            log.info("{} - updating school - id={} - name={} - zone={}", method, id, payload.getName(), payload.getZone());

            Optional<SchoolEntity> existingSchoolOpt = schoolRepository.findById(id);

            if (existingSchoolOpt.isPresent()) {
                SchoolEntity existing = existingSchoolOpt.get();

                if (payload.getAddress() != null) {
                    log.info("{} - updating address for school id={}", method, id);
                    AddressEntity addressEntity = addressMapper.toEntity(payload.getAddress());
                    AddressEntity savedAddress = addressRepository.save(addressEntity);
                    existing.setAddress(savedAddress);
                } else {
                    log.info("{} - keeping current address for school id={}", method, id);
                }

                SchoolEntity incoming = schoolMapper.toEntity(payload);
                incoming.setId(id);
                if (incoming.getAddress() == null) {
                    incoming.setAddress(existing.getAddress());
                }

                SchoolEntity updated = schoolRepository.save(incoming);
                log.info("{} - school updated successfully - id={}", method, updated.getId());
                return schoolMapper.toResponseDto(updated);
            }

            log.info("{} - school not found - id={}", method, id);
            throw new NotFoundException("Escola não encontrada com o ID: " + id);
        });
    }

    /**
     * Exclui uma escola pelo ‘ID’.
     *
     * @param id identificador da escola
     * @throws NotFoundException se a escola não for encontrada
     */
    @Override
    public void deleteSchool(Long id) {
        String method = "SchoolService.deleteSchool";

        logger.logExecutionVoid(method, () -> {
            log.info("{} - deleting school - id={}", method, id);

            boolean exists = schoolRepository.existsById(id);
            if (!exists) {
                log.info("{} - school not found to delete - id={}", method, id);
                throw new NotFoundException("Escola não encontrada com o ID: " + id);
            }

            schoolRepository.deleteById(id);
            log.info("{} - school deleted successfully", method);
        });
    }

    /**
     * Busca escolas filtrando por zona (rural ou urbana).
     *
     * @param zone tipo de zona
     * @return lista de {@link SchoolResponseDTO} encontradas
     */
    @Override
    @Transactional(readOnly = true)
    public List<SchoolResponseDTO> findSchoolsByZone(String zone) {
        String method = "SchoolService.findSchoolsByZone";

        return logger.logExecution(method, () -> {
            log.info("{} - searching schools by zone - zone={}", method, zone);
            List<SchoolEntity> schools = schoolRepository.findByZone(zone);
            log.info("{} - total schools found={}", method, schools.size());
            return schoolMapper.toResponseDtoList(schools);
        });
    }

    /**
     * Busca escolas pelo nome exato.
     *
     * @param name nome da escola
     * @return lista de {@link SchoolResponseDTO} encontradas
     */
    @Override
    @Transactional(readOnly = true)
    public List<SchoolResponseDTO> findSchoolByName(String name) {
        String method = "SchoolService.findSchoolByName";

        return logger.logExecution(method, () -> {
            log.info("{} - searching school by name - name={}", method, name);
            return schoolMapper.toResponseDtoList(schoolRepository.findByName(name));
        });
    }

    /**
     * Carrega um endereço existente a partir do ‘ID’.
     *
     * @param addressId ‘ID’ do endereço
     * @param method    nome do metodo para ‘log’
     * @return {@link AddressEntity} encontrado
     * @throws NotFoundException se não encontrado
     */
    private AddressEntity loadExistingAddress(Long addressId, String method) {
        log.info("{} - resolving existing address - id={}", method, addressId);
        return addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("{} - address not found - id={}", method, addressId);
                    return new NotFoundException("Endereço não encontrado: id=" + addressId);
                });
    }
}
