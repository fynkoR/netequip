package ru.ssau.netequip.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.netequip.dto.employee.CreateEmployeeDto;
import ru.ssau.netequip.dto.employee.ResponseEmployeeDto;
import ru.ssau.netequip.dto.employee.UpdateEmployeeDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    @PostMapping
    public ResponseEntity<ResponseEmployeeDto> createEmployee(
            @Valid @RequestBody CreateEmployeeDto dto) {
        log.info("Create employee : {}", dto.getFullName());
        ResponseEmployeeDto create = employeeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(create);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseEmployeeDto> getEmployeeById(@PathVariable Long id) {
        log.info("Get employee by id : {}", id);
        ResponseEmployeeDto employee = employeeService.getById(id);
        return ResponseEntity.ok(employee);
    }
    @GetMapping
    public ResponseEntity<Page<ResponseEmployeeDto>> getEmployees(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("Get employees page={}, size={}, search='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), search);
        Page<ResponseEmployeeDto> employees = employeeService.getPage(search, pageable);
        return ResponseEntity.ok(employees);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseEmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeDto dto
    ){
        log.info("Update employee : {}", id);
        ResponseEmployeeDto update = employeeService.update(id, dto);
        return ResponseEntity.ok(update);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        log.info("Delete employee : {}", id);
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
