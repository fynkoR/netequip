package ru.ssau.netequip.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ssau.netequip.dto.user.UserDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.User;
import ru.ssau.netequip.enums.UserRole;
import ru.ssau.netequip.exception.employee.NotFoundEmployeeException;
import ru.ssau.netequip.repository.EmployeeRepository;
import ru.ssau.netequip.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User createUser(Long id, String username, String password, UserRole role, Employee employee) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setEmployee(employee);
        return user;
    }

    private Employee createEmployee(Long id, String fullName) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFullName(fullName);
        return employee;
    }

    private UserDto createUserDto(String username, String password, Long employeeId) {
        UserDto dto = new UserDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setEmployeeId(employeeId);
        return dto;
    }

    @Test
    void testLoadUserByUsername_Success() {
        String username = "testuser";
        String password = "encodedPassword";
        User user = createUser(1L, username, password, UserRole.VIEWER, null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER")));
    }

    @Test
    void testLoadUserByUsername_AdminRole() {
        String username = "admin";
        String password = "encodedPassword";
        User user = createUser(1L, username, password, UserRole.ADMIN, null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound_ShouldThrowException() {
        String username = "nonexistent";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username));
    }

    @Test
    void testRegister_Success_UserRole() {
        String username = "regularuser";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        UserDto dto = createUserDto(username, rawPassword, null);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        User savedUser = createUser(100L, username, encodedPassword, UserRole.VIEWER, null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = customUserDetailsService.register(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(username, result.getUsername());
        assertNull(result.getEmployeeId());
        assertNull(result.getPassword()); // пароль не возвращается

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_Success_AdminRole() {
        String username = "admin";
        String rawPassword = "admin123";
        UserDto dto = createUserDto(username, rawPassword, null);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn("encodedAdminPass");

        User savedUser = createUser(100L, username, "encodedAdminPass", UserRole.ADMIN, null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = customUserDetailsService.register(dto);

        assertEquals(UserRole.ADMIN.name(), savedUser.getRole().name());
    }

    @Test
    void testRegister_WithEmployee_Success() {
        String username = "userwithemployee";
        Long employeeId = 50L;
        UserDto dto = createUserDto(username, "pass123", employeeId);

        Employee employee = createEmployee(employeeId, "Иванов Иван");

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        User savedUser = createUser(100L, username, "encoded", UserRole.VIEWER, employee);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = customUserDetailsService.register(dto);

        assertEquals(employeeId, result.getEmployeeId());
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void testRegister_DuplicateUsername_ShouldThrowException() {
        String username = "existinguser";
        UserDto dto = createUserDto(username, "pass123", null);

        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> customUserDetailsService.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_EmployeeNotFound_ShouldThrowException() {
        Long employeeId = 999L;
        UserDto dto = createUserDto("newuser", "pass123", employeeId);

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmployeeException.class, () -> customUserDetailsService.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testFindByUsername_Success() {
        String username = "testuser";
        User expectedUser = createUser(1L, username, "pass", UserRole.VIEWER, null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        User result = customUserDetailsService.findByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void testFindByUsername_NotFound_ShouldThrowException() {
        String username = "nonexistent";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.findByUsername(username));
    }

    @Test
    void testFindById_Success() {
        Long userId = 100L;
        User expectedUser = createUser(userId, "testuser", "pass", UserRole.VIEWER, null);

        when(userRepository.findByIdWithEmployee(userId)).thenReturn(Optional.of(expectedUser));

        User result = customUserDetailsService.findById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void testFindById_NotFound_ShouldThrowException() {
        Long userId = 999L;

        when(userRepository.findByIdWithEmployee(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.findById(userId));
    }

    @Test
    void testGetAllUsers_Success() {
        Employee employee = createEmployee(10L, "Тестов Тест");
        User user1 = createUser(1L, "user1", "pass1", UserRole.VIEWER, null);
        User user2 = createUser(2L, "user2", "pass2", UserRole.ADMIN, employee);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> result = customUserDetailsService.getAllUsers();

        assertEquals(2, result.size());
        assertNull(result.get(0).getEmployeeId());
        assertEquals(10L, result.get(1).getEmployeeId());
        assertNull(result.get(0).getPassword()); // пароль не возвращается
    }

    @Test
    void testGetAllUsers_Empty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = customUserDetailsService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateUser_Success_AddEmployee() {
        Long userId = 100L;
        Long employeeId = 50L;
        UserDto dto = new UserDto();
        dto.setEmployeeId(employeeId);

        User existingUser = createUser(userId, "testuser", "pass", UserRole.VIEWER, null);
        Employee employee = createEmployee(employeeId, "Новый сотрудник");
        User updatedUser = createUser(userId, "testuser", "pass", UserRole.VIEWER, employee);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);

        UserDto result = customUserDetailsService.updateUser(userId, dto);

        assertEquals(employeeId, result.getEmployeeId());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testUpdateUser_Success_RemoveEmployee() {
        Long userId = 100L;
        Employee employee = createEmployee(50L, "Старый сотрудник");
        UserDto dto = new UserDto();
        dto.setEmployeeId(null); // убираем привязку

        User existingUser = createUser(userId, "testuser", "pass", UserRole.VIEWER, employee);
        User updatedUser = createUser(userId, "testuser", "pass", UserRole.VIEWER, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);

        UserDto result = customUserDetailsService.updateUser(userId, dto);

        assertNull(result.getEmployeeId());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testUpdateUser_UserNotFound_ShouldThrowException() {
        Long userId = 999L;
        UserDto dto = new UserDto();
        dto.setEmployeeId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.updateUser(userId, dto));
    }

    @Test
    void testUpdateUser_EmployeeNotFound_ShouldThrowException() {
        Long userId = 100L;
        Long employeeId = 999L;
        UserDto dto = new UserDto();
        dto.setEmployeeId(employeeId);

        User existingUser = createUser(userId, "testuser", "pass", UserRole.VIEWER, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmployeeException.class,
                () -> customUserDetailsService.updateUser(userId, dto));
        verify(userRepository, never()).save(any());
    }
}