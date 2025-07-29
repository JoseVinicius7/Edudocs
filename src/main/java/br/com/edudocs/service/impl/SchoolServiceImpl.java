package br.com.edudocs.service.impl;

import br.com.edudocs.entity.SchoolEntity;
import br.com.edudocs.repository.AddressRepository;
import br.com.edudocs.repository.SchoolRepository;
import br.com.edudocs.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SchoolServiceImpl implements SchoolService {
    
    private final SchoolRepository schoolRepository;
    private final AddressRepository addressRepository;
    
    @Override
    public SchoolEntity createSchool(SchoolEntity school) {
        if (school.getAddress() != null) {
            addressRepository.save(school.getAddress());
        }
        return schoolRepository.save(school);
    }
    
    @Override
    public Optional<SchoolEntity> findSchoolById(Long id) {
        return schoolRepository.findById(id);
    }
    
    @Override
    public List<SchoolEntity> findAllSchools() {
        return schoolRepository.findAll();
    }
    
    @Override
    @Transactional
    public SchoolEntity updateSchool(Long id, SchoolEntity school) {
        Optional<SchoolEntity> existingSchool = schoolRepository.findById(id);
        if (existingSchool.isPresent()) {
            school.setId(id);
            if (school.getAddress() != null) {
                addressRepository.save(school.getAddress());
            }
            return schoolRepository.save(school);
        }
        throw new RuntimeException("Escola não encontrada com o ID: " + id);
    }
    
    @Override
    @Transactional
    public void deleteSchool(Long id) {
        schoolRepository.deleteById(id);
    }
    
    @Override
    public List<SchoolEntity> findSchoolsByZone(String zone) {
        return schoolRepository.findByZone(zone);
    }
    
    @Override
    public Optional<SchoolEntity> findSchoolByName(String name) {
        return schoolRepository.findByName(name);
    }
}
