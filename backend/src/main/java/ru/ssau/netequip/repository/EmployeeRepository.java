package ru.ssau.netequip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.enums.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByFullName(String employeeName);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByPosition(UserRole position);
    boolean existsByEmail(String email);
    List<Employee> findByPositionOrderByFullNameAsc(UserRole position);
}
