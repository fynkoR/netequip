package ru.ssau.netequip.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.netequip.dto.maintenanceHistory.CreateAndUpdateMaintenanceHistoryDTO;
import ru.ssau.netequip.dto.maintenanceHistory.ResponseMaintenanceHistoryDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.MaintenanceHistory;
import ru.ssau.netequip.exception.employee.NotFoundEmployeeException;
import ru.ssau.netequip.exception.equipment.NotFoundEquipmentException;
import ru.ssau.netequip.exception.maintenanceHistory.NotFoundMaintenanceHistoryException;
import ru.ssau.netequip.mapper.MaintenanceHistoryMapper;
import ru.ssau.netequip.repository.EmployeeRepository;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.repository.MaintenanceHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceHistoryServiceTest {

    @Mock
    private MaintenanceHistoryRepository maintenanceHistoryRepository;

    @Mock
    private MaintenanceHistoryMapper maintenanceHistoryMapper;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private MaintenanceHistoryService maintenanceHistoryService;

    @Mock
    private AuditLogService auditLogService;

    private Equipment createEquipment(Long id) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setName("Test Equipment");
        return equipment;
    }

    private Employee createEmployee(Long id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFullName("Test Employee");
        return employee;
    }

    private MaintenanceHistory createMaintenanceHistory(Long id, Equipment equipment, Employee employee) {
        MaintenanceHistory history = new MaintenanceHistory();
        history.setId(id);
        history.setEquipment(equipment);
        history.setPerformedBy(employee);
        history.setDate(LocalDateTime.now());
        history.setDescription("Test maintenance");
        return history;
    }

    private CreateAndUpdateMaintenanceHistoryDTO createDto(Long equipmentId, Long performedById) {
        CreateAndUpdateMaintenanceHistoryDTO dto = new CreateAndUpdateMaintenanceHistoryDTO();
        dto.setEquipmentId(equipmentId);
        dto.setPerformedById(performedById);
        dto.setDescription("Test maintenance description");
        dto.setType("ROUTINE_CHECK");
        return dto;
    }

    @Test
    void testCreate_Success_WithEmployee() {
        Long equipmentId = 1L;
        Long employeeId = 10L;
        CreateAndUpdateMaintenanceHistoryDTO dto = createDto(equipmentId, employeeId);

        Equipment equipment = createEquipment(equipmentId);
        Employee employee = createEmployee(employeeId);
        MaintenanceHistory entity = createMaintenanceHistory(null, equipment, employee);
        MaintenanceHistory saved = createMaintenanceHistory(100L, equipment, employee);
        ResponseMaintenanceHistoryDto responseDto = new ResponseMaintenanceHistoryDto();
        responseDto.setId(100L);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(maintenanceHistoryMapper.toEntity(dto)).thenReturn(entity);
        when(maintenanceHistoryRepository.save(any(MaintenanceHistory.class))).thenReturn(saved);
        when(maintenanceHistoryMapper.toResponseDTO(saved)).thenReturn(responseDto);

        ResponseMaintenanceHistoryDto result = maintenanceHistoryService.create(dto);

        assertNotNull(result);
        verify(maintenanceHistoryRepository, times(1)).save(entity);
        assertNotNull(entity.getDate());
    }

    @Test
    void testCreate_Success_WithoutEmployee() {
        Long equipmentId = 1L;
        CreateAndUpdateMaintenanceHistoryDTO dto = createDto(equipmentId, null);

        Equipment equipment = createEquipment(equipmentId);
        MaintenanceHistory entity = createMaintenanceHistory(null, equipment, null);
        MaintenanceHistory saved = createMaintenanceHistory(100L, equipment, null);
        ResponseMaintenanceHistoryDto responseDto = new ResponseMaintenanceHistoryDto();

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(maintenanceHistoryMapper.toEntity(dto)).thenReturn(entity);
        when(maintenanceHistoryRepository.save(any(MaintenanceHistory.class))).thenReturn(saved);
        when(maintenanceHistoryMapper.toResponseDTO(saved)).thenReturn(responseDto);

        ResponseMaintenanceHistoryDto result = maintenanceHistoryService.create(dto);

        assertNotNull(result);
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    void testCreate_WithCustomDate_ShouldNotOverride() {
        Long equipmentId = 1L;
        CreateAndUpdateMaintenanceHistoryDTO dto = createDto(equipmentId, null);
        LocalDateTime customDate = LocalDateTime.of(2024, 1, 1, 10, 0);
        dto.setDate(customDate);

        Equipment equipment = createEquipment(equipmentId);
        MaintenanceHistory entity = createMaintenanceHistory(null, equipment, null);
        entity.setDate(customDate);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(maintenanceHistoryMapper.toEntity(dto)).thenReturn(entity);
        when(maintenanceHistoryRepository.save(any(MaintenanceHistory.class))).thenReturn(entity);
        when(maintenanceHistoryMapper.toResponseDTO(entity)).thenReturn(new ResponseMaintenanceHistoryDto());

        maintenanceHistoryService.create(dto);

        assertEquals(customDate, entity.getDate());
    }

    @Test
    void testCreate_EquipmentNotFound_ShouldThrowException() {
        Long equipmentId = 999L;
        CreateAndUpdateMaintenanceHistoryDTO dto = createDto(equipmentId, null);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentException.class, () -> maintenanceHistoryService.create(dto));
        verify(maintenanceHistoryRepository, never()).save(any());
    }

    @Test
    void testCreate_EmployeeNotFound_ShouldThrowException() {
        Long equipmentId = 1L;
        Long employeeId = 999L;
        CreateAndUpdateMaintenanceHistoryDTO dto = createDto(equipmentId, employeeId);

        Equipment equipment = createEquipment(equipmentId);
        MaintenanceHistory entity = createMaintenanceHistory(null, equipment, null);  // создай entity

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        // ДОБАВЬ ЭТУ СТРОЧКУ:
        when(maintenanceHistoryMapper.toEntity(dto)).thenReturn(entity);

        assertThrows(NotFoundEmployeeException.class, () -> maintenanceHistoryService.create(dto));
        verify(maintenanceHistoryRepository, never()).save(any());
    }

    @Test
    void testGetById_Success() {
        Long historyId = 100L;
        Equipment equipment = createEquipment(1L);
        Employee employee = createEmployee(10L);
        MaintenanceHistory history = createMaintenanceHistory(historyId, equipment, employee);
        ResponseMaintenanceHistoryDto responseDto = new ResponseMaintenanceHistoryDto();
        responseDto.setId(historyId);

        when(maintenanceHistoryRepository.findById(historyId)).thenReturn(Optional.of(history));
        when(maintenanceHistoryMapper.toResponseDTO(history)).thenReturn(responseDto);

        ResponseMaintenanceHistoryDto result = maintenanceHistoryService.getById(historyId);

        assertNotNull(result);
        assertEquals(historyId, result.getId());
    }

    @Test
    void testGetById_NotFound_ShouldThrowException() {
        Long historyId = 999L;

        when(maintenanceHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

        assertThrows(NotFoundMaintenanceHistoryException.class,
                () -> maintenanceHistoryService.getById(historyId));
    }

    @Test
    void testGetAll_Success() {
        Equipment equipment = createEquipment(1L);
        MaintenanceHistory history1 = createMaintenanceHistory(1L, equipment, null);
        MaintenanceHistory history2 = createMaintenanceHistory(2L, equipment, null);
        ResponseMaintenanceHistoryDto dto1 = new ResponseMaintenanceHistoryDto();
        ResponseMaintenanceHistoryDto dto2 = new ResponseMaintenanceHistoryDto();

        when(maintenanceHistoryRepository.findAll()).thenReturn(List.of(history1, history2));
        when(maintenanceHistoryMapper.toResponseDTO(history1)).thenReturn(dto1);
        when(maintenanceHistoryMapper.toResponseDTO(history2)).thenReturn(dto2);

        List<ResponseMaintenanceHistoryDto> result = maintenanceHistoryService.getAll();

        assertEquals(2, result.size());
        verify(maintenanceHistoryRepository, times(1)).findAll();
    }

    @Test
    void testGetAll_Empty() {
        when(maintenanceHistoryRepository.findAll()).thenReturn(List.of());

        List<ResponseMaintenanceHistoryDto> result = maintenanceHistoryService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success_WithEmployee() {
        Long historyId = 100L;
        Long equipmentId = 2L;
        Long employeeId = 20L;
        CreateAndUpdateMaintenanceHistoryDTO dto = createDto(equipmentId, employeeId);

        Equipment oldEquipment = createEquipment(1L);
        Equipment newEquipment = createEquipment(equipmentId);
        Employee employee = createEmployee(employeeId);
        MaintenanceHistory existingHistory = createMaintenanceHistory(historyId, oldEquipment, null);
        MaintenanceHistory updatedHistory = createMaintenanceHistory(historyId, newEquipment, employee);

        when(maintenanceHistoryRepository.findById(historyId)).thenReturn(Optional.of(existingHistory));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(newEquipment));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(maintenanceHistoryRepository.save(any(MaintenanceHistory.class))).thenReturn(updatedHistory);
        when(maintenanceHistoryMapper.toResponseDTO(updatedHistory)).thenReturn(new ResponseMaintenanceHistoryDto());

        assertDoesNotThrow(() -> maintenanceHistoryService.update(dto, historyId));
        verify(maintenanceHistoryMapper, times(1)).updateEntityFromDTO(eq(dto), eq(existingHistory));
        verify(maintenanceHistoryRepository, times(1)).save(existingHistory);
    }

    @Test
    void testUpdate_HistoryNotFound_ShouldThrowException() {
        Long historyId = 999L;
        CreateAndUpdateMaintenanceHistoryDTO dto = createDto(1L, null);

        when(maintenanceHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

        assertThrows(NotFoundMaintenanceHistoryException.class,
                () -> maintenanceHistoryService.update(dto, historyId));
    }

    @Test
    void testUpdate_EquipmentNotFound_ShouldThrowException() {
        Long historyId = 100L;
        Long equipmentId = 999L;
        CreateAndUpdateMaintenanceHistoryDTO dto = createDto(equipmentId, null);

        MaintenanceHistory existingHistory = createMaintenanceHistory(historyId, createEquipment(1L), null);

        when(maintenanceHistoryRepository.findById(historyId)).thenReturn(Optional.of(existingHistory));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentException.class,
                () -> maintenanceHistoryService.update(dto, historyId));
    }

    @Test
    void testUpdate_EmployeeNotFound_ShouldThrowException() {
        Long historyId = 100L;
        Long equipmentId = 1L;
        Long employeeId = 999L;
        CreateAndUpdateMaintenanceHistoryDTO dto = createDto(equipmentId, employeeId);

        Equipment equipment = createEquipment(equipmentId);
        MaintenanceHistory existingHistory = createMaintenanceHistory(historyId, equipment, null);

        when(maintenanceHistoryRepository.findById(historyId)).thenReturn(Optional.of(existingHistory));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmployeeException.class,
                () -> maintenanceHistoryService.update(dto, historyId));
    }

