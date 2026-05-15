package ru.ssau.netequip.discovery.service;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;
import ru.ssau.netequip.discovery.dto.SnmpDeviceInfo;
import ru.ssau.netequip.discovery.dto.SnmpIpInfo;
import ru.ssau.netequip.discovery.dto.SnmpSystemInfo;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;
import ru.ssau.netequip.discovery.dto.SnmpPortInfo;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class SnmpQueryService {

    // OID-ы из MIB-II (RFC 1213) — system group
    private static final String OID_SYS_DESCR    = "1.3.6.1.2.1.1.1.0";
    private static final String OID_SYS_OBJECT_ID = "1.3.6.1.2.1.1.2.0";
    private static final String OID_SYS_UPTIME   = "1.3.6.1.2.1.1.3.0";
    private static final String OID_SYS_NAME     = "1.3.6.1.2.1.1.5.0";
    private static final String OID_SYS_LOCATION = "1.3.6.1.2.1.1.6.0";

    // OID-ы из ifTable (RFC 1213)
    private static final String OID_IF_DESCR        = "1.3.6.1.2.1.2.2.1.2";
    private static final String OID_IF_TYPE         = "1.3.6.1.2.1.2.2.1.3";
    private static final String OID_IF_SPEED        = "1.3.6.1.2.1.2.2.1.5";
    private static final String OID_IF_PHYS_ADDRESS = "1.3.6.1.2.1.2.2.1.6";
    private static final String OID_IF_ADMIN_STATUS = "1.3.6.1.2.1.2.2.1.7";
    private static final String OID_IF_OPER_STATUS  = "1.3.6.1.2.1.2.2.1.8";

    // OID-ы из ipAddrTable (RFC 1213)
    private static final String OID_IP_ENT_IF_INDEX = "1.3.6.1.2.1.4.20.1.2";
    private static final String OID_IP_ENT_NETMASK  = "1.3.6.1.2.1.4.20.1.3";

    // Параметры по умолчанию
    private static final int    SNMP_TIMEOUT_MS  = 2000;
    private static final int    SNMP_RETRIES     = 1;

    public Optional<SnmpSystemInfo> querySystem(String ipAddress, int port, String community) {
        log.info("Querying SNMP agent at {}:{} with community '{}'", ipAddress, port, community);

        Snmp snmp = null;
        try {
            // 1. Создаём транспорт UDP и открываем сокет
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            // 2. Описываем целевого агента
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(GenericAddress.parse("udp:" + ipAddress + "/" + port));
            target.setVersion(SnmpConstants.version2c);
            target.setTimeout(SNMP_TIMEOUT_MS);
            target.setRetries(SNMP_RETRIES);

            // 3. Формируем PDU: один запрос на пять OID-ов
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(OID_SYS_DESCR)));
            pdu.add(new VariableBinding(new OID(OID_SYS_OBJECT_ID)));
            pdu.add(new VariableBinding(new OID(OID_SYS_UPTIME)));
            pdu.add(new VariableBinding(new OID(OID_SYS_NAME)));
            pdu.add(new VariableBinding(new OID(OID_SYS_LOCATION)));
            pdu.setType(PDU.GET);

            // 4. Отправляем запрос синхронно и ждём ответ
            ResponseEvent event = snmp.send(pdu, target);

            // 5. Проверяем, что ответ пришёл
            if (event == null || event.getResponse() == null) {
                log.debug("No SNMP response from {}", ipAddress);
                return Optional.empty();
            }

            // 6. Разбираем ответ — собираем SnmpSystemInfo
            PDU response = event.getResponse();
            SnmpSystemInfo info = SnmpSystemInfo.builder()
                    .ipAddress(ipAddress)
                    .description(extractString(response, 0))
                    .objectId(extractString(response, 1))
                    .uptime(extractLong(response, 2))
                    .name(extractString(response, 3))
                    .location(extractString(response, 4))
                    .build();

            log.info("Discovered device at {}: name='{}', description='{}'",
                    ipAddress, info.getName(), info.getDescription());

            return Optional.of(info);

        } catch (IOException e) {
            log.warn("SNMP query failed for {}: {}", ipAddress, e.getMessage());
            return Optional.empty();
        } finally {
            // 7. Закрываем сокет, чтобы не плодить утечки
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ignored) { }
            }
        }
    }

    /**
     * Извлекает строковое значение из позиции index в ответе PDU.
     * Возвращает null, если значение отсутствует или ошибочное.
     */
    private String extractString(PDU response, int index) {
        if (index >= response.size()) return null;
        VariableBinding vb = response.get(index);
        if (vb == null || vb.getVariable() == null || vb.isException()) return null;
        return vb.getVariable().toString();
    }

    /**
     * Извлекает длинное целое значение из позиции index в ответе PDU.
     */
    private Long extractLong(PDU response, int index) {
        if (index >= response.size()) return null;
        VariableBinding vb = response.get(index);
        if (vb == null || vb.getVariable() == null || vb.isException()) return null;
        try {
            return vb.getVariable().toLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Опрашивает таблицу интерфейсов устройства через SNMP walk.
     * Возвращает список всех портов устройства с их характеристиками.
     *
     * @return список найденных интерфейсов; пустой список, если устройство не отвечает
     */
    public List<SnmpPortInfo> queryInterfaces(String ipAddress, int port, String community) {
        log.info("Querying interface table at {}:{}", ipAddress, port);
        List<SnmpPortInfo> result = new ArrayList<>();

        Snmp snmp = null;
        try {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(GenericAddress.parse("udp:" + ipAddress + "/" + port));
            target.setVersion(SnmpConstants.version2c);
            target.setTimeout(SNMP_TIMEOUT_MS);
            target.setRetries(SNMP_RETRIES);

            // Какие колонки таблицы нам нужны
            OID[] columns = {
                    new OID(OID_IF_DESCR),
                    new OID(OID_IF_TYPE),
                    new OID(OID_IF_SPEED),
                    new OID(OID_IF_PHYS_ADDRESS),
                    new OID(OID_IF_ADMIN_STATUS),
                    new OID(OID_IF_OPER_STATUS)
            };

            // TableUtils делает GETBULK-запросы и собирает таблицу
            TableUtils tableUtils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));
            List<TableEvent> events = tableUtils.getTable(target, columns, null, null);

            for (TableEvent event : events) {
                if (event == null || event.isError()) {
                    log.debug("Skipping erroneous row: {}",
                            event == null ? "null" : event.getErrorMessage());
                    continue;
                }

                VariableBinding[] columnValues = event.getColumns();
                if (columnValues == null) continue;

                // Индекс строки = ifIndex (последний компонент в OID любой ячейки)
                Integer ifIndex = event.getIndex().last();

                SnmpPortInfo portInfo = SnmpPortInfo.builder()
                        .ifIndex(ifIndex)
                        .description(extractStringFromVB(columnValues, 0))
                        .ifType(extractIntFromVB(columnValues, 1))
                        .speed(extractLongFromVB(columnValues, 2))
                        .macAddress(extractMacFromVB(columnValues, 3))
                        .adminStatus(extractIntFromVB(columnValues, 4))
                        .operStatus(extractIntFromVB(columnValues, 5))
                        .build();

                result.add(portInfo);
            }

            log.info("Got {} interfaces from {}", result.size(), ipAddress);
            return result;

        } catch (IOException e) {
            log.warn("Interface query failed for {}: {}", ipAddress, e.getMessage());
            return result;
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ignored) { }
            }
        }
    }

