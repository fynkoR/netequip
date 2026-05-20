package ru.ssau.netequip.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.netequip.dto.devicePort.CreateAndUpdateDevicePortDto;
import ru.ssau.netequip.dto.devicePort.ResponseDevicePortDto;
import ru.ssau.netequip.entity.DevicePort;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.exception.devicePort.DuplicateDevicePortException;
import ru.ssau.netequip.exception.devicePort.InvalidPortConnectionException;
import ru.ssau.netequip.exception.devicePort.NotFoundDevicePortException;
import ru.ssau.netequip.exception.equipment.NotFoundEquipmentException;
import ru.ssau.netequip.mapper.DevicePortMapper;
import ru.ssau.netequip.repository.DevicePortRepository;
import ru.ssau.netequip.repository.EquipmentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevicePortServiceTest {

    @Mock
    private DevicePortRepository devicePortRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private DevicePortMapper devicePortMapper;

    @InjectMocks
    private DevicePortService devicePortService;

    @Mock
    private AuditLogService auditLogService;

    private Equipment createEquipment(Long id) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setName("Test Switch");
        return equipment;
    }

    private DevicePort createDevicePort(Long id, Equipment equipment, Integer portNumber) {
        DevicePort port = new DevicePort();
        port.setId(id);
        port.setEquipment(equipment);
        port.setPortNumber(portNumber);
        return port;
    }

    private CreateAndUpdateDevicePortDto createDto(Long equipmentId, Integer portNumber) {
        CreateAndUpdateDevicePortDto dto = new CreateAndUpdateDevicePortDto();
        dto.setEquipmentId(equipmentId);
        dto.setPortNumber(portNumber);
        dto.setPortType("GigabitEthernet0/" + portNumber);
        dto.setDescription("Test port");
        return dto;
    }

    @Test
    void testCreate_Success() {
        Long equipmentId = 1L;
        Integer portNumber = 24;
        CreateAndUpdateDevicePortDto dto = createDto(equipmentId, portNumber);
        Equipment equipment = createEquipment(equipmentId);
        DevicePort devicePort = createDevicePort(null, equipment, portNumber);
        DevicePort savedPort = createDevicePort(100L, equipment, portNumber);
        ResponseDevicePortDto responseDto = new ResponseDevicePortDto();
        responseDto.setId(100L);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(devicePortRepository.findByEquipmentAndPortNumber(equipment, portNumber)).thenReturn(Optional.empty());
        when(devicePortMapper.toEntity(dto)).thenReturn(devicePort);
        when(devicePortRepository.save(any(DevicePort.class))).thenReturn(savedPort);
        when(devicePortMapper.toResponseDTO(savedPort)).thenReturn(responseDto);

        ResponseDevicePortDto result = devicePortService.create(dto);

        assertNotNull(result);
        verify(devicePortRepository, times(1)).save(devicePort);
    }

    @Test
    void testCreate_EquipmentNotFound_ShouldThrowException() {
        Long equipmentId = 999L;
        CreateAndUpdateDevicePortDto dto = createDto(equipmentId, 24);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentException.class, () -> devicePortService.create(dto));
        verify(devicePortRepository, never()).save(any());
    }

    @Test
    void testCreate_DuplicatePortNumber_ShouldThrowException() {
        Long equipmentId = 1L;
        Integer portNumber = 24;
        CreateAndUpdateDevicePortDto dto = createDto(equipmentId, portNumber);
        Equipment equipment = createEquipment(equipmentId);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(devicePortRepository.findByEquipmentAndPortNumber(equipment, portNumber))
                .thenReturn(Optional.of(new DevicePort()));

        assertThrows(DuplicateDevicePortException.class, () -> devicePortService.create(dto));
        verify(devicePortRepository, never()).save(any());
    }

    @Test
    void testCreate_WithConnection_Success() {
        Long equipmentId = 1L;
        Long connectedToEquipmentId = 2L;
        Long connectedToPortId = 50L;
        CreateAndUpdateDevicePortDto dto = createDto(equipmentId, 24);
        dto.setConnectedToEquipmentId(connectedToEquipmentId);
        dto.setConnectedToPortId(connectedToPortId);

        Equipment equipment = createEquipment(equipmentId);
        Equipment connectedEquipment = createEquipment(connectedToEquipmentId);
        DevicePort connectedPort = createDevicePort(connectedToPortId, connectedEquipment, 10);
        DevicePort devicePort = createDevicePort(null, equipment, 24);
        DevicePort savedPort = createDevicePort(100L, equipment, 24);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.findById(connectedToEquipmentId)).thenReturn(Optional.of(connectedEquipment));
        when(devicePortRepository.findById(connectedToPortId)).thenReturn(Optional.of(connectedPort));
        when(devicePortRepository.findByEquipmentAndPortNumber(equipment, 24)).thenReturn(Optional.empty());
        when(devicePortMapper.toEntity(dto)).thenReturn(devicePort);
        when(devicePortRepository.save(any(DevicePort.class))).thenReturn(savedPort);
        when(devicePortMapper.toResponseDTO(savedPort)).thenReturn(new ResponseDevicePortDto());

        assertDoesNotThrow(() -> devicePortService.create(dto));
        verify(devicePortRepository, times(1)).save(devicePort);
    }

    @Test
    void testCreate_InvalidConnection_ShouldThrowException() {
        Long equipmentId = 1L;
        Long connectedToEquipmentId = 3L;
        Long connectedToPortId = 50L;
        CreateAndUpdateDevicePortDto dto = createDto(equipmentId, 24);
        dto.setConnectedToEquipmentId(connectedToEquipmentId);
        dto.setConnectedToPortId(connectedToPortId);

        Equipment equipment = createEquipment(equipmentId);
        Equipment connectedEquipment = createEquipment(2L); // Different ID!
        DevicePort connectedPort = createDevicePort(connectedToPortId, connectedEquipment, 10);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.findById(connectedToEquipmentId)).thenReturn(Optional.of(connectedEquipment));
        when(devicePortRepository.findById(connectedToPortId)).thenReturn(Optional.of(connectedPort));
        when(devicePortRepository.findByEquipmentAndPortNumber(equipment, 24)).thenReturn(Optional.empty());
        when(devicePortMapper.toEntity(dto)).thenReturn(createDevicePort(null, equipment, 24));

        assertThrows(InvalidPortConnectionException.class, () -> devicePortService.create(dto));
    }

    @Test
    void testGetById_Success() {
        Long portId = 100L;
        Equipment equipment = createEquipment(1L);
        DevicePort devicePort = createDevicePort(portId, equipment, 24);
        ResponseDevicePortDto responseDto = new ResponseDevicePortDto();
        responseDto.setId(portId);

        when(devicePortRepository.findById(portId)).thenReturn(Optional.of(devicePort));
        when(devicePortMapper.toResponseDTO(devicePort)).thenReturn(responseDto);

        ResponseDevicePortDto result = devicePortService.getById(portId);

        assertNotNull(result);
        assertEquals(portId, result.getId());
    }

    @Test
    void testGetById_NotFound_ShouldThrowException() {
        Long portId = 999L;

        when(devicePortRepository.findById(portId)).thenReturn(Optional.empty());

        assertThrows(NotFoundDevicePortException.class, () -> devicePortService.getById(portId));
    }

    @Test
    void testGetAll_Success() {
        Equipment equipment = createEquipment(1L);
        DevicePort port1 = createDevicePort(1L, equipment, 24);
        DevicePort port2 = createDevicePort(2L, equipment, 25);
        ResponseDevicePortDto dto1 = new ResponseDevicePortDto();
        ResponseDevicePortDto dto2 = new ResponseDevicePortDto();

        when(devicePortRepository.findAll()).thenReturn(List.of(port1, port2));
        when(devicePortMapper.toResponseDTO(port1)).thenReturn(dto1);
        when(devicePortMapper.toResponseDTO(port2)).thenReturn(dto2);

        List<ResponseDevicePortDto> result = devicePortService.getAll();

        assertEquals(2, result.size());
        verify(devicePortRepository, times(1)).findAll();
    }

    @Test
    void testGetAll_Empty() {
        when(devicePortRepository.findAll()).thenReturn(List.of());

        List<ResponseDevicePortDto> result = devicePortService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success() {
        Long portId = 100L;
        Long newEquipmentId = 2L;
        Integer newPortNumber = 25;
        CreateAndUpdateDevicePortDto dto = createDto(newEquipmentId, newPortNumber);

        Equipment oldEquipment = createEquipment(1L);
        Equipment newEquipment = createEquipment(newEquipmentId);
        DevicePort existingPort = createDevicePort(portId, oldEquipment, 24);
        DevicePort updatedPort = createDevicePort(portId, newEquipment, newPortNumber);

        when(devicePortRepository.findById(portId)).thenReturn(Optional.of(existingPort));
        when(equipmentRepository.findById(newEquipmentId)).thenReturn(Optional.of(newEquipment));
        when(devicePortRepository.findByEquipmentAndPortNumber(newEquipment, newPortNumber))
                .thenReturn(Optional.empty());
        when(devicePortRepository.save(any(DevicePort.class))).thenReturn(updatedPort);
        when(devicePortMapper.toResponseDTO(updatedPort)).thenReturn(new ResponseDevicePortDto());

        assertDoesNotThrow(() -> devicePortService.update(portId, dto));
        verify(devicePortMapper, times(1)).updateEntityFromDTO(eq(dto), eq(existingPort));
        verify(devicePortRepository, times(1)).save(existingPort);
    }

    @Test
    void testUpdate_PortNotFound_ShouldThrowException() {
        Long portId = 999L;
        CreateAndUpdateDevicePortDto dto = createDto(1L, 24);

        when(devicePortRepository.findById(portId)).thenReturn(Optional.empty());

        assertThrows(NotFoundDevicePortException.class, () -> devicePortService.update(portId, dto));
    }

    @Test
    void testUpdate_EquipmentNotFound_ShouldThrowException() {
        Long portId = 100L;
        Long newEquipmentId = 999L;
        CreateAndUpdateDevicePortDto dto = createDto(newEquipmentId, 24);
        DevicePort existingPort = createDevicePort(portId, createEquipment(1L), 24);

        when(devicePortRepository.findById(portId)).thenReturn(Optional.of(existingPort));
        when(equipmentRepository.findById(newEquipmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentException.class, () -> devicePortService.update(portId, dto));
    }

    @Test
    void testUpdate_DuplicatePortNumber_ShouldThrowException() {
        Long portId = 100L;
        Long equipmentId = 1L;
        Integer newPortNumber = 25;
        CreateAndUpdateDevicePortDto dto = createDto(equipmentId, newPortNumber);

        Equipment equipment = createEquipment(equipmentId);
        DevicePort existingPort = createDevicePort(portId, equipment, 24);

        when(devicePortRepository.findById(portId)).thenReturn(Optional.of(existingPort));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(devicePortRepository.findByEquipmentAndPortNumber(equipment, newPortNumber))
                .thenReturn(Optional.of(new DevicePort()));

        assertThrows(DuplicateDevicePortException.class, () -> devicePortService.update(portId, dto));
    }

    @Test
    void testUpdate_SamePortNumber_ShouldNotCheckDuplicate() {
        Long portId = 100L;
        Long equipmentId = 1L;
        Integer samePortNumber = 24;
        CreateAndUpdateDevicePortDto dto = createDto(equipmentId, samePortNumber);

        Equipment equipment = createEquipment(equipmentId);
        DevicePort existingPort = createDevicePort(portId, equipment, samePortNumber);

        when(devicePortRepository.findById(portId)).thenReturn(Optional.of(existingPort));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(devicePortRepository.save(any(DevicePort.class))).thenReturn(existingPort);
        when(devicePortMapper.toResponseDTO(existingPort)).thenReturn(new ResponseDevicePortDto());

        assertDoesNotThrow(() -> devicePortService.update(portId, dto));
        verify(devicePortRepository, never()).findByEquipmentAndPortNumber(any(), any());
    }

    @Test
    void testDelete_Success() {
        Long portId = 100L;
        Equipment equipment = createEquipment(1L);
        DevicePort devicePort = createDevicePort(portId, equipment, 24);

        when(devicePortRepository.findById(portId)).thenReturn(Optional.of(devicePort));
        when(devicePortRepository.findByConnectedToPort(devicePort)).thenReturn(List.of());

        assertDoesNotThrow(() -> devicePortService.delete(portId));
        verify(devicePortRepository, times(1)).deleteById(portId);
    }

    @Test
    void testDelete_WithLinkedPorts_ClearsConnections() {
        Long portId = 100L;
        Equipment equipment = createEquipment(1L);
        DevicePort devicePort = createDevicePort(portId, equipment, 24);
        DevicePort linkedPort1 = createDevicePort(200L, equipment, 25);
        DevicePort linkedPort2 = createDevicePort(201L, equipment, 26);

        when(devicePortRepository.findById(portId)).thenReturn(Optional.of(devicePort));
        when(devicePortRepository.findByConnectedToPort(devicePort)).thenReturn(List.of(linkedPort1, linkedPort2));
        when(devicePortRepository.save(any(DevicePort.class))).thenReturn(linkedPort1, linkedPort2);

        assertDoesNotThrow(() -> devicePortService.delete(portId));

        verify(devicePortRepository, times(2)).save(any(DevicePort.class));
        verify(devicePortRepository, times(1)).deleteById(portId);

        assertNull(linkedPort1.getConnectedToPort());
        assertNull(linkedPort1.getConnectedToEquipment());
    }

    @Test
    void testDelete_NotFound_ShouldThrowException() {
        Long portId = 999L;

        when(devicePortRepository.findById(portId)).thenReturn(Optional.empty());

        assertThrows(NotFoundDevicePortException.class, () -> devicePortService.delete(portId));
        verify(devicePortRepository, never()).deleteById(any());
    }
}