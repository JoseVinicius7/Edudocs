package br.com.edudocs.mapper;

import br.com.edudocs.api.model.AddressDTO;
import br.com.edudocs.entity.AddressEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressEntity toEntity(AddressDTO dto);

}
