package ru.ssau.netequip.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.netequip.dto.ipAddress.CreateAndUpdateIpAddress;
import ru.ssau.netequip.dto.ipAddress.ResponseIpAddressDto;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.IpAddress;
import ru.ssau.netequip.exception.equipment.NotFoundEquipmentException;
import ru.ssau.netequip.exception.ipAddress.DuplicateIpAddressException;
import ru.ssau.netequip.exception.ipAddress.NotFoundIpAddressException;
import ru.ssau.netequip.exception.ipAddress.PrimaryIpAddressConflictException;
import ru.ssau.netequip.mapper.IpAddressMapper;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.repository.IpAddressRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpAddressServiceTest {

    @Mock
    private IpAddressRepository ipAddressRepository;

    @Mock
    private IpAddressMapper ipAddressMapper;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private IpAddressService ipAddressService;

    private Equipment createEquipment(Long id) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setName("Test Router");
        return equipment;
    }

    private IpAddress createIpAddress(Long id, Equipment equipment, String ip, Boolean isPrimary) {
        IpAddress ipAddress = new IpAddress();
        ipAddress.setId(id);
        ipAddress.setEquipment(equipment);
        ipAddress.setIpAddress(ip);
        ipAddress.setIsPrimary(isPrimary);
        ipAddress.setAssignedDate(LocalDate.now());
        return ipAddress;
    }

    private CreateAndUpdateIpAddress createDto(Long equipmentId, String ip, Boolean isPrimary) {
        CreateAndUpdateIpAddress dto = new CreateAndUpdateIpAddress();
        dto.setEquipmentId(equipmentId);
        dto.setIpAddress(ip);
        dto.setIsPrimary(isPrimary);
        dto.setSubnetMask("255.255.255.0");
        dto.setGateway("192.168.1.1");
        return dto;
    }

    @Test
    void testCreate_Success() {
        Long equipmentId = 1L;
        String ipAddress = "192.168.1.100";
        CreateAndUpdateIpAddress dto = createDto(equipmentId, ipAddress, false);
        Equipment equipment = createEquipment(equipmentId);
        IpAddress entity = createIpAddress(null, equipment, ipAddress, false);
        IpAddress saved = createIpAddress(100L, equipment, ipAddress, false);
        ResponseIpAddressDto responseDto = new ResponseIpAddressDto();
        responseDto.setId(100L);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(ipAddressRepository.existsByIpAddress(ipAddress)).thenReturn(false);
        when(ipAddressMapper.toEntity(dto)).thenReturn(entity);
        when(ipAddressRepository.save(any(IpAddress.class))).thenReturn(saved);
        when(ipAddressMapper.toResponseDTO(saved)).thenReturn(responseDto);

        ResponseIpAddressDto result = ipAddressService.create(dto);

        assertNotNull(result);
        verify(ipAddressRepository, times(1)).save(entity);
        assertNotNull(entity.getAssignedDate());
    }

    @Test
    void testCreate_EquipmentNotFound_ShouldThrowException() {
        Long equipmentId = 999L;
        CreateAndUpdateIpAddress dto = createDto(equipmentId, "192.168.1.100", false);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentException.class, () -> ipAddressService.create(dto));
        verify(ipAddressRepository, never()).save(any());
    }

    @Test
    void testCreate_DuplicateIp_ShouldThrowException() {
        Long equipmentId = 1L;
        String ipAddress = "192.168.1.100";
        CreateAndUpdateIpAddress dto = createDto(equipmentId, ipAddress, false);
        Equipment equipment = createEquipment(equipmentId);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(ipAddressRepository.existsByIpAddress(ipAddress)).thenReturn(true);

        assertThrows(DuplicateIpAddressException.class, () -> ipAddressService.create(dto));
        verify(ipAddressRepository, never()).save(any());
    }

    @Test
    void testCreate_PrimaryIp_Success() {
        Long equipmentId = 1L;
        String ipAddress = "192.168.1.100";
        CreateAndUpdateIpAddress dto = createDto(equipmentId, ipAddress, true);
        Equipment equipment = createEquipment(equipmentId);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(ipAddressRepository.existsByIpAddress(ipAddress)).thenReturn(false);
        when(ipAddressRepository.findByEquipmentAndIsPrimary(equipment, true)).thenReturn(Optional.empty());
        when(ipAddressMapper.toEntity(dto)).thenReturn(createIpAddress(null, equipment, ipAddress, true));
        when(ipAddressRepository.save(any(IpAddress.class))).thenReturn(createIpAddress(100L, equipment, ipAddress, true));
        when(ipAddressMapper.toResponseDTO(any())).thenReturn(new ResponseIpAddressDto());

        assertDoesNotThrow(() -> ipAddressService.create(dto));
    }

    @Test
    void testCreate_PrimaryIpAlreadyExists_ShouldThrowException() {
        Long equipmentId = 1L;
        String ipAddress = "192.168.1.100";
        CreateAndUpdateIpAddress dto = createDto(equipmentId, ipAddress, true);
        Equipment equipment = createEquipment(equipmentId);
        IpAddress existingPrimary = createIpAddress(50L, equipment, "192.168.1.1", true);

        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(ipAddressRepository.existsByIpAddress(ipAddress)).thenReturn(false);
        when(ipAddressRepository.findByEquipmentAndIsPrimary(equipment, true)).thenReturn(Optional.of(existingPrimary));

        assertThrows(PrimaryIpAddressConflictException.class, () -> ipAddressService.create(dto));
        verify(ipAddressRepository, never()).save(any());
    }

    @Test
    void testGetById_Success() {
        Long ipId = 100L;
        Equipment equipment = createEquipment(1L);
        IpAddress ipAddress = createIpAddress(ipId, equipment, "192.168.1.100", false);
        ResponseIpAddressDto responseDto = new ResponseIpAddressDto();
        responseDto.setId(ipId);

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.of(ipAddress));
        when(ipAddressMapper.toResponseDTO(ipAddress)).thenReturn(responseDto);

        ResponseIpAddressDto result = ipAddressService.getById(ipId);

        assertNotNull(result);
        assertEquals(ipId, result.getId());
    }

    @Test
    void testGetById_NotFound_ShouldThrowException() {
        Long ipId = 999L;

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.empty());

        assertThrows(NotFoundIpAddressException.class, () -> ipAddressService.getById(ipId));
    }

    @Test
    void testGetAll_Success() {
        Equipment equipment = createEquipment(1L);
        IpAddress ip1 = createIpAddress(1L, equipment, "192.168.1.1", false);
        IpAddress ip2 = createIpAddress(2L, equipment, "192.168.1.2", true);
        ResponseIpAddressDto dto1 = new ResponseIpAddressDto();
        ResponseIpAddressDto dto2 = new ResponseIpAddressDto();

        when(ipAddressRepository.findAll()).thenReturn(List.of(ip1, ip2));
        when(ipAddressMapper.toResponseDTO(ip1)).thenReturn(dto1);
        when(ipAddressMapper.toResponseDTO(ip2)).thenReturn(dto2);

        List<ResponseIpAddressDto> result = ipAddressService.getAll();

        assertEquals(2, result.size());
        verify(ipAddressRepository, times(1)).findAll();
    }

    @Test
    void testGetAll_Empty() {
        when(ipAddressRepository.findAll()).thenReturn(List.of());

        List<ResponseIpAddressDto> result = ipAddressService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdate_Success() {
        Long ipId = 100L;
        Long newEquipmentId = 2L;
        String newIpAddress = "10.0.0.1";
        CreateAndUpdateIpAddress dto = createDto(newEquipmentId, newIpAddress, false);

        Equipment oldEquipment = createEquipment(1L);
        Equipment newEquipment = createEquipment(newEquipmentId);
        IpAddress existingIp = createIpAddress(ipId, oldEquipment, "192.168.1.100", false);
        IpAddress updatedIp = createIpAddress(ipId, newEquipment, newIpAddress, false);

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.of(existingIp));
        when(equipmentRepository.findById(newEquipmentId)).thenReturn(Optional.of(newEquipment));
        when(ipAddressRepository.existsByIpAddress(newIpAddress)).thenReturn(false);
        when(ipAddressRepository.save(any(IpAddress.class))).thenReturn(updatedIp);
        when(ipAddressMapper.toResponseDTO(updatedIp)).thenReturn(new ResponseIpAddressDto());

        assertDoesNotThrow(() -> ipAddressService.update(ipId, dto));
        verify(ipAddressMapper, times(1)).updateEntityFromDTO(eq(dto), eq(existingIp));
        verify(ipAddressRepository, times(1)).save(existingIp);
    }

    @Test
    void testUpdate_IpNotFound_ShouldThrowException() {
        Long ipId = 999L;
        CreateAndUpdateIpAddress dto = createDto(1L, "10.0.0.1", false);

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.empty());

        assertThrows(NotFoundIpAddressException.class, () -> ipAddressService.update(ipId, dto));
    }

    @Test
    void testUpdate_EquipmentNotFound_ShouldThrowException() {
        Long ipId = 100L;
        Long newEquipmentId = 999L;
        CreateAndUpdateIpAddress dto = createDto(newEquipmentId, "10.0.0.1", false);
        IpAddress existingIp = createIpAddress(ipId, createEquipment(1L), "192.168.1.100", false);

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.of(existingIp));
        when(equipmentRepository.findById(newEquipmentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEquipmentException.class, () -> ipAddressService.update(ipId, dto));
    }

    @Test
    void testUpdate_DuplicateIp_ShouldThrowException() {
        Long ipId = 100L;
        Long equipmentId = 1L;
        String newIpAddress = "10.0.0.1";
        CreateAndUpdateIpAddress dto = createDto(equipmentId, newIpAddress, false);

        Equipment equipment = createEquipment(equipmentId);
        IpAddress existingIp = createIpAddress(ipId, equipment, "192.168.1.100", false);

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.of(existingIp));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(ipAddressRepository.existsByIpAddress(newIpAddress)).thenReturn(true);

        assertThrows(DuplicateIpAddressException.class, () -> ipAddressService.update(ipId, dto));
    }

    @Test
    void testUpdate_SameIp_ShouldNotCheckDuplicate() {
        Long ipId = 100L;
        Long equipmentId = 1L;
        String sameIp = "192.168.1.100";
        CreateAndUpdateIpAddress dto = createDto(equipmentId, sameIp, false);

        Equipment equipment = createEquipment(equipmentId);
        IpAddress existingIp = createIpAddress(ipId, equipment, sameIp, false);

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.of(existingIp));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(ipAddressRepository.save(any(IpAddress.class))).thenReturn(existingIp);
        when(ipAddressMapper.toResponseDTO(existingIp)).thenReturn(new ResponseIpAddressDto());

        assertDoesNotThrow(() -> ipAddressService.update(ipId, dto));
        verify(ipAddressRepository, never()).existsByIpAddress(any());
    }

    @Test
    void testUpdate_ToPrimaryIp_Success() {
        Long ipId = 100L;
        Long equipmentId = 1L;
        CreateAndUpdateIpAddress dto = createDto(equipmentId, "192.168.1.100", true);

        Equipment equipment = createEquipment(equipmentId);
        IpAddress existingIp = createIpAddress(ipId, equipment, "192.168.1.100", false);

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.of(existingIp));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        // Убираем проверку на existsByIpAddress, так как IP не меняется
        // when(ipAddressRepository.existsByIpAddress("192.168.1.100")).thenReturn(false); // ЭТО ЛИШНЕЕ
        when(ipAddressRepository.findByEquipmentAndIsPrimary(equipment, true)).thenReturn(Optional.empty());
        when(ipAddressRepository.save(any(IpAddress.class))).thenReturn(existingIp);
        when(ipAddressMapper.toResponseDTO(existingIp)).thenReturn(new ResponseIpAddressDto());

        assertDoesNotThrow(() -> ipAddressService.update(ipId, dto));

        // Проверяем, что метод existsByIpAddress НЕ вызывался (так как IP не менялся)
        verify(ipAddressRepository, never()).existsByIpAddress(any());
    }

    @Test
    void testUpdate_ToPrimaryIp_ButAnotherExists_ShouldThrowException() {
        Long ipId = 100L;
        Long equipmentId = 1L;
        CreateAndUpdateIpAddress dto = createDto(equipmentId, "192.168.1.100", true);

        Equipment equipment = createEquipment(equipmentId);
        IpAddress existingIp = createIpAddress(ipId, equipment, "192.168.1.100", false);
        IpAddress anotherPrimary = createIpAddress(200L, equipment, "192.168.1.1", true);

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.of(existingIp));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        // Убираем эту строчку - она не нужна, так как IP не меняется
        // when(ipAddressRepository.existsByIpAddress("192.168.1.100")).thenReturn(false);
        when(ipAddressRepository.findByEquipmentAndIsPrimary(equipment, true)).thenReturn(Optional.of(anotherPrimary));

        assertThrows(PrimaryIpAddressConflictException.class,
                () -> ipAddressService.update(ipId, dto));

        // Проверяем, что save НЕ вызывался
        verify(ipAddressRepository, never()).save(any());
    }

    @Test
    void testDelete_Success() {
        Long ipId = 100L;
        Equipment equipment = createEquipment(1L);
        IpAddress ipAddress = createIpAddress(ipId, equipment, "192.168.1.100", false);

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.of(ipAddress));

        assertDoesNotThrow(() -> ipAddressService.delete(ipId));
        verify(ipAddressRepository, times(1)).delete(ipAddress);
    }

    @Test
    void testDelete_NotFound_ShouldThrowException() {
        Long ipId = 999L;

        when(ipAddressRepository.findById(ipId)).thenReturn(Optional.empty());

        assertThrows(NotFoundIpAddressException.class, () -> ipAddressService.delete(ipId));
        verify(ipAddressRepository, never()).delete(any());
    }
}