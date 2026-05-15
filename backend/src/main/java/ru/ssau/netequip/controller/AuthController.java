package ru.ssau.netequip.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.ssau.netequip.dto.user.AuthResponseDto;
import ru.ssau.netequip.dto.user.LoginDto;
import ru.ssau.netequip.dto.user.RefreshRequestDto;
import ru.ssau.netequip.dto.user.UserInfoDto;
import ru.ssau.netequip.entity.User;
import ru.ssau.netequip.service.CustomUserDetailsService;
import ru.ssau.netequip.service.TokenService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final TokenService tokenService;

    /**
     * POST /auth/login — аутентификация и выдача access + refresh токенов.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        log.info("Попытка входа: {}", dto.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getUsername(), dto.getPassword())
            );

            User user = userDetailsService.findByUsername(dto.getUsername());

            long now = System.currentTimeMillis() / 1000;

            Map<String, Object> accessPayload = new LinkedHashMap<>();
            accessPayload.put("userId", user.getId());
            accessPayload.put("roles", List.of(user.getRole().name()));
            accessPayload.put("iat", now);
            accessPayload.put("exp", now + 30 * 60); // 30 минут

            String accessToken = tokenService.generateToken(accessPayload);

            // Refresh Token — 7 дней
            Map<String, Object> refreshPayload = new LinkedHashMap<>();
            refreshPayload.put("userId", user.getId());
            refreshPayload.put("iat", now);
            refreshPayload.put("exp", now + 7 * 24 * 60 * 60); // 7 дней

            String refreshToken = tokenService.generateToken(refreshPayload);

            log.info("Пользователь {} успешно авторизован", dto.getUsername());

            return ResponseEntity.ok(new AuthResponseDto(accessToken, refreshToken));

        } catch (AuthenticationException e) {
            log.warn("Неудачная попытка входа: {}", dto.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Неверный логин или пароль");
        }
    }

    /**
     * POST /auth/refresh — обновление access token по refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequestDto dto) {
        log.info("Запрос на обновление токена");

        Map<String, Object> payload = tokenService.validateToken(dto.getRefreshToken());

        if (payload == null) {
            log.warn("Невалидный или истёкший refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Невалидный или истёкший refresh token");
        }

        Long userId = ((Number) payload.get("userId")).longValue();
        User user = userDetailsService.findById(userId);

        long now = System.currentTimeMillis() / 1000;

        // Создание нового Access Token
        Map<String, Object> accessPayload = new LinkedHashMap<>();
        accessPayload.put("userId", user.getId());
        accessPayload.put("roles", List.of(user.getRole().name()));
        accessPayload.put("iat", now);
        accessPayload.put("exp", now + 30 * 60);

        String newAccessToken = tokenService.generateToken(accessPayload);

        log.info("Access token обновлён для пользователя с id: {}", userId);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    /**
     * GET /auth/me — возвращает логин и роли текущего пользователя.
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long userId = (Long) authentication.getPrincipal();
        User user = userDetailsService.findById(userId);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .collect(Collectors.toList());

        UserInfoDto info = new UserInfoDto();
        info.setUsername(user.getUsername());
        info.setRoles(roles);
        if (user.getEmployee() != null) {
            info.setEmployeeId(user.getEmployee().getId());
            info.setFullName(user.getEmployee().getFullName());
        }

        return ResponseEntity.ok(info);
    }
}
