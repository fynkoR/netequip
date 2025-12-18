package com.example.netequip.repository;

import com.example.netequip.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Поиск по имени
    List<Employee> findByFullNameContainingIgnoreCase(String name);

    // Поиск по email
    Optional<Employee> findByEmail(String email);

    // Поиск по должности
    List<Employee> findByPosition(String position);

    // Проверка существования email
    boolean existsByEmail(String email);

    // Получить всех сотрудников определенной должности
    List<Employee> findByPositionOrderByFullNameAsc(String position);
}
