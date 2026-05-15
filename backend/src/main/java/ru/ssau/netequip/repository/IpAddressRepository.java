package ru.ssau.netequip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.IpAddress;

import java.util.List;
import java.util.Optional;

@Repository
public interface IpAddressRepository extends JpaRepository<IpAddress,Long> {
    List<IpAddress> findByEquipment(Equipment equipment);
    Optional<IpAddress> findByIpAddress(String ipAddress);
    Optional<IpAddress> findByEquipmentAndIsPrimary(Equipment equipment, Boolean isPrimary);
    List<IpAddress> findByNetworkType(String networkType);
    List<IpAddress> findByEquipmentAndNetworkType(Equipment equipment, String networkType);
    boolean existsByIpAddress(String ipAddress);
    long countByEquipment(Equipment equipment);
    List<IpAddress> findBySubnetMask(String subnetMask);
    void deleteByEquipmentId(Long equipmentId);
}
