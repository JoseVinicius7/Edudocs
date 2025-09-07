package br.com.edudocs.service.impl;

import br.com.edudocs.api.model.SchoolRequestDTO;
import br.com.edudocs.api.model.SchoolResponseDTO;
import br.com.edudocs.entity.AddressEntity;
import br.com.edudocs.entity.SchoolEntity;
import br.com.edudocs.mapper.AddressMapper;
import br.com.edudocs.mapper.SchoolMapper;
import br.com.edudocs.repository.AddressRepository;
import br.com.edudocs.repository.SchoolRepository;
import br.com.edudocs.utils.BaseLogger.BaseServiceLogger;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    private SchoolServiceImpl service;

    @BeforeEach
    void setupLogger() {
        // Faz o logger executar o Supplier/Runner para permitir cobrir o corpo dos métodos
        lenient().when(logger.logExecution(anyString(), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        lenient().doAnswer(invocation -> {
            Runnable r = invocation.getArgument(1);
            r.run();
            return null;
        }).when(logger).logExecutionVoid(anyString(), any());
    }

    @Test
    void findSchoolById_returnsMappedDto_whenFound() {
        Long id = 10L;
        SchoolEntity entity = new SchoolEntity();
        entity.setId(id);
        SchoolResponseDTO dto = mock(SchoolResponseDTO.class);

        when(schoolRepository.findById(id)).thenReturn(Optional.of(entity));
        when(schoolMapper.toResponseDto(entity)).thenReturn(dto);

        Optional<SchoolResponseDTO> result = service.findSchoolById(id);

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
        verify(schoolRepository).findById(id);
        verify(schoolMapper).toResponseDto(entity);
    }

    @Test
    void findSchoolById_returnsEmpty_whenNotFound() {
        Long id = 11L;
        when(schoolRepository.findById(id)).thenReturn(Optional.empty());

        Optional<SchoolResponseDTO> result = service.findSchoolById(id);

        assertTrue(result.isEmpty());
        verify(schoolRepository).findById(id);
        verifyNoInteractions(schoolMapper);
    }

    @Test
    void findAllSchools_mapsList() {
        SchoolEntity e1 = new SchoolEntity();
        SchoolEntity e2 = new SchoolEntity();
        List<SchoolEntity> entities = List.of(e1, e2);
        SchoolResponseDTO r1 = mock(SchoolResponseDTO.class);
        SchoolResponseDTO r2 = mock(SchoolResponseDTO.class);
        List<SchoolResponseDTO> responses = List.of(r1, r2);

        when(schoolRepository.findAll()).thenReturn(entities);
        when(schoolMapper.toResponseDtoList(entities)).thenReturn(responses);

        List<SchoolResponseDTO> out = service.findAllSchools();

        assertEquals(2, out.size());
        assertEquals(responses, out);
        verify(schoolRepository).findAll();
        verify(schoolMapper).toResponseDtoList(entities);
    }

    @Test
    void createSchool_throws_whenPayloadAddressIsNull() {
        SchoolRequestDTO payload = mock(SchoolRequestDTO.class);
        when(payload.getAddress()).thenReturn(null);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.createSchool(payload));

        assertTrue(ex.getMessage().contains("Endereço obrigatório"));
        verifyNoInteractions(schoolRepository);
    }

    @Test
    void createSchool_persistsUsingExistingAddress_whenAddressIdPresent() {
        // payload -> entity com address contendo id
        SchoolRequestDTO payload = mock(SchoolRequestDTO.class);
        AddressEntity mappedAddress = new AddressEntity();
        mappedAddress.setId(5L);
        SchoolEntity mappedSchool = new SchoolEntity();
        mappedSchool.setAddress(mappedAddress);

        AddressEntity persistedAddress = new AddressEntity();
        persistedAddress.setId(5L);

        SchoolEntity saved = new SchoolEntity();
        saved.setId(100L);

        SchoolResponseDTO responseDTO = mock(SchoolResponseDTO.class);

        when(payload.getAddress()).thenReturn(mock(br.com.edudocs.api.model.AddressDTO.class));
        when(schoolMapper.toEntity(payload)).thenReturn(mappedSchool);
        when(addressRepository.findById(5L)).thenReturn(Optional.of(persistedAddress));
        when(schoolRepository.save(mappedSchool)).thenReturn(saved);
        when(schoolMapper.toResponseDto(saved)).thenReturn(responseDTO);

        SchoolResponseDTO out = service.createSchool(payload);

        assertEquals(responseDTO, out);
        // Garante que o address foi substituído pelo persistido
        assertEquals(persistedAddress, mappedSchool.getAddress());
        verify(addressRepository).findById(5L);
        verify(schoolRepository).save(mappedSchool);
        verify(schoolMapper).toResponseDto(saved);
    }

    @Test
    void createSchool_throws_whenAddressIdNotFound() {
        SchoolRequestDTO payload = mock(SchoolRequestDTO.class);
        AddressEntity mappedAddress = new AddressEntity();
        mappedAddress.setId(9L);
        SchoolEntity mappedSchool = new SchoolEntity();
        mappedSchool.setAddress(mappedAddress);

        when(payload.getAddress()).thenReturn(mock(br.com.edudocs.api.model.AddressDTO.class));
        when(schoolMapper.toEntity(payload)).thenReturn(mappedSchool);
        when(addressRepository.findById(9L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.createSchool(payload));

        assertTrue(ex.getMessage().contains("Endereço não encontrado"));
        verify(addressRepository).findById(9L);
        verifyNoMoreInteractions(addressRepository);
        verifyNoInteractions(schoolRepository);
    }

    @Test
    void createSchool_savesDirectly_whenMappedAddressIsNullOrIdNull() {
        // Caso address = null
        SchoolRequestDTO payload = mock(SchoolRequestDTO.class);
        SchoolEntity mappedSchool = new SchoolEntity();
        mappedSchool.setAddress(null);

        SchoolEntity saved = new SchoolEntity();
        saved.setId(200L);
        SchoolResponseDTO response = mock(SchoolResponseDTO.class);

        when(payload.getAddress()).thenReturn(mock(br.com.edudocs.api.model.AddressDTO.class));
        when(schoolMapper.toEntity(payload)).thenReturn(mappedSchool);
        when(schoolRepository.save(mappedSchool)).thenReturn(saved);
        when(schoolMapper.toResponseDto(saved)).thenReturn(response);

        SchoolResponseDTO out = service.createSchool(payload);

        assertEquals(response, out);
        verify(schoolRepository).save(mappedSchool);
        verify(schoolMapper).toResponseDto(saved);
        verifyNoInteractions(addressRepository);

        // Caso address.id = null
        AddressEntity a = new AddressEntity(); // id null
        mappedSchool.setAddress(a);

        when(schoolRepository.save(mappedSchool)).thenReturn(saved);

        out = service.createSchool(payload);

        assertEquals(response, out);
        verify(schoolRepository, times(2)).save(mappedSchool);
        verifyNoInteractions(addressRepository);
    }

    @Test
    void updateSchool_updatesAddress_whenAddressProvidedInPayload() {
        Long id = 77L;

        SchoolRequestDTO payload = mock(SchoolRequestDTO.class);
        when(payload.getAddress()).thenReturn(mock(br.com.edudocs.api.model.AddressDTO.class));

        SchoolEntity existing = new SchoolEntity();
        existing.setId(id);
        existing.setAddress(new AddressEntity()); // atual existente

        when(schoolRepository.findById(id)).thenReturn(Optional.of(existing));

        AddressEntity mappedFromPayload = new AddressEntity();
        AddressEntity savedAddress = new AddressEntity();
        savedAddress.setId(300L);
        when(addressMapper.toEntity(any())).thenReturn(mappedFromPayload);
        when(addressRepository.save(mappedFromPayload)).thenReturn(savedAddress);

        SchoolEntity incoming = new SchoolEntity();
        when(schoolMapper.toEntity(payload)).thenReturn(incoming);

        SchoolEntity updated = new SchoolEntity();
        updated.setId(id);
        when(schoolRepository.save(incoming)).thenReturn(updated);

        SchoolResponseDTO responseDTO = mock(SchoolResponseDTO.class);
        when(schoolMapper.toResponseDto(updated)).thenReturn(responseDTO);

        SchoolResponseDTO out = service.updateSchool(id, payload);

        assertEquals(responseDTO, out);
        // address do existing deve ter sido atualizado, e incoming deve manter address (do mapper ou existente se null)
        verify(addressMapper).toEntity(any());
        verify(addressRepository).save(mappedFromPayload);
        verify(schoolRepository).save(incoming);
        verify(schoolMapper).toResponseDto(updated);
        assertEquals(id, incoming.getId());
        assertEquals(savedAddress, existing.getAddress());
    }

    @Test
    void updateSchool_keepsExistingAddress_whenAddressNotProvidedInPayload() {
        Long id = 88L;

        SchoolRequestDTO payload = mock(SchoolRequestDTO.class);
        when(payload.getAddress()).thenReturn(null);

        SchoolEntity existing = new SchoolEntity();
        existing.setId(id);
        AddressEntity current = new AddressEntity();
        current.setId(10L);
        existing.setAddress(current);

        when(schoolRepository.findById(id)).thenReturn(Optional.of(existing));

        SchoolEntity incoming = new SchoolEntity();
        incoming.setAddress(null); // simula mapeamento sem address
        when(schoolMapper.toEntity(payload)).thenReturn(incoming);

        SchoolEntity updated = new SchoolEntity();
        updated.setId(id);
        when(schoolRepository.save(incoming)).thenReturn(updated);

        SchoolResponseDTO responseDTO = mock(SchoolResponseDTO.class);
        when(schoolMapper.toResponseDto(updated)).thenReturn(responseDTO);

        SchoolResponseDTO out = service.updateSchool(id, payload);

        assertEquals(responseDTO, out);
        // incoming deve herdar o address existente
        assertEquals(current, incoming.getAddress());
        verify(schoolRepository).save(incoming);
        verifyNoInteractions(addressRepository, addressMapper);
    }

    @Test
    void updateSchool_throws_whenSchoolNotFound() {
        Long id = 99L;
        SchoolRequestDTO payload = mock(SchoolRequestDTO.class);
        when(schoolRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.updateSchool(id, payload));

        assertTrue(ex.getMessage().contains("Escola não encontrada"));
        verify(schoolRepository).findById(id);
        verifyNoMoreInteractions(schoolRepository);
    }

    @Test
    void deleteSchool_deletes_whenExists() {
        Long id = 1L;
        when(schoolRepository.existsById(id)).thenReturn(true);

        service.deleteSchool(id);

        verify(schoolRepository).existsById(id);
        verify(schoolRepository).deleteById(id);
    }

    @Test
    void deleteSchool_throws_whenNotExists() {
        Long id = 2L;
        when(schoolRepository.existsById(id)).thenReturn(false);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.deleteSchool(id));

        assertTrue(ex.getMessage().contains("Escola não encontrada"));
        verify(schoolRepository).existsById(id);
        verify(schoolRepository, never()).deleteById(anyLong());
    }

    @Test
    void findSchoolsByZone_returnsMappedList() {
        String zone = "URBANA";
        SchoolEntity e = new SchoolEntity();
        List<SchoolEntity> entities = List.of(e);
        List<SchoolResponseDTO> dtos = List.of(mock(SchoolResponseDTO.class));

        when(schoolRepository.findByZone(zone)).thenReturn(entities);
        when(schoolMapper.toResponseDtoList(entities)).thenReturn(dtos);

        List<SchoolResponseDTO> out = service.findSchoolsByZone(zone);

        assertEquals(dtos, out);
        verify(schoolRepository).findByZone(zone);
        verify(schoolMapper).toResponseDtoList(entities);
    }

    @Test
    void findSchoolByName_returnsMappedList() {
        String name = "Escola X";
        List<SchoolEntity> entities = List.of(new SchoolEntity());
        List<SchoolResponseDTO> dtos = List.of(mock(SchoolResponseDTO.class));

        when(schoolRepository.findByName(name)).thenReturn(entities);
        when(schoolMapper.toResponseDtoList(entities)).thenReturn(dtos);

        List<SchoolResponseDTO> out = service.findSchoolByName(name);

        assertEquals(dtos, out);
        verify(schoolRepository).findByName(name);
        verify(schoolMapper).toResponseDtoList(entities);
    }
}
