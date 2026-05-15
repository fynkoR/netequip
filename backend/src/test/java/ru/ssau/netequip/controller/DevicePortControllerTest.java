package ru.ssau.netequip.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.ssau.netequip.dto.devicePort.CreateAndUpdateDevicePortDto;
import ru.ssau.netequip.dto.devicePort.ResponseDevicePortDto;
import ru.ssau.netequip.entity.DevicePort;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.repository.DevicePortRepository;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.repository.EquipmentTypeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DevicePortControllerTest {

    @Autowired
    private DevicePortController devicePortController;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private DevicePortRepository devicePortRepository;

    private Equipment testEquipment;
    private Equipment connectedEquipment;
    private DevicePort connectedPort; // Создадим реальный порт для подключения

    @BeforeEach
    void setUp() {
        // Очищаем в правильном порядке (сначала порты, потом оборудование, потом типы)
        devicePortRepository.deleteAllInBatch();
        equipmentRepository.deleteAllInBatch();
        equipmentTypeRepository.deleteAllInBatch();

        // Создаем тип оборудования
        EquipmentType type = new EquipmentType();
        type.setTypeName("Switch");
        equipmentTypeRepository.save(type);

        // Создаем основное оборудование
        testEquipment = new Equipment();
        testEquipment.setName("Test Switch 1");
        testEquipment.setType(type);
        testEquipment.setSerialNumber("SN-TEST-001");
        testEquipment.setMacAddress("AA:BB:CC:DD:EE:01");
        equipmentRepository.save(testEquipment);

        // Создаем оборудование для подключения
        connectedEquipment = new Equipment();
        connectedEquipment.setName("Test Switch 2");
        connectedEquipment.setType(type);
        connectedEquipment.setSerialNumber("SN-TEST-002");
        connectedEquipment.setMacAddress("AA:BB:CC:DD:EE:02");
        equipmentRepository.save(connectedEquipment);

        // Создаем порт на connectedEquipment для подключения
        connectedPort = new DevicePort();
        connectedPort.setEquipment(connectedEquipment);
        connectedPort.setPortNumber(10);
        connectedPort.setPortType("GigabitEthernet0/10");
        devicePortRepository.save(connectedPort);
    }

    @AfterEach
    void tearDown() {
        // Удаляем в правильном порядке: сначала порты, потом оборудование, потом типы
        devicePortRepository.deleteAllInBatch();
        equipmentRepository.deleteAllInBatch();
        equipmentTypeRepository.deleteAllInBatch();
    }

    @Test
    void testAddDevicePort_Success() {
        CreateAndUpdateDevicePortDto dto = new CreateAndUpdateDevicePortDto();
        dto.setEquipmentId(testEquipment.getId());
        dto.setPortNumber(24);
        dto.setPortType("GigabitEthernet0/24");
        dto.setDescription("Test port");

        ResponseEntity<ResponseDevicePortDto> response = devicePortController.addDevicePort(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(24, response.getBody().getPortNumber());
    }

    @Test
    void testAddDevicePort_WithConnection_Success() {
        CreateAndUpdateDevicePortDto dto = new CreateAndUpdateDevicePortDto();
        dto.setEquipmentId(testEquipment.getId());
        dto.setPortNumber(24);
        dto.setPortType("GigabitEthernet0/24");
        dto.setConnectedToEquipmentId(connectedEquipment.getId());
        dto.setConnectedToPortId(connectedPort.getId()); // Используем реальный ID порта

        ResponseEntity<ResponseDevicePortDto> response = devicePortController.addDevicePort(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetDevicePortById_Success() {
        // Сначала создаем порт
        CreateAndUpdateDevicePortDto createDto = new CreateAndUpdateDevicePortDto();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setPortNumber(1);
        ResponseEntity<ResponseDevicePortDto> createResponse = devicePortController.addDevicePort(createDto);
        Long portId = createResponse.getBody().getId();

        // Получаем по ID
        ResponseEntity<ResponseDevicePortDto> response = devicePortController.getDevicePortById(portId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(portId, response.getBody().getId());
    }

    @Test
    void testGetDevicePortById_NotFound_ShouldThrowException() {
        assertThrows(Exception.class, () -> devicePortController.getDevicePortById(99999L));
    }

    @Test
    void testGetAllDevicePorts_Success() {
        // Создаем несколько портов
        CreateAndUpdateDevicePortDto dto1 = new CreateAndUpdateDevicePortDto();
        dto1.setEquipmentId(testEquipment.getId());
        dto1.setPortNumber(1);
        devicePortController.addDevicePort(dto1);

        CreateAndUpdateDevicePortDto dto2 = new CreateAndUpdateDevicePortDto();
        dto2.setEquipmentId(testEquipment.getId());
        dto2.setPortNumber(2);
        devicePortController.addDevicePort(dto2);

        ResponseEntity<List<ResponseDevicePortDto>> response = devicePortController.getAllDevicePorts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 2);
    }

    @Test
    void testUpdateDevicePort_Success() {
        // Создаем порт
        CreateAndUpdateDevicePortDto createDto = new CreateAndUpdateDevicePortDto();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setPortNumber(5);
        ResponseEntity<ResponseDevicePortDto> createResponse = devicePortController.addDevicePort(createDto);
        Long portId = createResponse.getBody().getId();

        // Обновляем порт
        CreateAndUpdateDevicePortDto updateDto = new CreateAndUpdateDevicePortDto();
        updateDto.setEquipmentId(testEquipment.getId());
        updateDto.setPortNumber(5);
        updateDto.setPortType("Updated Port Name");
        updateDto.setDescription("Updated description");

        ResponseEntity<ResponseDevicePortDto> response = devicePortController.updateDevicePort(portId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Port Name", response.getBody().getPortType());
    }

    @Test
    void testDeleteDevicePort_Success() {
        // Создаем порт
        CreateAndUpdateDevicePortDto createDto = new CreateAndUpdateDevicePortDto();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setPortNumber(10);
        ResponseEntity<ResponseDevicePortDto> createResponse = devicePortController.addDevicePort(createDto);
        Long portId = createResponse.getBody().getId();

        // Удаляем порт
        ResponseEntity<Void> response = devicePortController.deleteDevicePort(portId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверяем, что порт действительно удален
        assertThrows(Exception.class, () -> devicePortController.getDevicePortById(portId));
    }
}