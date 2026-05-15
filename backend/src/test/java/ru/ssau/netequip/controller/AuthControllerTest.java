package ru.ssau.netequip.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;
import ru.ssau.netequip.dto.user.AuthResponseDto;
import ru.ssau.netequip.dto.user.LoginDto;
import ru.ssau.netequip.dto.user.RefreshRequestDto;
import ru.ssau.netequip.dto.user.UserInfoDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.User;
import ru.ssau.netequip.enums.Position;
import ru.ssau.netequip.enums.Role;
import ru.ssau.netequip.repository.EmployeeRepository;
import ru.ssau.netequip.repository.UserRepository;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RestClient restClient;
    private String baseUrl;
    private String testUsername;
    private String testPassword;

    @BeforeEach
    void setUp() {
        userRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();

        baseUrl = "http://localhost:" + port;
        restClient = RestClient.builder().build();

        testUsername = "testuser" + System.currentTimeMillis();
        testPassword = "password123";

        Employee employee = new Employee();
        employee.setFullName("Тестовый Сотрудник");
        employee.setPosition(Position.ENGINEER);
        employee.setEmail("test" + System.currentTimeMillis() + "@ssau.ru");
        employeeRepository.save(employee);

        User user = new User();
        user.setUsername(testUsername);
        user.setPassword(passwordEncoder.encode(testPassword));
        user.setRole(Role.USER);
        user.setEmployee(employee);
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
    }

    @Test
    void testLogin_Success() {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(testUsername);
        loginDto.setPassword(testPassword);

        ResponseEntity<AuthResponseDto> response = restClient.post()
                .uri(baseUrl + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginDto)
                .retrieve()
                .toEntity(AuthResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());
    }

    @Test
    void testLogin_WrongPassword_ShouldReturn401() {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(testUsername);
        loginDto.setPassword("wrongpassword");

        ResponseEntity<String> response = restClient.post()
                .uri(baseUrl + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginDto)
                .retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED, (request, response1) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLogin_UserNotFound_ShouldReturn401() {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("nonexistentuser");
        loginDto.setPassword("password");

        ResponseEntity<String> response = restClient.post()
                .uri(baseUrl + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginDto)
                .retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED, (request, response1) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testRefresh_Success() {
        // Сначала логинимся
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(testUsername);
        loginDto.setPassword(testPassword);

        ResponseEntity<AuthResponseDto> loginResponse = restClient.post()
                .uri(baseUrl + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginDto)
                .retrieve()
                .toEntity(AuthResponseDto.class);

        String refreshToken = loginResponse.getBody().getRefreshToken();

        // Обновляем токен
        RefreshRequestDto refreshDto = new RefreshRequestDto();
        refreshDto.setRefreshToken(refreshToken);

        ResponseEntity<Map> refreshResponse = restClient.post()
                .uri(baseUrl + "/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .body(refreshDto)
                .retrieve()
                .toEntity(Map.class);

        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        assertNotNull(refreshResponse.getBody());
        assertNotNull(refreshResponse.getBody().get("accessToken"));
    }

    @Test
    void testRefresh_InvalidToken_ShouldReturn401() {
        RefreshRequestDto refreshDto = new RefreshRequestDto();
        refreshDto.setRefreshToken("invalid.token.string");

        ResponseEntity<String> response = restClient.post()
                .uri(baseUrl + "/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .body(refreshDto)
                .retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED, (request, response1) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetMe_Success() {
        // Сначала логинимся
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(testUsername);
        loginDto.setPassword(testPassword);

        ResponseEntity<AuthResponseDto> loginResponse = restClient.post()
                .uri(baseUrl + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginDto)
                .retrieve()
                .toEntity(AuthResponseDto.class);

        String accessToken = loginResponse.getBody().getAccessToken();

        // Запрашиваем информацию о себе
        ResponseEntity<UserInfoDto> meResponse = restClient.get()
                .uri(baseUrl + "/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toEntity(UserInfoDto.class);

        assertEquals(HttpStatus.OK, meResponse.getStatusCode());
        assertNotNull(meResponse.getBody());
        assertEquals(testUsername, meResponse.getBody().getUsername());
        assertNotNull(meResponse.getBody().getRoles());
    }

    @Test
    void testGetMe_WithoutToken_ShouldReturn401() {
        ResponseEntity<String> response = restClient.get()
                .uri(baseUrl + "/auth/me")
                .retrieve()
                .onStatus(status -> status == HttpStatus.FORBIDDEN || status == HttpStatus.UNAUTHORIZED,
                        (request, response1) -> {})
                .toEntity(String.class);

        // Принимаем и 401, и 403 (зависит от конфигурации Spring Security)
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                response.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    void testGetMe_WithInvalidToken_ShouldReturn401() {
        ResponseEntity<String> response = restClient.get()
                .uri(baseUrl + "/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.string")
                .retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED, (request, response1) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}