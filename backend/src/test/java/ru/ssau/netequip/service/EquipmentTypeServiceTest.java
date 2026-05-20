package ru.ssau.netequip.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.netequip.dto.equipmentType.CreateAndUpdateEquipmentTypeDto;
import ru.ssau.netequip.dto.equipmentType.ResponseEquipmentTypeDto;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.exception.equipmentType.DuplicateEquipmentTypeNameException;
import ru.ssau.netequip.exception.equipmentType.NotFoundEquipmentTypeException;
import ru.ssau.netequip.mapper.EquipmentTypeMapper;
import ru.ssau.netequip.repository.EquipmentTypeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentTypeServiceTest {

    @Mock
    private EquipmentTypeRepository equipmentTypeRepository;

    @Mock
    private EquipmentTypeMapper equipmentTypeMapper;

    @InjectMocks
    private EquipmentTypeService equipmentTypeService;

    @Mock
    private AuditLogService auditLogService;

    private EquipmentType createEquipmentType(Long id, String typeName) {
        EquipmentType type = new EquipmentType();
        type.setId(id);
        type.setTypeName(typeName);
        return type;
    }

    private CreateAndUpdateEquipmentTypeDto createDto(String typeName) {
        CreateAndUpdateEquipmentTypeDto dto = new CreateAndUpdateEquipmentTypeDto();
        dto.setTypeName(typeName);
        return dto;
    }

    @Test
    void testCreate_Success() {
        String typeName = "Switch";
        CreateAndUpdateEquipmentTypeDto dto = createDto(typeName);
        EquipmentType entity = createEquipmentType(null, typeName);
        EquipmentType saved = createEquipmentType(100L, typeName);
        ResponseEquipmentTypeDto responseDto = new ResponseEquipmentTypeDto();
        responseDto.setId(100L);
        responseDto.setTypeName(typeName);

        when(equipmentTypeRepository.existsByTypeName(typeName)).thenReturn(false);
        when(equipmentTypeMapper.toEntity(dto)).thenReturn(entity);
        when(equipmentTypeRepository.save(any(EquipmentType.class))).thenReturn(saved);
        when(equipmentTypeMapper.toResponseDTO(saved)).thenReturn(responseDto);

        ResponseEquipmentTypeDto result = equipmentTypeService.create(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(typeName, result.getTypeName());
        verify(equipmentTypeRepository, times(1)).save(entity);
    }

    @Test
    void testCreate_DuplicateName_ShouldThrowException() {
        String typeName = "Switch";
        CreateAndUpdateEquipmentTypeDto dto = createDto(typeName);

        when(equipmentTypeRepository.existsByTypeName(typeName)).thenReturn(true);

        assertThrows(DuplicateEquipmentTypeNameException.class,
                () -> equipmentTypeService.create(dto));
        verify(equipmentTypeRepository, never()).save(any());
    }

    @Test
    void testGetById_Success() {
        Long typeId = 100L;
        String typeName = "Router";
        EquipmentType type = createEquipmentType(typeId, typeName);
        ResponseEquipmentTypeDto responseDto = new ResponseEquipmentTypeDto();
        responseDto.setId(typeId);
        responseDto.setTypeName(typeName);

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(equipmentTypeMapper.toResponseDTO(type)).thenReturn(responseDto);

        ResponseEquipmentTypeDto result = equipmentTypeService.getById(typeId);

        assertNotNull(result);
        assertEquals(typeId, result.getId());
        assertEquals(typeName, result.getTypeName());
    }

    @Test
    void testGetById_NotFound_ShouldThrowException() {
        Long typeId = 999L;

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentTypeException.class,
                () -> equipmentTypeService.getById(typeId));
    }

    @Test
    void testGetAll_Success() {
        EquipmentType type1 = createEquipmentType(1L, "Switch");
        EquipmentType type2 = createEquipmentType(2L, "Router");
        EquipmentType type3 = createEquipmentType(3L, "Firewall");

        ResponseEquipmentTypeDto dto1 = new ResponseEquipmentTypeDto();
        ResponseEquipmentTypeDto dto2 = new ResponseEquipmentTypeDto();
        ResponseEquipmentTypeDto dto3 = new ResponseEquipmentTypeDto();

        when(equipmentTypeRepository.findAll()).thenReturn(List.of(type1, type2, type3));
        when(equipmentTypeMapper.toResponseDTO(type1)).thenReturn(dto1);
        when(equipmentTypeMapper.toResponseDTO(type2)).thenReturn(dto2);
        when(equipmentTypeMapper.toResponseDTO(type3)).thenReturn(dto3);

        List<ResponseEquipmentTypeDto> result = equipmentTypeService.getAll();

        assertEquals(3, result.size());
        verify(equipmentTypeRepository, times(1)).findAll();
    }

    @Test
    void testGetAll_Empty() {
        when(equipmentTypeRepository.findAll()).thenReturn(List.of());

        List<ResponseEquipmentTypeDto> result = equipmentTypeService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success_ChangeName() {
        Long typeId = 100L;
        String oldName = "Switch";
        String newName = "Managed Switch";
        CreateAndUpdateEquipmentTypeDto dto = createDto(newName);

        EquipmentType existingType = createEquipmentType(typeId, oldName);
        EquipmentType updatedType = createEquipmentType(typeId, newName);
        ResponseEquipmentTypeDto responseDto = new ResponseEquipmentTypeDto();
        responseDto.setId(typeId);
        responseDto.setTypeName(newName);

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(existingType));
        when(equipmentTypeRepository.existsByTypeName(newName)).thenReturn(false);
        when(equipmentTypeRepository.save(any(EquipmentType.class))).thenReturn(updatedType);
        when(equipmentTypeMapper.toResponseDTO(updatedType)).thenReturn(responseDto);

        ResponseEquipmentTypeDto result = equipmentTypeService.update(typeId, dto);

        assertNotNull(result);
        assertEquals(newName, result.getTypeName());
        verify(equipmentTypeMapper, times(1)).updateEntityFromDTO(eq(dto), eq(existingType));
        verify(equipmentTypeRepository, times(1)).save(existingType);
    }

    @Test
    void testUpdate_Success_SameName_ShouldNotCheckDuplicate() {
        Long typeId = 100L;
        String sameName = "Switch";
        CreateAndUpdateEquipmentTypeDto dto = createDto(sameName);

        EquipmentType existingType = createEquipmentType(typeId, sameName);
        ResponseEquipmentTypeDto responseDto = new ResponseEquipmentTypeDto();

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(existingType));
        // Не стабим existsByTypeName - он не должен вызываться, так как имя не меняется
        when(equipmentTypeRepository.save(any(EquipmentType.class))).thenReturn(existingType);
        when(equipmentTypeMapper.toResponseDTO(existingType)).thenReturn(responseDto);

        assertDoesNotThrow(() -> equipmentTypeService.update(typeId, dto));

        // Проверяем, что existsByTypeName НЕ вызывался (потому что имя не менялось)
        verify(equipmentTypeRepository, never()).existsByTypeName(any());
    }

    @Test
    void testUpdate_NotFound_ShouldThrowException() {
        Long typeId = 999L;
        CreateAndUpdateEquipmentTypeDto dto = createDto("New Name");

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentTypeException.class,
                () -> equipmentTypeService.update(typeId, dto));
    }

    @Test
    void testUpdate_DuplicateName_ShouldThrowException() {
        Long typeId = 100L;
        String oldName = "Switch";
        String duplicateName = "Router"; // этот тип уже существует
        CreateAndUpdateEquipmentTypeDto dto = createDto(duplicateName);

        EquipmentType existingType = createEquipmentType(typeId, oldName);

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(existingType));
        when(equipmentTypeRepository.existsByTypeName(duplicateName)).thenReturn(true);

        assertThrows(DuplicateEquipmentTypeNameException.class,
                () -> equipmentTypeService.update(typeId, dto));
        verify(equipmentTypeRepository, never()).save(any());
    }

    @Test
    void testDelete_Success() {
        Long typeId = 100L;
        EquipmentType type = createEquipmentType(typeId, "Switch");

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));

        assertDoesNotThrow(() -> equipmentTypeService.delete(typeId));
        verify(equipmentTypeRepository, times(1)).deleteById(typeId);
    }

    @Test
    void testDelete_NotFound_ShouldThrowException() {
        Long typeId = 999L;

        when(equipmentTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentTypeException.class,
                () -> equipmentTypeService.delete(typeId));
        verify(equipmentTypeRepository, never()).deleteById(any());
    }
}