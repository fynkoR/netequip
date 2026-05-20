package ru.ssau.netequip.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ssau.netequip.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(a.employeeFullName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(a.entityType) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(a.entityName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(a.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        """)
    Page<AuditLog> search(@Param("search") String search, Pageable pageable);
}