package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.Degree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DegreeRepository
 * Handles persistence for Degree entities.
 */
@Repository
public interface DegreeRepository extends JpaRepository<Degree, Long> {

    /** All degrees belonging to a specific department. */
    List<Degree> findByDepartmentId(Long departmentId);

    /** Find by unique code (e.g., "BSCS"). */
    Optional<Degree> findByCode(String code);

    /** Duplicate guard. */
    boolean existsByCodeAndDepartmentId(String code, Long departmentId);

    /**
     * Fetch degree with its courseMappings eagerly to avoid
     * N+1 queries when building the course catalog.
     */
    @Query("SELECT d FROM Degree d " +
            "LEFT JOIN FETCH d.courseMappings cm " +
            "LEFT JOIN FETCH cm.course " +
            "WHERE d.id = :id")
    Optional<Degree> findByIdWithCourseMappings(@Param("id") Long id);
}