//    @Test
//    void testUpdate_WithoutEmployee_ClearsEmployee() {
//        Long historyId = 100L;
//        Long equipmentId = 1L;
//        CreateAndUpdateMaintenanceHistoryDTO dto = createDto(equipmentId, null);
//
//        Equipment equipment = createEquipment(equipmentId);
//        Employee oldEmployee = createEmployee(10L);
//        MaintenanceHistory existingHistory = createMaintenanceHistory(historyId, equipment, oldEmployee);
//        MaintenanceHistory updatedHistory = createMaintenanceHistory(historyId, equipment, null);
//
//        when(maintenanceHistoryRepository.findById(historyId)).thenReturn(Optional.of(existingHistory));
//        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
//        when(maintenanceHistoryRepository.save(any(MaintenanceHistory.class))).thenReturn(updatedHistory);
//        when(maintenanceHistoryMapper.toResponseDTO(updatedHistory)).thenReturn(new ResponseMaintenanceHistoryDto());
//
//        assertDoesNotThrow(() -> maintenanceHistoryService.update(dto, historyId));
//
//        // Проверяем, что employee установлен в null
//        assertNull(existingHistory.getPerformedBy());
//    }

    @Test
    void testDelete_Success() {
        Long historyId = 100L;
        MaintenanceHistory history = createMaintenanceHistory(historyId, createEquipment(1L), null);

        when(maintenanceHistoryRepository.findById(historyId)).thenReturn(Optional.of(history));

        assertDoesNotThrow(() -> maintenanceHistoryService.delete(historyId));
        verify(maintenanceHistoryRepository, times(1)).deleteById(historyId);
    }

    @Test
    void testDelete_NotFound_ShouldThrowException() {
        Long historyId = 999L;

        when(maintenanceHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

        assertThrows(NotFoundMaintenanceHistoryException.class,
                () -> maintenanceHistoryService.delete(historyId));
        verify(maintenanceHistoryRepository, never()).deleteById(any());
    }
}