// Вспомогательные методы для работы с VariableBinding[] из TableEvent

    private String extractStringFromVB(VariableBinding[] columns, int index) {
        if (index >= columns.length || columns[index] == null) return null;
        if (columns[index].isException()) return null;
        return columns[index].getVariable().toString();
    }

    private Integer extractIntFromVB(VariableBinding[] columns, int index) {
        if (index >= columns.length || columns[index] == null) return null;
        if (columns[index].isException()) return null;
        try {
            return columns[index].getVariable().toInt();
        } catch (Exception e) {
            return null;
        }
    }

    private Long extractLongFromVB(VariableBinding[] columns, int index) {
        if (index >= columns.length || columns[index] == null) return null;
        if (columns[index].isException()) return null;
        try {
            return columns[index].getVariable().toLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * MAC-адрес в SNMP представлен как OctetString из 6 байт.
     * Преобразуем в стандартное представление с двоеточиями.
     */
    private String extractMacFromVB(VariableBinding[] columns, int index) {
        if (index >= columns.length || columns[index] == null) return null;
        if (columns[index].isException()) return null;
        org.snmp4j.smi.Variable var = columns[index].getVariable();
        if (!(var instanceof OctetString)) return null;
        byte[] bytes = ((OctetString) var).getValue();
        if (bytes == null || bytes.length != 6) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) sb.append(":");
            sb.append(String.format("%02x", bytes[i] & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Опрашивает таблицу IP-адресов устройства через SNMP walk.
     *
     * @return список IP-адресов с привязкой к интерфейсам
     */
    public List<SnmpIpInfo> queryIpAddresses(String ipAddress, int port, String community) {
        log.info("Querying IP address table at {}:{}", ipAddress, port);
        List<SnmpIpInfo> result = new ArrayList<>();

        Snmp snmp = null;
        try {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(GenericAddress.parse("udp:" + ipAddress + "/" + port));
            target.setVersion(SnmpConstants.version2c);
            target.setTimeout(SNMP_TIMEOUT_MS);
            target.setRetries(SNMP_RETRIES);

            OID[] columns = {
                    new OID(OID_IP_ENT_IF_INDEX),
                    new OID(OID_IP_ENT_NETMASK)
            };

            TableUtils tableUtils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));
            List<TableEvent> events = tableUtils.getTable(target, columns, null, null);

            for (TableEvent event : events) {
                if (event == null || event.isError()) {
                    log.debug("Skipping erroneous row: {}",
                            event == null ? "null" : event.getErrorMessage());
                    continue;
                }

                VariableBinding[] columnValues = event.getColumns();
                if (columnValues == null) continue;

                // Индекс строки = сам IP-адрес (4 компонента OID)
                String ipFromIndex = oidToIpAddress(event.getIndex());
                if (ipFromIndex == null) continue;

                SnmpIpInfo ipInfo = SnmpIpInfo.builder()
                        .ipAddress(ipFromIndex)
                        .ifIndex(extractIntFromVB(columnValues, 0))
                        .subnetMask(extractStringFromVB(columnValues, 1))
                        .build();

                result.add(ipInfo);
            }

            log.info("Got {} IP addresses from {}", result.size(), ipAddress);
            return result;

        } catch (IOException e) {
            log.warn("IP address query failed for {}: {}", ipAddress, e.getMessage());
            return result;
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ignored) { }
            }
        }
    }

    /**
     * Преобразует OID из четырёх компонентов в IP-адрес.
     * Пример: OID(192, 168, 1, 10) -> "192.168.1.10"
     */
    private String oidToIpAddress(OID indexOid) {
        if (indexOid == null || indexOid.size() != 4) return null;
        return indexOid.get(0) + "." + indexOid.get(1) + "." +
                indexOid.get(2) + "." + indexOid.get(3);
    }

    /**
     * Выполняет полный SNMP-опрос устройства:
     * системную информацию, список интерфейсов, список IP-адресов.
     *
     * @return Optional с полным портретом устройства,
     *         пустой Optional если устройство не отвечает на базовый SNMP-запрос
     */
    public Optional<SnmpDeviceInfo> queryFullDevice(String ipAddress, int port, String community) {
        log.info("Starting full SNMP query for {}:{}", ipAddress, port);

        // 1. Сначала — системная информация. Если её нет, дальше не идём.
        Optional<SnmpSystemInfo> systemOpt = querySystem(ipAddress, port, community);
        if (systemOpt.isEmpty()) {
            log.debug("Device {} does not respond to SNMP, skipping table queries", ipAddress);
            return Optional.empty();
        }

        // 2. Параллельно можно было бы опрашивать интерфейсы и IP,
        //    но для одного устройства это излишне — последовательно проще.
        List<SnmpPortInfo> ports = queryInterfaces(ipAddress, port, community);
        List<SnmpIpInfo> ips = queryIpAddresses(ipAddress, port, community);

        SnmpDeviceInfo deviceInfo = SnmpDeviceInfo.builder()
                .system(systemOpt.get())
                .ports(ports)
                .ipAddresses(ips)
                .build();

        log.info("Full SNMP query complete for {}: ports={}, ips={}",
                ipAddress, ports.size(), ips.size());

        return Optional.of(deviceInfo);
    }
}