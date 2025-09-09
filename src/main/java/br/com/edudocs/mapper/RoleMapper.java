package br.com.edudocs.mapper;

import br.com.edudocs.api.model.RoleDTO;
import br.com.edudocs.entity.RoleEntity;
import org.mapstruct.Mapper;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleEntity toEntity(RoleDTO dto);

    RoleDTO toDto(RoleEntity entity);

    default Set<RoleEntity> toRoleEntitySet(List<RoleDTO> roles) {
        if (roles == null) return new HashSet<>();
        return roles.stream()
                .filter(Objects::nonNull)
                .map(r -> {
                    RoleEntity entity = new RoleEntity();
                    entity.setId(r.getId());
                    entity.setName(r.getName());
                    return entity;
                })
                .collect(Collectors.toSet());
    }

}
