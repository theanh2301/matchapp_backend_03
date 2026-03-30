package com.company.mathapp_backend_03.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_practice")
public class UserPractice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    Integer userId;

    @ManyToOne
    @JoinColumn(name = "practice_id")
    Practice practice;

    Boolean isCompleted;
    Integer totalXp;
}
