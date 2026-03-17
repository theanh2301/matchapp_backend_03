package com.company.mathapp_backend_03.entity;

import com.company.mathapp_backend_03.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String fullName;
    LocalDate dob;
    String status;
    String email;
    Integer phone;
    String password;
    String avatarUrl;
    Boolean isPremium;
    Role role;
    LocalDate createdAt;
    LocalDate updatedAt;
}
