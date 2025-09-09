package br.com.edudocs.mapper;

import br.com.edudocs.api.model.EmployeeRequestDTO;
import br.com.edudocs.api.model.EmployeeResponseDTO;
import br.com.edudocs.api.model.RoleDTO;
import br.com.edudocs.entity.EmployeeEntity;
import br.com.edudocs.entity.EmployeeRoleEntity;
import br.com.edudocs.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface EmployeeMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(entity.getRoles()))")
    EmployeeResponseDTO toResponseDto(EmployeeEntity entity);

    List<EmployeeResponseDTO> toResponseDtoList(List<EmployeeEntity> entities);

    EmployeeEntity toEntity(EmployeeRequestDTO dto);

    default List<RoleDTO> mapRoles(Set<EmployeeRoleEntity> employeeRoles) {
        if (employeeRoles == null) return new ArrayList<>();
        return employeeRoles.stream()
                .map(er -> {
                    RoleEntity role = er.getRole();
                    if (role == null) return null;
                    RoleDTO dto = new RoleDTO();
                    dto.setId(role.getId());
                    dto.setName(role.getName());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
