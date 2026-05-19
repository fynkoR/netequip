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
import ru.ssau.netequip.dto.equipment.CreateEquipmentDto;
import ru.ssau.netequip.dto.equipment.ResponseEquipmentDto;
import ru.ssau.netequip.dto.equipment.UpdateEquipmentDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.enums.UserRole;
import ru.ssau.netequip.repository.EmployeeRepository;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.repository.EquipmentTypeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EquipmentControllerTest {

    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    private EquipmentType testType;
    private Employee testEmployee;
    private int employeeCounter = 0;

    @BeforeEach
    void setUp() {
        // Очищаем в правильном порядке
        equipmentRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
        equipmentTypeRepository.deleteAllInBatch();

        // Создаем тип оборудования
        testType = new EquipmentType();
        testType.setTypeName("Router");
        equipmentTypeRepository.save(testType);

        // Создаем сотрудника с уникальным email
        testEmployee = new Employee();
        testEmployee.setFullName("Тестов Сотрудник");
        testEmployee.setPosition(UserRole.ENGINEER);
        testEmployee.setEmail("test" + System.currentTimeMillis() + "@ssau.ru"); // Уникальный email
        employeeRepository.save(testEmployee);
    }

    @AfterEach
    void tearDown() {
        // Удаляем в правильном порядке: сначала оборудование, потом сотрудников, потом типы
        equipmentRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
        equipmentTypeRepository.deleteAllInBatch();
    }

    @Test
    void testAddEquipment_Success() {
        CreateEquipmentDto dto = new CreateEquipmentDto();
        dto.setName("Cisco Router 2900");
        dto.setTypeId(testType.getId());
        dto.setSerialNumber("SN-CISCO-001" + System.currentTimeMillis());
        dto.setMacAddress("00:11:22:33:44:" + System.currentTimeMillis() % 100);
        dto.setEmployeeId(testEmployee.getId());

        ResponseEntity<ResponseEquipmentDto> response = equipmentController.addEquipment(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Cisco Router 2900", response.getBody().getName());
    }

    @Test
    void testAddEquipment_WithoutEmployee_Success() {
        CreateEquipmentDto dto = new CreateEquipmentDto();
        dto.setName("MikroTik Router");
        dto.setTypeId(testType.getId());
        dto.setSerialNumber("SN-MIKRO-001" + System.currentTimeMillis());
        dto.setMacAddress("00:11:22:33:44:" + System.currentTimeMillis() % 100);
        dto.setEmployeeId(null);

        ResponseEntity<ResponseEquipmentDto> response = equipmentController.addEquipment(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetEquipmentById_Success() {
        // Сначала создаем оборудование
        CreateEquipmentDto createDto = new CreateEquipmentDto();
        createDto.setName("Test Equipment");
        createDto.setTypeId(testType.getId());
        createDto.setSerialNumber("SN-TEST-001" + System.currentTimeMillis());
        createDto.setMacAddress("AA:BB:CC:DD:EE:" + System.currentTimeMillis() % 100);
        ResponseEntity<ResponseEquipmentDto> createResponse = equipmentController.addEquipment(createDto);
        Long equipmentId = createResponse.getBody().getId();

        // Получаем по ID
        ResponseEntity<ResponseEquipmentDto> response = equipmentController.getEquipmentById(equipmentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(equipmentId, response.getBody().getId());
    }

    @Test
    void testGetEquipmentById_NotFound_ShouldThrowException() {
        assertThrows(Exception.class, () -> equipmentController.getEquipmentById(99999L));
    }

    @Test
    void testGetAllEquipment_Success() {
        // Создаем несколько оборудований
        CreateEquipmentDto dto1 = new CreateEquipmentDto();
        dto1.setName("Equipment 1");
        dto1.setTypeId(testType.getId());
        dto1.setSerialNumber("SN-001" + System.currentTimeMillis());
        dto1.setMacAddress("AA:BB:CC:DD:EE:01");
        equipmentController.addEquipment(dto1);

        CreateEquipmentDto dto2 = new CreateEquipmentDto();
        dto2.setName("Equipment 2");
        dto2.setTypeId(testType.getId());
        dto2.setSerialNumber("SN-002" + System.currentTimeMillis());
        dto2.setMacAddress("AA:BB:CC:DD:EE:02");
        equipmentController.addEquipment(dto2);

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<Page<ResponseEquipmentDto>> response = equipmentController.getAllEquipment(null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTotalElements() >= 2);
    }

    @Test
    void testUpdateEquipment_Success() {
        // Создаем оборудование
        CreateEquipmentDto createDto = new CreateEquipmentDto();
        createDto.setName("Old Name");
        createDto.setTypeId(testType.getId());
        createDto.setSerialNumber("SN-OLD-001" + System.currentTimeMillis());
        createDto.setMacAddress("AA:BB:CC:DD:EE:99");
        ResponseEntity<ResponseEquipmentDto> createResponse = equipmentController.addEquipment(createDto);
        Long equipmentId = createResponse.getBody().getId();

        // Обновляем оборудование
        UpdateEquipmentDto updateDto = new UpdateEquipmentDto();
        updateDto.setName("New Name");
        updateDto.setTypeId(testType.getId());
        updateDto.setSerialNumber(createDto.getSerialNumber());
        updateDto.setMacAddress(createDto.getMacAddress());
        updateDto.setEmployeeId(testEmployee.getId());

        ResponseEntity<ResponseEquipmentDto> response = equipmentController.updateEquipment(equipmentId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Name", response.getBody().getName());
    }

    @Test
    void testDeleteEquipment_Success() {
        // Создаем оборудование
        CreateEquipmentDto createDto = new CreateEquipmentDto();
        createDto.setName("To Delete");
        createDto.setTypeId(testType.getId());
        createDto.setSerialNumber("SN-DELETE-001" + System.currentTimeMillis());
        createDto.setMacAddress("AA:BB:CC:DD:EE:77");
        ResponseEntity<ResponseEquipmentDto> createResponse = equipmentController.addEquipment(createDto);
        Long equipmentId = createResponse.getBody().getId();

        // Удаляем оборудование
        ResponseEntity<Void> response = equipmentController.deleteEquipment(equipmentId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверяем, что оборудование действительно удалено
        assertThrows(Exception.class, () -> equipmentController.getEquipmentById(equipmentId));
    }
}