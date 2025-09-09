package br.com.edudocs.mapper;

import br.com.edudocs.api.model.EnrollmentRequestDTO;
import br.com.edudocs.api.model.EnrollmentResponseDTO;
import br.com.edudocs.entity.EnrollmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper responsável por converter entre {@link EnrollmentEntity} e seus DTOs.
 */
@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    /**
     * Converte um {@link EnrollmentRequestDTO} em {@link EnrollmentEntity}.
     *
     * @param dto dados recebidos na requisição
     * @return entidade de matrícula
     */
    @Mapping(source = "studentId", target = "student.id")
    @Mapping(source = "schoolId", target = "school.id")
    EnrollmentEntity toEntity(EnrollmentRequestDTO dto);

    /**
     * Converte uma {@link EnrollmentEntity} em {@link EnrollmentResponseDTO}.
     *
     * @param entity entidade de matrícula
     * @return DTO de resposta
     */
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "school.id", target = "schoolId")
    EnrollmentResponseDTO toResponseDto(EnrollmentEntity entity);

    /**
     * Converte uma lista de {@link EnrollmentEntity} em uma lista de {@link EnrollmentResponseDTO}.
     *
     * @param entities lista de entidades
     * @return lista de DTOs de resposta
     */
    List<EnrollmentResponseDTO> toResponseDtoList(List<EnrollmentEntity> entities);
}
