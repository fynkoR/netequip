package ru.ssau.netequip.discovery.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилита для разбора CIDR-нотации сетевых подсетей.
 * Преобразует строки вида "192.168.1.0/24" в список конкретных IP-адресов.
 */
public final class CidrUtils {

    private static final Pattern CIDR_PATTERN = Pattern.compile(
            "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})/(\\d{1,2})$"
    );

    private CidrUtils() {
        // утилитный класс, экземпляры не создаются
    }

    /**
     * Разворачивает CIDR-подсеть в список всех IP-адресов диапазона.
     * Включает сетевой и broadcast-адреса.
     *
     * @param cidr строка в формате "X.X.X.X/Y"
     * @return список IP-адресов
     * @throws IllegalArgumentException если формат некорректный
     */
    public static List<String> expand(String cidr) {
        if (cidr == null || cidr.isBlank()) {
            throw new IllegalArgumentException("CIDR is null or blank");
        }

        Matcher matcher = CIDR_PATTERN.matcher(cidr.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid CIDR format: " + cidr);
        }

        int a = Integer.parseInt(matcher.group(1));
        int b = Integer.parseInt(matcher.group(2));
        int c = Integer.parseInt(matcher.group(3));
        int d = Integer.parseInt(matcher.group(4));
        int prefix = Integer.parseInt(matcher.group(5));

        // Валидация октетов
        if (a > 255 || b > 255 || c > 255 || d > 255) {
            throw new IllegalArgumentException("Invalid IP octet in: " + cidr);
        }
        if (prefix < 0 || prefix > 32) {
            throw new IllegalArgumentException("Invalid prefix length: " + prefix);
        }

        // Защита: не позволяем сканировать гигантские подсети
        if (prefix < 16) {
            throw new IllegalArgumentException(
                    "Subnet too large (prefix " + prefix + "). " +
                            "Use prefix >= 16 (max 65536 addresses)."
            );
        }

        // Преобразуем базовый IP в 32-битное целое
        long baseIp = ((long) a << 24) | ((long) b << 16) | ((long) c << 8) | d;

        // Маска: единицы в старших prefix битах
        long mask = (prefix == 0) ? 0L : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;

        // Начало и конец диапазона
        long network   = baseIp & mask;
        long broadcast = network | (~mask & 0xFFFFFFFFL);

        List<String> result = new ArrayList<>();
        for (long ip = network; ip <= broadcast; ip++) {
            result.add(longToIp(ip));
        }
        return result;
    }

    /**
     * Преобразует 32-битное число в строковое представление IP.
     */
    private static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                (ip & 0xFF);
    }
}