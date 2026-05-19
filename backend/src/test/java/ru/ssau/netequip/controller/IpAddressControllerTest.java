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
import ru.ssau.netequip.dto.ipAddress.CreateAndUpdateIpAddress;
import ru.ssau.netequip.dto.ipAddress.ResponseIpAddressDto;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.entity.IpAddress;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.repository.EquipmentTypeRepository;
import ru.ssau.netequip.repository.IpAddressRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IpAddressControllerTest {

    @Autowired
    private IpAddressController ipAddressController;

    @Autowired
    private IpAddressRepository ipAddressRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    private Equipment testEquipment;

    @BeforeEach
    void setUp() {
        // Очищаем в правильном порядке
        ipAddressRepository.deleteAllInBatch();
        equipmentRepository.deleteAllInBatch();
        equipmentTypeRepository.deleteAllInBatch();

        // Создаем тип оборудования
        EquipmentType type = new EquipmentType();
        type.setTypeName("Router");
        equipmentTypeRepository.save(type);

        // Создаем оборудование для тестов
        testEquipment = new Equipment();
        testEquipment.setName("Test Router");
        testEquipment.setType(type);
        testEquipment.setSerialNumber("SN-ROUTER-001");
        testEquipment.setMacAddress("AA:BB:CC:DD:EE:01");
        equipmentRepository.save(testEquipment);
    }

    @AfterEach
    void tearDown() {
        // Удаляем в правильном порядке
        ipAddressRepository.deleteAllInBatch();
        equipmentRepository.deleteAllInBatch();
        equipmentTypeRepository.deleteAllInBatch();
    }

    @Test
    void testAddIpAddress_Success() {
        CreateAndUpdateIpAddress dto = new CreateAndUpdateIpAddress();
        dto.setEquipmentId(testEquipment.getId());
        dto.setIpAddress("192.168.1.100");
        dto.setSubnetMask("255.255.255.0");
        dto.setGateway("192.168.1.1");
        dto.setIsPrimary(false);

        ResponseEntity<ResponseIpAddressDto> response = ipAddressController.addIpAddress(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("192.168.1.100", response.getBody().getIpAddress());
    }

    @Test
    void testAddIpAddress_Primary_Success() {
        CreateAndUpdateIpAddress dto = new CreateAndUpdateIpAddress();
        dto.setEquipmentId(testEquipment.getId());
        dto.setIpAddress("192.168.1.1");
        dto.setSubnetMask("255.255.255.0");
        dto.setGateway("192.168.1.1");
        dto.setIsPrimary(true);

        ResponseEntity<ResponseIpAddressDto> response = ipAddressController.addIpAddress(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getIsPrimary());
    }

    @Test
    void testAddIpAddress_DuplicateIp_ShouldThrowException() {
        String ipAddress = "192.168.1.200";

        // Создаем первый IP
        CreateAndUpdateIpAddress dto1 = new CreateAndUpdateIpAddress();
        dto1.setEquipmentId(testEquipment.getId());
        dto1.setIpAddress(ipAddress);
        dto1.setSubnetMask("255.255.255.0");
        dto1.setGateway("192.168.1.1");
        dto1.setIsPrimary(false);
        ipAddressController.addIpAddress(dto1);

        // Пытаемся создать дубликат
        CreateAndUpdateIpAddress dto2 = new CreateAndUpdateIpAddress();
        dto2.setEquipmentId(testEquipment.getId());
        dto2.setIpAddress(ipAddress);
        dto2.setSubnetMask("255.255.255.0");
        dto2.setGateway("192.168.1.1");
        dto2.setIsPrimary(false);

        assertThrows(Exception.class, () -> ipAddressController.addIpAddress(dto2));
    }

    @Test
    void testAddIpAddress_EquipmentNotFound_ShouldThrowException() {
        CreateAndUpdateIpAddress dto = new CreateAndUpdateIpAddress();
        dto.setEquipmentId(99999L);
        dto.setIpAddress("10.0.0.1");
        dto.setSubnetMask("255.255.255.0");
        dto.setGateway("10.0.0.1");
        dto.setIsPrimary(false);

        assertThrows(Exception.class, () -> ipAddressController.addIpAddress(dto));
    }

    @Test
    void testAddIpAddress_TwoPrimaryIps_ShouldThrowException() {
        // Создаем первый основной IP
        CreateAndUpdateIpAddress dto1 = new CreateAndUpdateIpAddress();
        dto1.setEquipmentId(testEquipment.getId());
        dto1.setIpAddress("192.168.1.1");
        dto1.setSubnetMask("255.255.255.0");
        dto1.setGateway("192.168.1.1");
        dto1.setIsPrimary(true);
        ipAddressController.addIpAddress(dto1);

        // Пытаемся создать второй основной IP на том же оборудовании
        CreateAndUpdateIpAddress dto2 = new CreateAndUpdateIpAddress();
        dto2.setEquipmentId(testEquipment.getId());
        dto2.setIpAddress("192.168.1.2");
        dto2.setSubnetMask("255.255.255.0");
        dto2.setGateway("192.168.1.1");
        dto2.setIsPrimary(true);

        assertThrows(Exception.class, () -> ipAddressController.addIpAddress(dto2));
    }

    @Test
    void testGetIpAddressById_Success() {
        // Сначала создаем IP
        CreateAndUpdateIpAddress createDto = new CreateAndUpdateIpAddress();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setIpAddress("10.10.10.10");
        createDto.setSubnetMask("255.255.255.0");
        createDto.setGateway("10.10.10.1");
        createDto.setIsPrimary(false);
        ResponseEntity<ResponseIpAddressDto> createResponse = ipAddressController.addIpAddress(createDto);
        Long ipId = createResponse.getBody().getId();

        // Получаем по ID
        ResponseEntity<ResponseIpAddressDto> response = ipAddressController.getIpAddressById(ipId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ipId, response.getBody().getId());
        assertEquals("10.10.10.10", response.getBody().getIpAddress());
    }

    @Test
    void testGetIpAddressById_NotFound_ShouldThrowException() {
        assertThrows(Exception.class, () -> ipAddressController.getIpAddressById(99999L));
    }

    @Test
    void testGetAllIpAddress_Success() {
        // Создаем несколько IP
        CreateAndUpdateIpAddress dto1 = new CreateAndUpdateIpAddress();
        dto1.setEquipmentId(testEquipment.getId());
        dto1.setIpAddress("192.168.1.10");
        dto1.setSubnetMask("255.255.255.0");
        dto1.setGateway("192.168.1.1");
        dto1.setIsPrimary(false);
        ipAddressController.addIpAddress(dto1);

        CreateAndUpdateIpAddress dto2 = new CreateAndUpdateIpAddress();
        dto2.setEquipmentId(testEquipment.getId());
        dto2.setIpAddress("192.168.1.20");
        dto2.setSubnetMask("255.255.255.0");
        dto2.setGateway("192.168.1.1");
        dto2.setIsPrimary(false);
        ipAddressController.addIpAddress(dto2);

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<Page<ResponseIpAddressDto>> response = ipAddressController.getAllIpAddress(null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTotalElements() >= 2);
    }

    @Test
    void testGetAllIpAddress_Empty() {
        // Убеждаемся, что IP адресов нет
        ipAddressRepository.deleteAllInBatch();

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<Page<ResponseIpAddressDto>> response = ipAddressController.getAllIpAddress(null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testUpdateIpAddress_Success() {
        // Создаем IP
        CreateAndUpdateIpAddress createDto = new CreateAndUpdateIpAddress();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setIpAddress("172.16.1.100");
        createDto.setSubnetMask("255.255.0.0");
        createDto.setGateway("172.16.1.1");
        createDto.setIsPrimary(false);
        ResponseEntity<ResponseIpAddressDto> createResponse = ipAddressController.addIpAddress(createDto);
        Long ipId = createResponse.getBody().getId();

        // Обновляем IP
        CreateAndUpdateIpAddress updateDto = new CreateAndUpdateIpAddress();
        updateDto.setEquipmentId(testEquipment.getId());
        updateDto.setIpAddress("172.16.1.200");
        updateDto.setSubnetMask("255.255.0.0");
        updateDto.setGateway("172.16.1.1");
        updateDto.setIsPrimary(true);

        ResponseEntity<ResponseIpAddressDto> response = ipAddressController.updateIpAddress(ipId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("172.16.1.200", response.getBody().getIpAddress());
        assertTrue(response.getBody().getIsPrimary());
    }

    @Test
    void testUpdateIpAddress_SameIp_ShouldSuccess() {
        // Создаем IP
        String sameIp = "10.20.30.40";
        CreateAndUpdateIpAddress createDto = new CreateAndUpdateIpAddress();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setIpAddress(sameIp);
        createDto.setSubnetMask("255.255.255.0");
        createDto.setGateway("10.20.30.1");
        createDto.setIsPrimary(false);
        ResponseEntity<ResponseIpAddressDto> createResponse = ipAddressController.addIpAddress(createDto);
        Long ipId = createResponse.getBody().getId();

        // Обновляем IP с тем же адресом
        CreateAndUpdateIpAddress updateDto = new CreateAndUpdateIpAddress();
        updateDto.setEquipmentId(testEquipment.getId());
        updateDto.setIpAddress(sameIp);
        updateDto.setSubnetMask("255.255.255.0");
        updateDto.setGateway("10.20.30.1");
        updateDto.setIsPrimary(true);

        ResponseEntity<ResponseIpAddressDto> response = ipAddressController.updateIpAddress(ipId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getIsPrimary());
    }

    @Test
    void testUpdateIpAddress_DuplicateIp_ShouldThrowException() {
        // Создаем первый IP
        CreateAndUpdateIpAddress dto1 = new CreateAndUpdateIpAddress();
        dto1.setEquipmentId(testEquipment.getId());
        dto1.setIpAddress("8.8.8.8");
        dto1.setSubnetMask("255.255.255.0");
        dto1.setGateway("8.8.8.1");
        dto1.setIsPrimary(false);
        ipAddressController.addIpAddress(dto1);

        // Создаем второй IP
        CreateAndUpdateIpAddress dto2 = new CreateAndUpdateIpAddress();
        dto2.setEquipmentId(testEquipment.getId());
        dto2.setIpAddress("8.8.4.4");
        dto2.setSubnetMask("255.255.255.0");
        dto2.setGateway("8.8.4.1");
        dto2.setIsPrimary(false);
        ResponseEntity<ResponseIpAddressDto> createResponse = ipAddressController.addIpAddress(dto2);
        Long secondIpId = createResponse.getBody().getId();

        // Пытаемся обновить второй IP, меняя его на адрес первого
        CreateAndUpdateIpAddress updateDto = new CreateAndUpdateIpAddress();
        updateDto.setEquipmentId(testEquipment.getId());
        updateDto.setIpAddress("8.8.8.8");
        updateDto.setSubnetMask("255.255.255.0");
        updateDto.setGateway("8.8.8.1");
        updateDto.setIsPrimary(false);

        assertThrows(Exception.class, () -> ipAddressController.updateIpAddress(secondIpId, updateDto));
    }

    @Test
    void testUpdateIpAddress_NotFound_ShouldThrowException() {
        CreateAndUpdateIpAddress updateDto = new CreateAndUpdateIpAddress();
        updateDto.setEquipmentId(testEquipment.getId());
        updateDto.setIpAddress("1.1.1.1");
        updateDto.setSubnetMask("255.255.255.0");
        updateDto.setGateway("1.1.1.1");
        updateDto.setIsPrimary(false);

        assertThrows(Exception.class, () -> ipAddressController.updateIpAddress(99999L, updateDto));
    }

    @Test
    void testDeleteIpAddress_Success() {
        // Создаем IP
        CreateAndUpdateIpAddress createDto = new CreateAndUpdateIpAddress();
        createDto.setEquipmentId(testEquipment.getId());
        createDto.setIpAddress("192.168.99.99");
        createDto.setSubnetMask("255.255.255.0");
        createDto.setGateway("192.168.99.1");
        createDto.setIsPrimary(false);
        ResponseEntity<ResponseIpAddressDto> createResponse = ipAddressController.addIpAddress(createDto);
        Long ipId = createResponse.getBody().getId();

        // Удаляем IP
        ResponseEntity<Void> response = ipAddressController.deleteIpAddress(ipId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверяем, что IP действительно удален
        assertThrows(Exception.class, () -> ipAddressController.getIpAddressById(ipId));
    }

    @Test
    void testDeleteIpAddress_NotFound_ShouldThrowException() {
        assertThrows(Exception.class, () -> ipAddressController.deleteIpAddress(99999L));
    }
}