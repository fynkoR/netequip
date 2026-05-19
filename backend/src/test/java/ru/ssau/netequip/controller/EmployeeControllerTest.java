package ru.ssau.netequip.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.ssau.netequip.dto.employee.CreateEmployeeDto;
import ru.ssau.netequip.dto.employee.ResponseEmployeeDto;
import ru.ssau.netequip.dto.employee.UpdateEmployeeDto;
import ru.ssau.netequip.enums.UserRole;
import ru.ssau.netequip.repository.EmployeeRepository;
import ru.ssau.netequip.repository.EquipmentRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmployeeControllerTest {

    @Autowired
    private EmployeeController employeeController;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @BeforeEach
    void setUp() {
        // Очищаем в правильном порядке
        equipmentRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
    }

    @AfterEach
    void tearDown() {
        // Удаляем в правильном порядке: сначала оборудование (если есть связь), потом сотрудников
        equipmentRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
    }

    @Test
    void testCreateEmployee_Success() {
        CreateEmployeeDto dto = new CreateEmployeeDto();
        dto.setFullName("Тестов Тест Тестович");
        dto.setPosition(UserRole.ENGINEER);
        dto.setEmail("test" + System.currentTimeMillis() + "@ssau.ru"); // Уникальный email

        ResponseEntity<ResponseEmployeeDto> response = employeeController.createEmployee(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(dto.getFullName(), response.getBody().getFullName());
        assertEquals(dto.getEmail(), response.getBody().getEmail());
    }

    @Test
    void testCreateEmployee_DuplicateEmail_ShouldThrowException() {
        // Сначала создаем сотрудника
        String email = "duplicate@ssau.ru";
        CreateEmployeeDto dto1 = new CreateEmployeeDto();
        dto1.setFullName("Первый Сотрудник");
        dto1.setPosition(UserRole.ENGINEER);
        dto1.setEmail(email);
        employeeController.createEmployee(dto1);

        // Пытаемся создать второго с тем же email
        CreateEmployeeDto dto2 = new CreateEmployeeDto();
        dto2.setFullName("Второй Сотрудник");
        dto2.setPosition(UserRole.ADMIN);
        dto2.setEmail(email);

        assertThrows(Exception.class, () -> employeeController.createEmployee(dto2));
    }

    @Test
    void testGetEmployeeById_Success() {
        // Сначала создаем сотрудника
        CreateEmployeeDto createDto = new CreateEmployeeDto();
        createDto.setFullName("Тестовый Сотрудник");
        createDto.setPosition(UserRole.ENGINEER);
        createDto.setEmail("gettest" + System.currentTimeMillis() + "@ssau.ru");
        ResponseEntity<ResponseEmployeeDto> createResponse = employeeController.createEmployee(createDto);
        Long employeeId = createResponse.getBody().getId();

        // Получаем по ID
        ResponseEntity<ResponseEmployeeDto> response = employeeController.getEmployeeById(employeeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(employeeId, response.getBody().getId());
        assertEquals("Тестовый Сотрудник", response.getBody().getFullName());
    }

    @Test
    void testGetEmployeeById_NotFound_ShouldThrowException() {
        assertThrows(Exception.class, () -> employeeController.getEmployeeById(99999L));
    }

    @Test
    void testGetEmployees_Success() {
        // Создаем несколько сотрудников
        CreateEmployeeDto dto1 = new CreateEmployeeDto();
        dto1.setFullName("Сотрудник 1");
        dto1.setPosition(UserRole.ENGINEER);
        dto1.setEmail("emp1" + System.currentTimeMillis() + "@ssau.ru");
        employeeController.createEmployee(dto1);

        CreateEmployeeDto dto2 = new CreateEmployeeDto();
        dto2.setFullName("Сотрудник 2");
        dto2.setPosition(UserRole.ADMIN);
        dto2.setEmail("emp2" + System.currentTimeMillis() + "@ssau.ru");
        employeeController.createEmployee(dto2);

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<Page<ResponseEmployeeDto>> response = employeeController.getEmployees(null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTotalElements() >= 2);
    }

    @Test
    void testUpdateEmployee_Success() {
        // Создаем сотрудника
        String originalEmail = "update" + System.currentTimeMillis() + "@ssau.ru";
        CreateEmployeeDto createDto = new CreateEmployeeDto();
        createDto.setFullName("Старое Имя");
        createDto.setPosition(UserRole.ENGINEER);
        createDto.setEmail(originalEmail);
        ResponseEntity<ResponseEmployeeDto> createResponse = employeeController.createEmployee(createDto);
        Long employeeId = createResponse.getBody().getId();

        // Обновляем сотрудника
        UpdateEmployeeDto updateDto = new UpdateEmployeeDto();
        updateDto.setFullName("Новое Имя");
        updateDto.setPosition(UserRole.ADMIN);
        updateDto.setEmail(originalEmail); // Тот же email (не меняем)

        ResponseEntity<ResponseEmployeeDto> response = employeeController.updateEmployee(employeeId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Новое Имя", response.getBody().getFullName());
        assertEquals(UserRole.ADMIN.name(), response.getBody().getPosition());
    }

    @Test
    void testUpdateEmployee_ChangeEmail_Duplicate_ShouldThrowException() {
        // Создаем первого сотрудника
        String email1 = "first@ssau.ru";
        CreateEmployeeDto dto1 = new CreateEmployeeDto();
        dto1.setFullName("Первый");
        dto1.setPosition(UserRole.ENGINEER);
        dto1.setEmail(email1);
        employeeController.createEmployee(dto1);

        // Создаем второго сотрудника
        String email2 = "second@ssau.ru";
        CreateEmployeeDto dto2 = new CreateEmployeeDto();
        dto2.setFullName("Второй");
        dto2.setPosition(UserRole.ENGINEER);
        dto2.setEmail(email2);
        ResponseEntity<ResponseEmployeeDto> createResponse = employeeController.createEmployee(dto2);
        Long secondId = createResponse.getBody().getId();

        // Пытаемся обновить второго сотрудника, меняя email на email первого
        UpdateEmployeeDto updateDto = new UpdateEmployeeDto();
        updateDto.setFullName("Второй Обновленный");
        updateDto.setPosition(UserRole.ADMIN);
        updateDto.setEmail(email1); // Email первого сотрудника

        assertThrows(Exception.class, () -> employeeController.updateEmployee(secondId, updateDto));
    }

    @Test
    void testDeleteEmployee_Success() {
        // Создаем сотрудника
        CreateEmployeeDto createDto = new CreateEmployeeDto();
        createDto.setFullName("Удаляемый Сотрудник");
        createDto.setPosition(UserRole.ENGINEER);
        createDto.setEmail("delete" + System.currentTimeMillis() + "@ssau.ru");
        ResponseEntity<ResponseEmployeeDto> createResponse = employeeController.createEmployee(createDto);
        Long employeeId = createResponse.getBody().getId();

        // Удаляем сотрудника
        ResponseEntity<Void> response = employeeController.deleteEmployee(employeeId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверяем, что сотрудник действительно удален
        assertThrows(Exception.class, () -> employeeController.getEmployeeById(employeeId));
    }

    @Test
    void testDeleteEmployee_NotFound_ShouldThrowException() {
        assertThrows(Exception.class, () -> employeeController.deleteEmployee(99999L));
    }
}