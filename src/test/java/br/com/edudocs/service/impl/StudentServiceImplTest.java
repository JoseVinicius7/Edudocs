package br.com.edudocs.service.impl;

import br.com.edudocs.api.model.StudentRequestDTO;
import br.com.edudocs.api.model.StudentResponseDTO;
import br.com.edudocs.entity.SchoolEntity;
import br.com.edudocs.entity.StudentEntity;
import br.com.edudocs.exception.BadRequestException;
import br.com.edudocs.exception.NotFoundException;
import br.com.edudocs.mapper.StudentMapper;
import br.com.edudocs.repository.SchoolRepository;
import br.com.edudocs.repository.StudentRepository;
import br.com.edudocs.utils.BaseLogger.BaseServiceLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private BaseServiceLogger logger;

    @InjectMocks
    private StudentServiceImpl studentService;

    private StudentRequestDTO studentRequest;
    private StudentEntity studentEntity;
    private StudentResponseDTO studentDTO;
    private SchoolEntity schoolEntity;

    @BeforeEach
    void setup() {
        schoolEntity = new SchoolEntity();
        schoolEntity.setId(1L);

        studentRequest = new StudentRequestDTO();
        studentRequest.setName("João");
        studentRequest.setBirthDate(LocalDate.of(2010, 1, 1));
        studentRequest.setGender(StudentRequestDTO.GenderEnum.valueOf(String.valueOf(StudentEntity.Gender.MALE)));
        studentRequest.setSchoolId(1L);

        studentEntity = new StudentEntity();
        studentEntity.setId(100L);
        studentEntity.setName(studentRequest.getName());
        studentEntity.setBirthDate(studentRequest.getBirthDate());
        studentEntity.setGender(StudentEntity.Gender.valueOf(studentRequest.getGender().getValue()));
        studentEntity.setSchool(schoolEntity);

        studentDTO = new StudentResponseDTO();
        studentDTO.setId(studentEntity.getId());
        studentDTO.setName(studentEntity.getName());
        studentDTO.setBirthDate(studentEntity.getBirthDate());
        studentDTO.setGender(StudentResponseDTO.GenderEnum.valueOf(studentRequest.getGender().getValue()));
        studentDTO.setSchoolId(schoolEntity.getId());


        // Mock do logger para executar a lambda real
        lenient().when(logger.logExecution(anyString(), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Supplier<?> lambda = invocation.getArgument(1);
            return lambda.get();
        });

    }

    @Test
    void createStudent_success() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(schoolEntity));
        when(studentMapper.toEntity(studentRequest)).thenReturn(studentEntity);
        when(studentRepository.save(studentEntity)).thenReturn(studentEntity);
        when(studentMapper.toResponseDto(studentEntity)).thenReturn(studentDTO);

        StudentResponseDTO result = studentService.createStudent(studentRequest);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(studentRepository, times(1)).save(studentEntity);
    }

    @Test
    void createStudent_schoolNotFound_shouldThrow() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> studentService.createStudent(studentRequest));

        assertEquals("School not found with id: 1", exception.getMessage());
    }

    @Test
    void createStudent_missingName_shouldThrow() {
        studentRequest.setName(null);
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> studentService.createStudent(studentRequest));
        assertEquals("Student name is required.", exception.getMessage());
    }

    @Test
    void getStudentById_shouldReturnStudentDTO() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(studentEntity));
        when(studentMapper.toResponseDto(studentEntity)).thenReturn(studentDTO);

        StudentResponseDTO result = studentService.getStudentById(1L);
        assertEquals(studentDTO.getId(), result.getId());
    }

    @Test
    void getStudentById_shouldThrowNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> studentService.getStudentById(1L));
        assertTrue(ex.getMessage().contains("Aluno não encontrado"));
    }

    @Test
    void getAllStudents_shouldReturnList() {
        when(studentRepository.findAll()).thenReturn(List.of(studentEntity));
        when(studentMapper.toResponseDto(studentEntity)).thenReturn(studentDTO);

        List<StudentResponseDTO> result = studentService.getAllStudents();
        assertEquals(1, result.size());
        assertEquals(studentDTO.getId(), result.get(0).getId());
    }

    @Test
    void getStudentsBySchool_shouldReturnList() {
        when(studentRepository.findBySchoolId(1L)).thenReturn(List.of(studentEntity));
        when(studentMapper.toResponseDtoList(anyList())).thenReturn(List.of(studentDTO));

        List<StudentResponseDTO> result = studentService.getStudentsBySchool(1L);
        assertEquals(1, result.size());
    }

    @Test
    void updateStudent_shouldReturnUpdatedStudent() {
        studentRequest.setName("João Atualizado");
        studentRequest.setBirthDate(LocalDate.of(2000, 1, 1));
        studentDTO.setGender(StudentResponseDTO.GenderEnum.valueOf(StudentEntity.Gender.MALE.name()));
        studentRequest.setSchoolId(1L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(studentEntity));
        when(studentMapper.mapSchoolIdToEntity(1L)).thenReturn(schoolEntity);
        when(studentRepository.save(studentEntity)).thenReturn(studentEntity);
        when(studentMapper.toResponseDto(studentEntity)).thenReturn(studentDTO);

        StudentResponseDTO result = studentService.updateStudent(1L, studentRequest);
        assertEquals(studentDTO.getId(), result.getId());
    }

    @Test
    void deleteStudent_shouldMarkInactive() {
        studentService.deleteStudent(1L);
        assertEquals(StudentEntity.StudentStatus.ACTIVE, studentEntity.getStatus());
    }
}
