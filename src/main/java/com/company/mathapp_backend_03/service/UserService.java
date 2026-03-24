package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.model.enums.Role;
import com.company.mathapp_backend_03.entity.User;
import com.company.mathapp_backend_03.entity.UserStat;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.model.request.LoginRequest;
import com.company.mathapp_backend_03.model.request.RegisterRequest;
import com.company.mathapp_backend_03.model.response.UserResponse;
import com.company.mathapp_backend_03.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public void register(RegisterRequest registerRequest) {
        Optional<User> existingUser = userRepository.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new BadRequestException("Email already in use");
        }

        if (!Objects.equals(
                registerRequest.getPassword(),
                registerRequest.getConfirmPassword()
        )) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = User.builder()
                .email(registerRequest.getEmail())
                .fullName(registerRequest.getFullName())
                .dob(registerRequest.getDob())
                .phone(registerRequest.getPhone())
                .role(Role.USER)
                .password(bCryptPasswordEncoder.encode(registerRequest.getPassword()))
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isPremium(false)
                .build();

        UserStat stat = UserStat.builder()
                .totalXP(0)
                .totalLesson(0)
                .streakDay(0)
                .lastStudyDate(LocalDateTime.now())
                .build();

        stat.setUser(user);

        userRepository.save(user);
    }

    public UserResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("Email not found"));

        if (!bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password is incorrect");
        }

        UserResponse response = new UserResponse();
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setDob(user.getDob());
        response.setIsPremium(user.getIsPremium());
        response.setRole(user.getRole());

        return response;
    }
}
