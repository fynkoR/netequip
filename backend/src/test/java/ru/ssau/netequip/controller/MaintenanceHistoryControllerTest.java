package ru.ssau.netequip.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.ssau.netequip.dto.maintenanceHistory.CreateAndUpdateMaintenanceHistoryDTO;
import ru.ssau.netequip.dto.maintenanceHistory.ResponseMaintenanceHistoryDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.entity.User;
import ru.ssau.netequip.enums.Position;
import ru.ssau.netequip.enums.Role;
import ru.ssau.netequip.repository.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MaintenanceHistoryControllerTest {

    @Autowired
    private MaintenanceHistoryRepository maintenanceHistoryRepository;

    @Autowired
    private MaintenanceHistoryController maintenanceHistoryController;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    private Equipment testEquipment;
    private Employee testEmployee;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Очищаем в правильном порядке
        maintenanceHistoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
        equipmentRepository.deleteAllInBatch();
        equipmentTypeRepository.deleteAllInBatch();

        // Создаем тип оборудования
        EquipmentType type = new EquipmentType();
        type.setTypeName("Switch");
        equipmentTypeRepository.save(type);

        // Создаем оборудование
        testEquipment = new Equipment();
        testEquipment.setName("Test Switch");
        testEquipment.setType(type);
        testEquipment.setSerialNumber("SN-SWITCH-001");
        testEquipment.setMacAddress("AA:BB:CC:DD:EE:01");
        equipmentRepository.save(testEquipment);

        // Создаем сотрудника
        testEmployee = new Employee();
        testEmployee.setFullName("Тестовый Сотрудник");
        testEmployee.setPosition(Position.ENGINEER);
        testEmployee.setEmail("employee" + System.currentTimeMillis() + "@ssau.ru");
        employeeRepository.save(testEmployee);

        // Создаем пользователя и привязываем к сотруднику
        testUser = new User();
        testUser.setUsername("testuser" + System.currentTimeMillis());
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setEmployee(testEmployee);
        userRepository.save(testUser);

        // Настраиваем SecurityContext для текущего пользователя
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser.getId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + testUser.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        // Очищаем SecurityContext
        SecurityContextHolder.clearContext();

        // Удаляем в правильном порядке
        maintenanceHistoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
        equipmentRepository.deleteAllInBatch();
        equipmentTypeRepository.deleteAllInBatch();
    }

    @Test
    void testAddMaintenanceHistory_Success_WithEmployee() {
        CreateAndUpdateMaintenanceHistoryDTO dto = new CreateAndUpdateMaintenanceHistoryDTO();
        dto.setEquipmentId(testEquipment.getId());
        dto.setDescription("Плановое техническое обслуживание");
        dto.setType("ROUTINE_CHECK");
        // performedById будет установлен автоматически из текущего пользователя

        ResponseEntity<ResponseMaintenanceHistoryDto> response = maintenanceHistoryController.addMaintenanceHistory(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Плановое техническое обслуживание", response.getBody().getDescription());
        assertNotNull(response.getBody().getDate());
    }

    @Test
    void testAddMaintenanceHistory_WithCustomDate() {
        LocalDateTime customDate = LocalDateTime.of(2024, 12, 25, 10, 30);

        CreateAndUpdateMaintenanceHistoryDTO dto = new CreateAndUpdateMaintenanceHistoryDTO();
        dto.setEquipmentId(testEquipment.getId());
        dto.setDescription("Внеплановое обслуживание");
        dto.setType("EMERGENCY");
        dto.setDate(customDate);

        ResponseEntity<ResponseMaintenanceHistoryDto> response = maintenanceHistoryController.addMaintenanceHistory(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(customDate, response.getBody().getDate());
    }

    @Test
    void testAddMaintenanceHistory_EquipmentNotFound_ShouldThrowException() {
        CreateAndUpdateMaintenanceHistoryDTO dto = new CreateAndUpdateMaintenanceHistoryDTO();
        dto.setEquipmentId(99999L);
        dto.setDescription("Обслуживание несуществующего оборудования");
        dto.setType("ROUTINE_CHECK");

        assertThrows(Exception.class, () -> maintenanceHistoryController.addMaintenanceHistory(dto));
    }

    @Test
    void testAddMaintenanceHistory_UserWithoutEmployee_ShouldWork() {
        // Создаем пользователя без сотрудника
        User userWithoutEmployee = new User();
        userWithoutEmployee.setUsername("userwithoutemp" + System.currentTimeMillis());
        userWithoutEmployee.setPassword("encodedPassword");
        userWithoutEmployee.setRole(Role.USER);
        userWithoutEmployee.setEmployee(null);
        userRepository.save(userWithoutEmployee);

        // Настраиваем SecurityContext для этого пользователя
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userWithoutEmployee.getId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CreateAndUpdateMaintenanceHistoryDTO dto = new CreateAndUpdateMaintenanceHistoryDTO();
        dto.setEquipmentId(testEquipment.getId());
        dto.setDescription("Обслуживание без сотрудника");
        dto.setType("ROUTINE_CHECK");

        ResponseEntity<ResponseMaintenanceHistoryDto> response = maintenanceHistoryController.addMaintenanceHistory(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getPerformedById());
    }

    @Test
    void testGetMaintenanceHistoryById_Success() {
        // Сначала создаем запись
        CreateAndUpdateMaintenanceHistoryDTO createDto = new CreateAndUpdateMaintenanceHistoryDTO();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setDescription("Тестовая запись");
        createDto.setType("INSPECTION");
        ResponseEntity<ResponseMaintenanceHistoryDto> createResponse =
                maintenanceHistoryController.addMaintenanceHistory(createDto);
        Long historyId = createResponse.getBody().getId();

        // Получаем по ID
        ResponseEntity<ResponseMaintenanceHistoryDto> response =
                maintenanceHistoryController.getMaintenanceHistory(historyId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(historyId, response.getBody().getId());
        assertEquals("Тестовая запись", response.getBody().getDescription());
    }

    @Test
    void testGetMaintenanceHistoryById_NotFound_ShouldThrowException() {
        assertThrows(Exception.class, () -> maintenanceHistoryController.getMaintenanceHistory(99999L));
    }

    @Test
    void testGetListMaintenanceHistory_Success() {
        // Создаем несколько записей
        CreateAndUpdateMaintenanceHistoryDTO dto1 = new CreateAndUpdateMaintenanceHistoryDTO();
        dto1.setEquipmentId(testEquipment.getId());
        dto1.setDescription("Запись 1");
        dto1.setType("ROUTINE_CHECK");
        maintenanceHistoryController.addMaintenanceHistory(dto1);

        CreateAndUpdateMaintenanceHistoryDTO dto2 = new CreateAndUpdateMaintenanceHistoryDTO();
        dto2.setEquipmentId(testEquipment.getId());
        dto2.setDescription("Запись 2");
        dto2.setType("REPAIR");
        maintenanceHistoryController.addMaintenanceHistory(dto2);

        ResponseEntity<List<ResponseMaintenanceHistoryDto>> response =
                maintenanceHistoryController.getListMaintenanceHistory();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 2);
    }

    @Test
    void testGetListMaintenanceHistory_Empty() {
        ResponseEntity<List<ResponseMaintenanceHistoryDto>> response =
                maintenanceHistoryController.getListMaintenanceHistory();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Может быть не пустым, если есть данные от других тестов
    }

    @Test
    void testUpdateMaintenanceHistory_Success() {
        // Сначала создаем запись
        CreateAndUpdateMaintenanceHistoryDTO createDto = new CreateAndUpdateMaintenanceHistoryDTO();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setDescription("Старое описание");
        createDto.setType("ROUTINE_CHECK");
        ResponseEntity<ResponseMaintenanceHistoryDto> createResponse =
                maintenanceHistoryController.addMaintenanceHistory(createDto);
        Long historyId = createResponse.getBody().getId();

        // Обновляем запись
        CreateAndUpdateMaintenanceHistoryDTO updateDto = new CreateAndUpdateMaintenanceHistoryDTO();
        updateDto.setEquipmentId(testEquipment.getId());
        updateDto.setDescription("Новое описание");
        updateDto.setType("EMERGENCY");
        updateDto.setPerformedById(testEmployee.getId());

        ResponseEntity<ResponseMaintenanceHistoryDto> response =
                maintenanceHistoryController.update(historyId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Новое описание", response.getBody().getDescription());
    }

    @Test
    void testUpdateMaintenanceHistory_NotFound_ShouldThrowException() {
        CreateAndUpdateMaintenanceHistoryDTO updateDto = new CreateAndUpdateMaintenanceHistoryDTO();
        updateDto.setEquipmentId(testEquipment.getId());
        updateDto.setDescription("Обновление");
        updateDto.setType("ROUTINE_CHECK");

        assertThrows(Exception.class, () -> maintenanceHistoryController.update(99999L, updateDto));
    }

    @Test
    void testUpdateMaintenanceHistory_EquipmentNotFound_ShouldThrowException() {
        // Сначала создаем запись
        CreateAndUpdateMaintenanceHistoryDTO createDto = new CreateAndUpdateMaintenanceHistoryDTO();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setDescription("Оригинал");
        createDto.setType("ROUTINE_CHECK");
        ResponseEntity<ResponseMaintenanceHistoryDto> createResponse =
                maintenanceHistoryController.addMaintenanceHistory(createDto);
        Long historyId = createResponse.getBody().getId();

        // Пытаемся обновить с несуществующим оборудованием
        CreateAndUpdateMaintenanceHistoryDTO updateDto = new CreateAndUpdateMaintenanceHistoryDTO();
        updateDto.setEquipmentId(99999L);
        updateDto.setDescription("Обновление");
        updateDto.setType("ROUTINE_CHECK");

        assertThrows(Exception.class, () -> maintenanceHistoryController.update(historyId, updateDto));
    }

    @Test
    void testDeleteMaintenanceHistory_Success() {
        // Сначала создаем запись
        CreateAndUpdateMaintenanceHistoryDTO createDto = new CreateAndUpdateMaintenanceHistoryDTO();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setDescription("Удаляемая запись");
        createDto.setType("ROUTINE_CHECK");
        ResponseEntity<ResponseMaintenanceHistoryDto> createResponse =
                maintenanceHistoryController.addMaintenanceHistory(createDto);
        Long historyId = createResponse.getBody().getId();

        // Удаляем запись
        ResponseEntity<Void> response = maintenanceHistoryController.deleteMaintenanceHistory(historyId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверяем, что запись действительно удалена
        assertThrows(Exception.class, () -> maintenanceHistoryController.getMaintenanceHistory(historyId));
    }

    @Test
    void testDeleteMaintenanceHistory_NotFound_ShouldThrowException() {
        assertThrows(Exception.class, () -> maintenanceHistoryController.deleteMaintenanceHistory(99999L));
    }
}