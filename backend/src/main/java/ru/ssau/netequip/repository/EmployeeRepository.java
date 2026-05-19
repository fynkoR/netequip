package ru.ssau.netequip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByFullName(String employeeName);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByPosition(UserRole position);
    boolean existsByEmail(String email);
    List<Employee> findByPositionOrderByFullNameAsc(UserRole position);
    @Query("""
    SELECT e FROM Employee e
    WHERE LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(COALESCE(e.email, '')) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<Employee> search(@Param("search") String search, Pageable pageable);
}
