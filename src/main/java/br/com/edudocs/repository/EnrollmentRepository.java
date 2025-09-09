package br.com.edudocs.repository;

import br.com.edudocs.entity.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    @Query("""
                SELECT e FROM EnrollmentEntity e
                WHERE (:studentId IS NULL OR e.student.id = :studentId)
                  AND (:schoolId IS NULL OR e.school.id = :schoolId)
                  AND (:status IS NULL OR e.status = :status)
            """)
    List<EnrollmentEntity> findByFilters(@Param("studentId") Long studentId, @Param("schoolId") Long schoolId, @Param("status") String status);

    Optional<EnrollmentEntity> findByStudentIdAndSchoolIdAndStatus(Long id, Long id1, EnrollmentEntity.Status status);
}
