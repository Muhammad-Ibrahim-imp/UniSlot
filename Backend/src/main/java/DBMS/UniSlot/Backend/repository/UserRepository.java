package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — used by Spring Security's UserDetailsService
 * to load users by email (username) during login.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Primary lookup used during JWT authentication. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}