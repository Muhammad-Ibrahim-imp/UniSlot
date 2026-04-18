package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * DepartmentRepository
 * Provides CRUD + custom finders for Department entities.
 * Spring Data JPA auto-implements all methods at runtime.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /** Find department by its unique code (e.g., "CS"). */
    Optional<Department> findByCode(String code);

    /** Check for duplicate name before saving. */
    boolean existsByName(String name);

    boolean existsByCode(String code);
}
