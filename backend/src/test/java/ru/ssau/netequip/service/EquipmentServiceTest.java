package ru.ssau.netequip.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.netequip.dto.equipment.CreateEquipmentDto;
import ru.ssau.netequip.dto.equipment.ResponseEquipmentDto;
import ru.ssau.netequip.dto.equipment.UpdateEquipmentDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.exception.employee.NotFoundEmployeeException;
import ru.ssau.netequip.exception.equipment.DuplicateEquipmentNameException;
import ru.ssau.netequip.exception.equipment.NotFoundEquipmentException;
import ru.ssau.netequip.exception.equipmentType.NotFoundEquipmentTypeException;
import ru.ssau.netequip.mapper.EquipmentMapper;
import ru.ssau.netequip.repository.EmployeeRepository;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.repository.EquipmentTypeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private EquipmentMapper equipmentMapper;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EquipmentTypeRepository equipmentTypeRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    private EquipmentType createEquipmentType(Long id, String typeName) {
        EquipmentType type = new EquipmentType();
        type.setId(id);
        type.setTypeName(typeName);
        return type;
    }

    private Employee createEmployee(Long id, String fullName) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFullName(fullName);
        return employee;
    }

    private Equipment createEquipment(Long id, String name, EquipmentType type, Employee employee) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setName(name);
        equipment.setType(type);
        equipment.setEmployee(employee);
        equipment.setSerialNumber("SN" + id);
        equipment.setMacAddress("00:11:22:33:44:" + id);
        equipment.setDateAdded(LocalDate.now());
        return equipment;
    }

    private CreateEquipmentDto createCreateDto(Long typeId, Long employeeId, String name) {
        CreateEquipmentDto dto = new CreateEquipmentDto();
        dto.setTypeId(typeId);
        dto.setEmployeeId(employeeId);
        dto.setName(name);
        dto.setSerialNumber("SN12345");
        dto.setMacAddress("AA:BB:CC:DD:EE:FF");
        return dto;
    }

    private UpdateEquipmentDto createUpdateDto(Long typeId, Long employeeId, String name) {
        UpdateEquipmentDto dto = new UpdateEquipmentDto();
        dto.setTypeId(typeId);
        dto.setEmployeeId(employeeId);
        dto.setName(name);
        dto.setSerialNumber("SN67890");
        dto.setMacAddress("11:22:33:44:55:66");
        return dto;
    }

    @Test
    void testCreate_Success_WithEmployee() {
        Long typeId = 1L;
        Long employeeId = 10L;
        CreateEquipmentDto dto = createCreateDto(typeId, employeeId, "Cisco Switch");

        EquipmentType type = createEquipmentType(typeId, "Switch");
        Employee employee = createEmployee(employeeId, "Иванов Иван");
        Equipment entity = createEquipment(null, "Cisco Switch", type, employee);
        Equipment saved = createEquipment(100L, "Cisco Switch", type, employee);
        ResponseEquipmentDto responseDto = new ResponseEquipmentDto();
        responseDto.setId(100L);

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(equipmentRepository.findBySerialNumber(dto.getSerialNumber())).thenReturn(Optional.empty());
        when(equipmentRepository.findByMacAddress(dto.getMacAddress())).thenReturn(Optional.empty());
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(equipmentMapper.toEntity(dto)).thenReturn(entity);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(saved);
        when(equipmentMapper.toResponseDTO(saved)).thenReturn(responseDto);

        ResponseEquipmentDto result = equipmentService.create(dto);

        assertNotNull(result);
        verify(equipmentRepository, times(1)).save(entity);
        assertNotNull(entity.getDateAdded());
    }

    @Test
    void testCreate_Success_WithoutEmployee() {
        Long typeId = 1L;
        CreateEquipmentDto dto = createCreateDto(typeId, null, "MikroTik Router");

        EquipmentType type = createEquipmentType(typeId, "Router");
        Equipment entity = createEquipment(null, "MikroTik Router", type, null);
        Equipment saved = createEquipment(100L, "MikroTik Router", type, null);
        ResponseEquipmentDto responseDto = new ResponseEquipmentDto();

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(equipmentRepository.findBySerialNumber(dto.getSerialNumber())).thenReturn(Optional.empty());
        when(equipmentRepository.findByMacAddress(dto.getMacAddress())).thenReturn(Optional.empty());
        when(equipmentMapper.toEntity(dto)).thenReturn(entity);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(saved);
        when(equipmentMapper.toResponseDTO(saved)).thenReturn(responseDto);

        ResponseEquipmentDto result = equipmentService.create(dto);

        assertNotNull(result);
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    void testCreate_WithCustomDate_ShouldNotOverride() {
        Long typeId = 1L;
        CreateEquipmentDto dto = createCreateDto(typeId, null, "Test Device");
        LocalDate customDate = LocalDate.of(2024, 1, 1);
        dto.setDateAdded(customDate);

        EquipmentType type = createEquipmentType(typeId, "Type");
        Equipment entity = createEquipment(null, "Test Device", type, null);
        entity.setDateAdded(customDate);

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(equipmentRepository.findBySerialNumber(dto.getSerialNumber())).thenReturn(Optional.empty());
        when(equipmentRepository.findByMacAddress(dto.getMacAddress())).thenReturn(Optional.empty());
        when(equipmentMapper.toEntity(dto)).thenReturn(entity);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(entity);
        when(equipmentMapper.toResponseDTO(entity)).thenReturn(new ResponseEquipmentDto());

        equipmentService.create(dto);

        assertEquals(customDate, entity.getDateAdded());
    }

    @Test
    void testCreate_EquipmentTypeNotFound_ShouldThrowException() {
        Long typeId = 999L;
        CreateEquipmentDto dto = createCreateDto(typeId, null, "Test");

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentTypeException.class, () -> equipmentService.create(dto));
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void testCreate_DuplicateSerialNumber_ShouldThrowException() {
        Long typeId = 1L;
        CreateEquipmentDto dto = createCreateDto(typeId, null, "Test");
        EquipmentType type = createEquipmentType(typeId, "Type");
        Equipment existingEquipment = createEquipment(50L, "Existing", type, null);

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(equipmentRepository.findBySerialNumber(dto.getSerialNumber()))
                .thenReturn(Optional.of(existingEquipment));

        assertThrows(DuplicateEquipmentNameException.class, () -> equipmentService.create(dto));
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void testCreate_DuplicateMacAddress_ShouldThrowException() {
        Long typeId = 1L;
        CreateEquipmentDto dto = createCreateDto(typeId, null, "Test");
        EquipmentType type = createEquipmentType(typeId, "Type");
        Equipment existingEquipment = createEquipment(50L, "Existing", type, null);

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(equipmentRepository.findBySerialNumber(dto.getSerialNumber())).thenReturn(Optional.empty());
        when(equipmentRepository.findByMacAddress(dto.getMacAddress()))
                .thenReturn(Optional.of(existingEquipment));

        assertThrows(DuplicateEquipmentNameException.class, () -> equipmentService.create(dto));
        verify(equipmentRepository, never()).save(any());
    }

//    @Test
//    void testCreate_EmployeeNotFound_ShouldThrowException() {
//        Long typeId = 1L;
//        Long employeeId = 999L;
//        CreateEquipmentDto dto = createCreateDto(typeId, employeeId, "Test");
//        EquipmentType type = createEquipmentType(typeId, "Type");
//
//        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
//        // ДОБАВЬ ЭТИ ДВЕ СТРОЧКИ:
//        when(equipmentRepository.findBySerialNumber(dto.getSerialNumber())).thenReturn(Optional.empty());
//        when(equipmentRepository.findByMacAddress(dto.getMacAddress())).thenReturn(Optional.empty());
//
//        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
//
//        assertThrows(NotFoundEmployeeException.class, () -> equipmentService.create(dto));
//        verify(equipmentRepository, never()).save(any());
//    }

    @Test
    void testGetById_Success() {
        Long equipmentId = 100L;
        EquipmentType type = createEquipmentType(1L, "Switch");
        Equipment equipment = createEquipment(equipmentId, "Cisco Switch", type, null);
        ResponseEquipmentDto responseDto = new ResponseEquipmentDto();
        responseDto.setId(equipmentId);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(equipmentMapper.toResponseDTO(equipment)).thenReturn(responseDto);

        ResponseEquipmentDto result = equipmentService.getById(equipmentId);

        assertNotNull(result);
        assertEquals(equipmentId, result.getId());
    }

    @Test
    void testGetById_NotFound_ShouldThrowException() {
        Long equipmentId = 999L;

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentException.class, () -> equipmentService.getById(equipmentId));
    }

    @Test
    void testGetAll_Success() {
        EquipmentType type = createEquipmentType(1L, "Switch");
        Equipment eq1 = createEquipment(1L, "Device1", type, null);
        Equipment eq2 = createEquipment(2L, "Device2", type, null);
        ResponseEquipmentDto dto1 = new ResponseEquipmentDto();
        ResponseEquipmentDto dto2 = new ResponseEquipmentDto();

        when(equipmentRepository.findAll()).thenReturn(List.of(eq1, eq2));
        when(equipmentMapper.toResponseDTO(eq1)).thenReturn(dto1);
        when(equipmentMapper.toResponseDTO(eq2)).thenReturn(dto2);

        List<ResponseEquipmentDto> result = equipmentService.getAll();

        assertEquals(2, result.size());
        verify(equipmentRepository, times(1)).findAll();
    }

    @Test
    void testGetAll_Empty() {
        when(equipmentRepository.findAll()).thenReturn(List.of());

        List<ResponseEquipmentDto> result = equipmentService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success() {
        Long equipmentId = 100L;
        Long newTypeId = 2L;
        Long newEmployeeId = 20L;
        UpdateEquipmentDto dto = createUpdateDto(newTypeId, newEmployeeId, "Updated Name");

        EquipmentType oldType = createEquipmentType(1L, "Old");
        EquipmentType newType = createEquipmentType(newTypeId, "New");
        Employee newEmployee = createEmployee(newEmployeeId, "New Employee");
        Equipment existingEquipment = createEquipment(equipmentId, "Old Name", oldType, null);
        Equipment updatedEquipment = createEquipment(equipmentId, "Updated Name", newType, newEmployee);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(existingEquipment));
        when(equipmentTypeRepository.findById(newTypeId)).thenReturn(Optional.of(newType));
        when(equipmentRepository.findBySerialNumber(dto.getSerialNumber())).thenReturn(Optional.empty());
        when(equipmentRepository.findByMacAddress(dto.getMacAddress())).thenReturn(Optional.empty());
        when(employeeRepository.findById(newEmployeeId)).thenReturn(Optional.of(newEmployee));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(updatedEquipment);
        when(equipmentMapper.toResponseDTO(updatedEquipment)).thenReturn(new ResponseEquipmentDto());

        assertDoesNotThrow(() -> equipmentService.update(equipmentId, dto));
        verify(equipmentMapper, times(1)).updateEntityFromDTO(eq(dto), eq(existingEquipment));
        verify(equipmentRepository, times(1)).save(existingEquipment);
        assertNotNull(existingEquipment.getDateUpdated());
    }

    @Test
    void testUpdate_EquipmentNotFound_ShouldThrowException() {
        Long equipmentId = 999L;
        UpdateEquipmentDto dto = createUpdateDto(1L, null, "Test");

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentException.class, () -> equipmentService.update(equipmentId, dto));
    }

    @Test
    void testUpdate_EquipmentTypeNotFound_ShouldThrowException() {
        Long equipmentId = 100L;
        Long newTypeId = 999L;
        UpdateEquipmentDto dto = createUpdateDto(newTypeId, null, "Test");
        Equipment existingEquipment = createEquipment(equipmentId, "Old", createEquipmentType(1L, "Old"), null);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(existingEquipment));
        when(equipmentTypeRepository.findById(newTypeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentTypeException.class, () -> equipmentService.update(equipmentId, dto));
    }

    @Test
    void testUpdate_SameSerialNumber_ShouldNotCheckDuplicate() {
        Long equipmentId = 100L;
        Long typeId = 1L;
        UpdateEquipmentDto dto = createUpdateDto(typeId, null, "Updated");
        String sameSerialNumber = "SN100";
        dto.setSerialNumber(sameSerialNumber);

        EquipmentType type = createEquipmentType(typeId, "Type");
        Equipment existingEquipment = createEquipment(equipmentId, "Old", type, null);
        existingEquipment.setSerialNumber(sameSerialNumber);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(existingEquipment));
        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        // НУЖНО застабить findBySerialNumber, потому что метод ВЫЗОВЕТСЯ
        when(equipmentRepository.findBySerialNumber(sameSerialNumber)).thenReturn(Optional.of(existingEquipment));  // <- возвращаем тот же объект
        when(equipmentRepository.findByMacAddress(dto.getMacAddress())).thenReturn(Optional.empty());
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(existingEquipment);
        when(equipmentMapper.toResponseDTO(existingEquipment)).thenReturn(new ResponseEquipmentDto());

        assertDoesNotThrow(() -> equipmentService.update(equipmentId, dto));

        // Проверяем, что findBySerialNumber ВЫЗВАЛСЯ (он вызывается всегда)
        verify(equipmentRepository, times(1)).findBySerialNumber(sameSerialNumber);
    }

    @Test
    void testUpdate_DuplicateSerialNumber_ShouldThrowException() {
        Long equipmentId = 100L;
        Long typeId = 1L;
        UpdateEquipmentDto dto = createUpdateDto(typeId, null, "Updated");

        EquipmentType type = createEquipmentType(typeId, "Type");
        Equipment existingEquipment = createEquipment(equipmentId, "Old", type, null);
        Equipment anotherEquipment = createEquipment(200L, "Another", type, null);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(existingEquipment));
        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(equipmentRepository.findBySerialNumber(dto.getSerialNumber()))
                .thenReturn(Optional.of(anotherEquipment));
        // Не нужно стабить findByMacAddress, так как ошибка произойдет раньше

        assertThrows(DuplicateEquipmentNameException.class, () -> equipmentService.update(equipmentId, dto));
    }

    @Test
    void testUpdate_WithoutEmployee_ClearsEmployee() {
        Long equipmentId = 100L;
        Long typeId = 1L;
        UpdateEquipmentDto dto = createUpdateDto(typeId, null, "Updated");

        EquipmentType type = createEquipmentType(typeId, "Type");
        Employee oldEmployee = createEmployee(10L, "Old Employee");
        Equipment existingEquipment = createEquipment(equipmentId, "Old", type, oldEmployee);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(existingEquipment));
        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(equipmentRepository.findBySerialNumber(dto.getSerialNumber())).thenReturn(Optional.empty());
        when(equipmentRepository.findByMacAddress(dto.getMacAddress())).thenReturn(Optional.empty());
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(existingEquipment);
        when(equipmentMapper.toResponseDTO(existingEquipment)).thenReturn(new ResponseEquipmentDto());

        assertDoesNotThrow(() -> equipmentService.update(equipmentId, dto));
        assertNull(existingEquipment.getEmployee());
    }

    @Test
    void testDelete_Success() {
        Long equipmentId = 100L;

        when(equipmentRepository.existsById(equipmentId)).thenReturn(true);

        assertDoesNotThrow(() -> equipmentService.delete(equipmentId));
        verify(equipmentRepository, times(1)).deleteById(equipmentId);
    }

    @Test
    void testDelete_NotFound_ShouldThrowException() {
        Long equipmentId = 999L;

        when(equipmentRepository.existsById(equipmentId)).thenReturn(false);

        assertThrows(NotFoundEquipmentException.class, () -> equipmentService.delete(equipmentId));
        verify(equipmentRepository, never()).deleteById(any());
    }
}