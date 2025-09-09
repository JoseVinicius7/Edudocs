package br.com.edudocs.mapper;

import br.com.edudocs.api.model.StudentRequestDTO;
import br.com.edudocs.api.model.StudentResponseDTO;
import br.com.edudocs.entity.SchoolEntity;
import br.com.edudocs.entity.StudentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(source = "schoolId", target = "school", qualifiedByName = "mapSchoolIdToEntity")
    StudentEntity toEntity(StudentRequestDTO dto);

    @Mapping(source = "school.id", target = "schoolId")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    StudentResponseDTO toResponseDto(StudentEntity entity);

    List<StudentResponseDTO> toResponseDtoList(List<StudentEntity> entities);

    @Named("mapSchoolIdToEntity")
    default SchoolEntity mapSchoolIdToEntity(Long schoolId) {
        if (schoolId == null) return null;
        SchoolEntity school = new SchoolEntity();
        school.setId(schoolId);
        return school;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    default LocalDateTime map(OffsetDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }
}
