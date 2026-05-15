package ru.ssau.netequip.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.netequip.dto.user.UserDto;
import ru.ssau.netequip.dto.user.UserInfoDto;
import ru.ssau.netequip.service.CustomUserDetailsService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserDto dto) {
        log.info("Регистрация пользователя: {}", dto.getUsername());
        UserDto registered = userDetailsService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(){
        List<UserDto> users = userDetailsService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto dto){
        UserDto upd = userDetailsService.updateUser(id,dto);
        return ResponseEntity.ok(upd);
    }
}
