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
import br.com.edudocs.utils.BaseLogger.BaseServiceLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolServiceImplTest {

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private BaseServiceLogger logger;

    @Mock
    private SchoolMapper schoolMapper;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private SchoolServiceImpl schoolService;

    private SchoolEntity schoolEntity;
    private SchoolRequestDTO schoolRequestDTO;
    private SchoolResponseDTO schoolResponseDTO;
    private AddressEntity addressEntity;

    @BeforeEach
    void setup() {
        addressEntity = new AddressEntity();
        addressEntity.setId(1L);
        addressEntity.setStreet("Rua A");
        addressEntity.setCity("Cidade");
        addressEntity.setState("Estado");
        addressEntity.setPostalCode("12345-678");

        schoolEntity = new SchoolEntity();
        schoolEntity.setId(1L);
        schoolEntity.setName("Escola Teste");
        schoolEntity.setZone("URBANA");
        schoolEntity.setAddress(addressEntity);

        schoolRequestDTO = new SchoolRequestDTO();
        schoolRequestDTO.setName("Escola Teste");
        schoolRequestDTO.setZone(SchoolRequestDTO.ZoneEnum.valueOf("URBANA"));

        schoolResponseDTO = new SchoolResponseDTO();
        schoolResponseDTO.setId(1L);
        schoolResponseDTO.setName("Escola Teste");
        schoolResponseDTO.setZone(SchoolResponseDTO.ZoneEnum.valueOf("URBANA"));

        // Evita duplicar logExecution em todos os métodos
        lenient().when(logger.logExecution(any(), any())).thenAnswer(invocation -> {
            var supplier = invocation.<java.util.function.Supplier<?>>getArgument(1);
            return supplier.get();
        });

        lenient().doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        }).when(logger).logExecutionVoid(any(), any());

    }

    @Test
    void findSchoolById_ShouldReturnSchool_WhenFound() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(schoolEntity));
        when(schoolMapper.toResponseDto(schoolEntity)).thenReturn(schoolResponseDTO);

        Optional<SchoolResponseDTO> result = schoolService.findSchoolById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Escola Teste");
        verify(schoolRepository).findById(1L);
    }

    @Test
    void findSchoolById_ShouldReturnEmpty_WhenNotFound() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<SchoolResponseDTO> result = schoolService.findSchoolById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllSchools_ShouldReturnList() {
        when(schoolRepository.findAll()).thenReturn(List.of(schoolEntity));
        when(schoolMapper.toResponseDtoList(List.of(schoolEntity))).thenReturn(List.of(schoolResponseDTO));

        List<SchoolResponseDTO> result = schoolService.findAllSchools();

        assertThat(result).hasSize(1);
        verify(schoolRepository).findAll();
    }

    @Test
    void createSchool_ShouldThrowBadRequest_WhenNameIsNull() {
        schoolRequestDTO.setName(null);

        assertThatThrownBy(() -> schoolService.createSchool(schoolRequestDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Nome da escola é obrigatório.");
    }

    @Test
    void createSchool_ShouldThrowBadRequest_WhenZoneIsInvalid() {
        schoolRequestDTO.setZone(null);

        assertThatThrownBy(() -> schoolService.createSchool(schoolRequestDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Zona inválida. Valores aceitos: RURAL, URBANA.");
    }

    @Test
    void createSchool_ShouldThrowConflict_WhenSchoolAlreadyExists() {
        when(schoolRepository.existsByName("Escola Teste")).thenReturn(true);

        assertThatThrownBy(() -> schoolService.createSchool(schoolRequestDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Já existe uma escola com este nome.");
    }

    @Test
    void createSchool_ShouldCreateSuccessfully() {
        when(schoolRepository.existsByName("Escola Teste")).thenReturn(false);
        when(schoolMapper.toEntity(schoolRequestDTO)).thenReturn(schoolEntity);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(addressEntity));
        when(schoolRepository.save(any(SchoolEntity.class))).thenReturn(schoolEntity);


        SchoolResponseDTO result = schoolService.createSchool(schoolRequestDTO);

        verify(schoolRepository).save(schoolEntity);
    }

    @Test
    void updateSchool_ShouldUpdateSuccessfully() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(schoolEntity));
        when(schoolMapper.toEntity(schoolRequestDTO)).thenReturn(schoolEntity);
        when(schoolRepository.save(any(SchoolEntity.class))).thenReturn(schoolEntity);
        when(schoolMapper.toResponseDto(schoolEntity)).thenReturn(schoolResponseDTO);

        SchoolResponseDTO result = schoolService.updateSchool(1L, schoolRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Escola Teste");
        verify(schoolRepository).save(any(SchoolEntity.class));
    }

    @Test
    void updateSchool_ShouldThrowNotFound_WhenSchoolDoesNotExist() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolService.updateSchool(1L, schoolRequestDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Escola não encontrada com o ID: 1");
    }

    @Test
    void deleteSchool_ShouldDeleteSuccessfully() {
        when(schoolRepository.existsById(1L)).thenReturn(true);
        doNothing().when(schoolRepository).deleteById(1L);

        schoolService.deleteSchool(1L);

        verify(schoolRepository).deleteById(1L);
    }

    @Test
    void deleteSchool_ShouldThrowNotFound_WhenSchoolDoesNotExist() {
        when(schoolRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> schoolService.deleteSchool(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Escola não encontrada com o ID: 1");
    }

    @Test
    void findSchoolsByZone_ShouldReturnList() {
        when(schoolRepository.findByZone("URBANA")).thenReturn(List.of(schoolEntity));
        when(schoolMapper.toResponseDtoList(List.of(schoolEntity))).thenReturn(List.of(schoolResponseDTO));

        List<SchoolResponseDTO> result = schoolService.findSchoolsByZone("URBANA");

        assertThat(result).hasSize(1);
        verify(schoolRepository).findByZone("URBANA");
    }

    @Test
    void findSchoolByName_ShouldReturnList() {
        when(schoolRepository.findByName("Escola Teste")).thenReturn(List.of(schoolEntity));
        when(schoolMapper.toResponseDtoList(List.of(schoolEntity))).thenReturn(List.of(schoolResponseDTO));

        List<SchoolResponseDTO> result = schoolService.findSchoolByName("Escola Teste");

        assertThat(result).hasSize(1);
        verify(schoolRepository).findByName("Escola Teste");
    }
}
