package com.company.mathapp_backend_03.model.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    String email;
    String fullName;
    Integer phone;
    LocalDate dob;
    String password;
    String confirmPassword;
}