package br.com.edudocs.repository;

import br.com.edudocs.entity.SchoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<SchoolEntity, Long> {
    
    // Busca escola pelo nome
    List<SchoolEntity> findByName(String name);
    
    // Busca escolas por zona
    List<SchoolEntity> findByZone(String zone);
    
    // Verifica se existe escola com determinado nome
    boolean existsByName(String name);
    
    // Busca escolas que contenham determinado texto no nome
    List<SchoolEntity> findByNameContainingIgnoreCase(String namePart);
}
