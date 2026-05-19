package ru.ssau.netequip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ssau.netequip.entity.User;
import ru.ssau.netequip.enums.UserRole;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.employee WHERE u.id = :id")
    Optional<User> findByIdWithEmployee(@Param("id") Long id);
    long countByRole(UserRole role);
}
