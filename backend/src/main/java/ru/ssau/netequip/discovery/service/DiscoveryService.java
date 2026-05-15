package ru.ssau.netequip.discovery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ssau.netequip.discovery.dto.ScanRequestDto;
import ru.ssau.netequip.discovery.dto.ScanResultDto;
import ru.ssau.netequip.discovery.dto.SnmpDeviceInfo;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import ru.ssau.netequip.discovery.dto.DiscoveredDeviceDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscoveryService {

    private final SnmpQueryService snmpQueryService;
    private final TypeResolverService typeResolverService;

    /**
     * Размер пула параллельных потоков для сканирования.
     * Можно изменить через application.properties параметром
     * netequip.discovery.thread-pool-size.
     */
    @Value("${netequip.discovery.thread-pool-size:50}")
    private int threadPoolSize;

    /**
     * Стандартный SNMP-порт по умолчанию (если в запросе не указан).
     */
    private static final int DEFAULT_SNMP_PORT = 161;

    /**
     * Стандартное community по умолчанию.
     */
    private static final String DEFAULT_COMMUNITY = "public";

    /**
     * Плейсхолдер в community, заменяемый на IP-адрес.
     */
    private static final String IP_PLACEHOLDER = "{ip}";

    /**
     * Выполняет сканирование подсети через SNMP.
     *
     * @param request параметры сканирования
     * @return результат с найденными устройствами
     */
    public ScanResultDto scan(ScanRequestDto request) {
        long startTime = System.currentTimeMillis();

        // 1. Разбираем CIDR в список IP
        List<String> targetIps = CidrUtils.expand(request.getCidr());
        log.info("Starting scan of {} ({} addresses), thread pool size: {}",
                request.getCidr(), targetIps.size(), threadPoolSize);

        // 2. Нормализуем параметры (применяем дефолты)
        int port = (request.getPort() != null) ? request.getPort() : DEFAULT_SNMP_PORT;
        String communityTemplate = (request.getCommunity() != null && !request.getCommunity().isBlank())
                ? request.getCommunity()
                : DEFAULT_COMMUNITY;

        // 3. Запускаем параллельное сканирование через executor service
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        try {
            List<CompletableFuture<DiscoveredDeviceDto>> futures = new ArrayList<>();

            for (String ip : targetIps) {
                final String communityForIp = resolveCommunity(communityTemplate, ip);
                CompletableFuture<DiscoveredDeviceDto> future = CompletableFuture.supplyAsync(
                        () -> scanOne(ip, port, communityForIp),
                        executor
                );
                futures.add(future);
            }

            // 4. Ждём результаты всех задач
            List<DiscoveredDeviceDto> discovered = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Scan of {} complete: {} devices found in {} ms",
                    request.getCidr(), discovered.size(), duration);

            return ScanResultDto.builder()
                    .cidr(request.getCidr())
                    .totalAddressesScanned(targetIps.size())
                    .durationMs(duration)
                    .discoveredDevices(discovered)
                    .build();

        } finally {
            // 5. Корректно завершаем executor
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Опрашивает один IP. Возвращает null, если устройство не отвечает.
     */
    private DiscoveredDeviceDto scanOne(String ip, int port, String community) {
        try {
            Optional<SnmpDeviceInfo> deviceOpt = snmpQueryService.queryFullDevice(ip, port, community);
            if (deviceOpt.isEmpty()) {
                return null;
            }

            SnmpDeviceInfo deviceInfo = deviceOpt.get();
            TypeResolverService.ResolverResult typeResult =
                    typeResolverService.resolve(deviceInfo.getSystem());

            return DiscoveredDeviceDto.builder()
                    .snmpData(deviceInfo)
                    .suggestedTypeId(typeResult.type() != null ? typeResult.type().getId() : null)
                    .suggestedTypeName(typeResult.type() != null ? typeResult.type().getTypeName() : null)
                    .matchConfidence(typeResult.confidence())
                    .build();

        } catch (Exception e) {
            log.warn("Unexpected error during SNMP query to {}: {}", ip, e.getMessage());
            return null;
        }
    }

    /**
     * Применяет плейсхолдер {ip} к шаблону community.
     */
    private String resolveCommunity(String template, String ip) {
        if (template == null) return DEFAULT_COMMUNITY;
        return template.replace(IP_PLACEHOLDER, ip);
    }
}