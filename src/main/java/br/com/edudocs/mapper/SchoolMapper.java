package br.com.edudocs.mapper;

import br.com.edudocs.api.model.SchoolRequestDTO;
import br.com.edudocs.api.model.SchoolResponseDTO;
import br.com.edudocs.entity.SchoolEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SchoolMapper {

    // Criação: DTO de request -> Entidade
    @Mapping(target = "zone", expression = "java(dto.getZone() != null ? dto.getZone().getValue() : null)")
    @Mapping(target = "address", source = "address")
    SchoolEntity toEntity(SchoolRequestDTO dto);

    // Entidade -> DTO de resposta
    @Mapping(target = "zone", expression = "java(entity.getZone() != null ? br.com.edudocs.api.model.SchoolResponseDTO.ZoneEnum.fromValue(entity.getZone()) : null)")
    @Mapping(target = "address", source = "address")
    SchoolResponseDTO toResponseDto(SchoolEntity entity);

    // Lista de entidades -> Lista de DTOs de resposta
    List<SchoolResponseDTO> toResponseDtoList(List<SchoolEntity> entities);

    // Se SchoolEntity.zone for enum no futuro, troque as expressions para converter enum <-> enum adequadamente.
}
