package com.example.netequip.repository;

import com.example.netequip.entity.Equipment;
import com.example.netequip.entity.IpAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IpAddressRepository extends JpaRepository<IpAddress, Long> {

    // Получить все IP-адреса устройства
    List<IpAddress> findByEquipment(Equipment equipment);

    // Найти IP-адрес
    Optional<IpAddress> findByIpAddress(String ipAddress);

    // Получить основной IP устройства
    Optional<IpAddress> findByEquipmentAndIsPrimary(Equipment equipment, Boolean isPrimary);

    // Получить IP по типу сети
    List<IpAddress> findByNetworkType(String networkType);

    // Получить IP устройства по типу сети
    List<IpAddress> findByEquipmentAndNetworkType(Equipment equipment, String networkType);

    // Проверка существования IP
    boolean existsByIpAddress(String ipAddress);

    // Подсчет IP-адресов устройства
    long countByEquipment(Equipment equipment);

    // Получить все устройства в подсети (по маске)
    List<IpAddress> findBySubnetMask(String subnetMask);
}
