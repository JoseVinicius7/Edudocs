package br.com.edudocs.repository;

import br.com.edudocs.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    
    // Busca endereços por cidade
    List<AddressEntity> findByCity(String city);
    
    // Busca endereços por estado
    List<AddressEntity> findByState(String state);
    
    // Busca endereços por CEP
    Optional<AddressEntity> findByPostalCode(String postalCode);
    
    // Busca endereços por cidade e estado
    List<AddressEntity> findByCityAndState(String city, String state);
}
