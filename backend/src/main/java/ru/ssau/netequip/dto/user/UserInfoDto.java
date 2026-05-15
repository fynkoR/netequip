package ru.ssau.netequip.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {
    private String username;
    private List<String> roles;
    private Long employeeId;
    private String fullName;
}
