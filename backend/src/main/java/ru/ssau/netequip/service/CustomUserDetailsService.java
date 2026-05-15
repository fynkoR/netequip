package ru.ssau.netequip.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.netequip.dto.user.UserDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.User;
import ru.ssau.netequip.enums.Role;
import ru.ssau.netequip.exception.employee.NotFoundEmployeeException;
import ru.ssau.netequip.repository.EmployeeRepository;
import ru.ssau.netequip.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь не найден: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
    }

    /**
     * Регистрация пользователя.
     * Если username == "admin", назначается роль ADMIN, иначе — USER.
     * Если передан employeeId — привязываем User к существующему Employee.
     */
    @Transactional
    public UserDto register(UserDto dto) {
        log.info("Регистрация пользователя: {}", dto.getUsername());

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует: " + dto.getUsername());
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Назначение роли
        if ("admin".equalsIgnoreCase(dto.getUsername())) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

        // Привязка к сотруднику, если указан employeeId
        if (dto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> {
                        log.warn("Сотрудник не найден с id: {}", dto.getEmployeeId());
                        return new NotFoundEmployeeException(dto.getEmployeeId());
                    });
            user.setEmployee(employee);
        }

        User saved = userRepository.save(user);
        log.info("Пользователь зарегистрирован с id: {}, роль: {}, employeeId: {}",
                saved.getId(), saved.getRole(),
                saved.getEmployee() != null ? saved.getEmployee().getId() : "нет");

        UserDto response = new UserDto();
        response.setId(saved.getId());
        response.setUsername(saved.getUsername());
        response.setEmployeeId(saved.getEmployee() != null ? saved.getEmployee().getId() : null);
        // пароль не возвращаем
        return response;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь не найден: " + username));
    }

    public User findById(Long id) {
        return userRepository.findByIdWithEmployee(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь не найден с id: " + id));
    }

    public List<UserDto> getAllUsers(){
        return userRepository.findAll().stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setEmployeeId(user.getEmployee() != null ? user.getEmployee().getId() : null);
                    return dto;
                }).collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto dto){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден с таким id: " + id));
        if(dto.getEmployeeId() != null){
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new NotFoundEmployeeException(dto.getEmployeeId()));
            user.setEmployee(employee);
        }else{
            user.setEmployee(null);
        }
        User saved = userRepository.save(user);

        UserDto response = new UserDto();
        response.setId(saved.getId());
        response.setUsername(saved.getUsername());
        response.setEmployeeId(saved.getEmployee() != null ? saved.getEmployee().getId() : null);
        return response;
    }
}
