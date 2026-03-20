package com.company.mathapp_backend_03.model.response;

import com.company.mathapp_backend_03.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    String fullName;
    LocalDate dob;
    String status;
    String email;
    String phone;
    String avatarUrl;
    Boolean isPremium;
    Role role;
}
