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
import ru.ssau.netequip.dto.equipmentType.CreateAndUpdateEquipmentTypeDto;
import ru.ssau.netequip.dto.equipmentType.ResponseEquipmentTypeDto;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.repository.EquipmentTypeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EquipmentTypeControllerTest {

    @Autowired
    private EquipmentTypeController equipmentTypeController;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @BeforeEach
    void setUp() {
        // Очищаем в правильном порядке
        equipmentRepository.deleteAllInBatch();
        equipmentTypeRepository.deleteAllInBatch();
    }

    @AfterEach
    void tearDown() {
        // Удаляем в правильном порядке: сначала оборудование, потом типы
        equipmentRepository.deleteAllInBatch();
        equipmentTypeRepository.deleteAllInBatch();
    }

    @Test
    void testAddEquipmentType_Success() {
        CreateAndUpdateEquipmentTypeDto dto = new CreateAndUpdateEquipmentTypeDto();
        dto.setTypeName("Switch");

        ResponseEntity<ResponseEquipmentTypeDto> response = equipmentTypeController.addEquipmentType(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Switch", response.getBody().getTypeName());
    }

    @Test
    void testAddEquipmentType_DuplicateName_ShouldThrowException() {
        // Сначала создаем тип
        String typeName = "Router";
        CreateAndUpdateEquipmentTypeDto dto1 = new CreateAndUpdateEquipmentTypeDto();
        dto1.setTypeName(typeName);
        equipmentTypeController.addEquipmentType(dto1);

        // Пытаемся создать тип с таким же именем
        CreateAndUpdateEquipmentTypeDto dto2 = new CreateAndUpdateEquipmentTypeDto();
        dto2.setTypeName(typeName);

        assertThrows(Exception.class, () -> equipmentTypeController.addEquipmentType(dto2));
    }

    @Test
    void testGetEquipmentType_Success() {
        // Сначала создаем тип
        CreateAndUpdateEquipmentTypeDto createDto = new CreateAndUpdateEquipmentTypeDto();
        createDto.setTypeName("Firewall");
        ResponseEntity<ResponseEquipmentTypeDto> createResponse = equipmentTypeController.addEquipmentType(createDto);
        Long typeId = createResponse.getBody().getId();

        // Получаем по ID
        ResponseEntity<ResponseEquipmentTypeDto> response = equipmentTypeController.getEquipmentType(typeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(typeId, response.getBody().getId());
        assertEquals("Firewall", response.getBody().getTypeName());
    }

    @Test
    void testGetEquipmentType_NotFound_ShouldThrowException() {
        assertThrows(Exception.class, () -> equipmentTypeController.getEquipmentType(99999L));
    }

    @Test
    void testGetAllEquipmentTypes_Success() {
        // Создаем несколько типов
        CreateAndUpdateEquipmentTypeDto dto1 = new CreateAndUpdateEquipmentTypeDto();
        dto1.setTypeName("Type1");
        equipmentTypeController.addEquipmentType(dto1);

        CreateAndUpdateEquipmentTypeDto dto2 = new CreateAndUpdateEquipmentTypeDto();
        dto2.setTypeName("Type2");
        equipmentTypeController.addEquipmentType(dto2);

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<Page<ResponseEquipmentTypeDto>> response = equipmentTypeController.getAllEquipmentTypes(null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTotalElements() >= 2);
    }

    @Test
    void testGetAllEquipmentTypes_Empty() {
        // Убеждаемся, что БД пустая
        equipmentTypeRepository.deleteAllInBatch();

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<Page<ResponseEquipmentTypeDto>> response = equipmentTypeController.getAllEquipmentTypes(null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testUpdateEquipmentType_Success() {
        // Создаем тип
        CreateAndUpdateEquipmentTypeDto createDto = new CreateAndUpdateEquipmentTypeDto();
        createDto.setTypeName("Old Name");
        ResponseEntity<ResponseEquipmentTypeDto> createResponse = equipmentTypeController.addEquipmentType(createDto);
        Long typeId = createResponse.getBody().getId();

        // Обновляем тип
        CreateAndUpdateEquipmentTypeDto updateDto = new CreateAndUpdateEquipmentTypeDto();
        updateDto.setTypeName("New Name");

        ResponseEntity<ResponseEquipmentTypeDto> response = equipmentTypeController.updateEquipmentType(typeId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Name", response.getBody().getTypeName());
    }

    @Test
    void testUpdateEquipmentType_SameName_ShouldSuccess() {
        // Создаем тип
        String typeName = "Same Name";
        CreateAndUpdateEquipmentTypeDto createDto = new CreateAndUpdateEquipmentTypeDto();
        createDto.setTypeName(typeName);
        ResponseEntity<ResponseEquipmentTypeDto> createResponse = equipmentTypeController.addEquipmentType(createDto);
        Long typeId = createResponse.getBody().getId();

        // Обновляем тип с тем же именем (не должно быть ошибки)
        CreateAndUpdateEquipmentTypeDto updateDto = new CreateAndUpdateEquipmentTypeDto();
        updateDto.setTypeName(typeName);

        ResponseEntity<ResponseEquipmentTypeDto> response = equipmentTypeController.updateEquipmentType(typeId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(typeName, response.getBody().getTypeName());
    }

    @Test
    void testUpdateEquipmentType_DuplicateName_ShouldThrowException() {
        // Создаем первый тип
        CreateAndUpdateEquipmentTypeDto dto1 = new CreateAndUpdateEquipmentTypeDto();
        dto1.setTypeName("First Type");
        equipmentTypeController.addEquipmentType(dto1);

        // Создаем второй тип
        CreateAndUpdateEquipmentTypeDto dto2 = new CreateAndUpdateEquipmentTypeDto();
        dto2.setTypeName("Second Type");
        ResponseEntity<ResponseEquipmentTypeDto> createResponse = equipmentTypeController.addEquipmentType(dto2);
        Long secondTypeId = createResponse.getBody().getId();

        // Пытаемся обновить второй тип, меняя имя на имя первого типа
        CreateAndUpdateEquipmentTypeDto updateDto = new CreateAndUpdateEquipmentTypeDto();
        updateDto.setTypeName("First Type");

        assertThrows(Exception.class, () -> equipmentTypeController.updateEquipmentType(secondTypeId, updateDto));
    }

    @Test
    void testUpdateEquipmentType_NotFound_ShouldThrowException() {
        CreateAndUpdateEquipmentTypeDto updateDto = new CreateAndUpdateEquipmentTypeDto();
        updateDto.setTypeName("Any Name");

        assertThrows(Exception.class, () -> equipmentTypeController.updateEquipmentType(99999L, updateDto));
    }

    @Test
    void testDeleteEquipmentType_Success() {
        // Создаем тип
        CreateAndUpdateEquipmentTypeDto createDto = new CreateAndUpdateEquipmentTypeDto();
        createDto.setTypeName("To Delete");
        ResponseEntity<ResponseEquipmentTypeDto> createResponse = equipmentTypeController.addEquipmentType(createDto);
        Long typeId = createResponse.getBody().getId();

        // Удаляем тип
        ResponseEntity<Void> response = equipmentTypeController.deleteEquipmentType(typeId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверяем, что тип действительно удален
        assertThrows(Exception.class, () -> equipmentTypeController.getEquipmentType(typeId));
    }

    @Test
    void testDeleteEquipmentType_NotFound_ShouldThrowException() {
        assertThrows(Exception.class, () -> equipmentTypeController.deleteEquipmentType(99999L));
    }

    @Test
    void testDeleteEquipmentType_WithEquipment_ShouldThrowException() {
        // Создаем тип
        EquipmentType type = new EquipmentType();
        type.setTypeName("Type With Equipment");
        equipmentTypeRepository.save(type);

        // Создаем оборудование с этим типом
        Equipment equipment = new Equipment();
        equipment.setName("Test Equipment");
        equipment.setType(type);
        equipment.setSerialNumber("SN-TEST-001");
        equipment.setMacAddress("AA:BB:CC:DD:EE:FF");
        equipmentRepository.save(equipment);

        // Пытаемся удалить тип (должно быть исключение из-за внешнего ключа)
        // В зависимости от логики сервиса, может быть исключение или нет
        // Если в сервисе есть проверка, то будет исключение
        // Если нет, то тест покажет, что нужно добавить проверку

        // Проверяем, что тип нельзя удалить (если есть связанное оборудование)
        // Этот тест может падать, если в сервисе нет проверки - это нормально, нужно будет добавить проверку
        try {
            equipmentTypeController.deleteEquipmentType(type.getId());
            // Если дошли сюда, значит удаление прошло - нужно добавить проверку в сервис
            fail("Should not be able to delete equipment type with existing equipment");
        } catch (Exception e) {
            // Ожидаемое исключение
            assertTrue(true);
        }
    }

    @Test
    void testSearchEquipmentTypes_FindsByTypeName() {
        CreateAndUpdateEquipmentTypeDto dto1 = new CreateAndUpdateEquipmentTypeDto();
        dto1.setTypeName("Cisco Switch");
        equipmentTypeController.addEquipmentType(dto1);

        CreateAndUpdateEquipmentTypeDto dto2 = new CreateAndUpdateEquipmentTypeDto();
        dto2.setTypeName("Juniper Router");
        equipmentTypeController.addEquipmentType(dto2);

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<Page<ResponseEquipmentTypeDto>> response =
                equipmentTypeController.getAllEquipmentTypes("cisco", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Cisco Switch", response.getBody().getContent().get(0).getTypeName());
    }

    @Test
    void testSearchEquipmentTypes_CaseInsensitive() {
        CreateAndUpdateEquipmentTypeDto dto = new CreateAndUpdateEquipmentTypeDto();
        dto.setTypeName("Cisco Switch");
        equipmentTypeController.addEquipmentType(dto);

        Pageable pageable = PageRequest.of(0, 20);

        // ищем в верхнем регистре — должен найти
        ResponseEntity<Page<ResponseEquipmentTypeDto>> upper =
                equipmentTypeController.getAllEquipmentTypes("CISCO", pageable);
        assertEquals(1, upper.getBody().getTotalElements());

        // в нижнем — тоже
        ResponseEntity<Page<ResponseEquipmentTypeDto>> lower =
                equipmentTypeController.getAllEquipmentTypes("cisco", pageable);
        assertEquals(1, lower.getBody().getTotalElements());
    }

    @Test
    void testSearchEquipmentTypes_FindsByManufacturer() {
        CreateAndUpdateEquipmentTypeDto dto = new CreateAndUpdateEquipmentTypeDto();
        dto.setTypeName("Some Switch");
        dto.setManufacturer("Cisco");
        equipmentTypeController.addEquipmentType(dto);

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<Page<ResponseEquipmentTypeDto>> response =
                equipmentTypeController.getAllEquipmentTypes("cisco", pageable);

        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testSearchEquipmentTypes_EmptyResult() {
        CreateAndUpdateEquipmentTypeDto dto = new CreateAndUpdateEquipmentTypeDto();
        dto.setTypeName("Cisco");
        equipmentTypeController.addEquipmentType(dto);

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<Page<ResponseEquipmentTypeDto>> response =
                equipmentTypeController.getAllEquipmentTypes("nonexistent", pageable);

        assertEquals(0, response.getBody().getTotalElements());
        assertTrue(response.getBody().isEmpty());
    }
}