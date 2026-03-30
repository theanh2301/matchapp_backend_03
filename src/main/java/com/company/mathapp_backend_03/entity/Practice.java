package com.company.mathapp_backend_03.entity;

import com.company.mathapp_backend_03.model.enums.PracticeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "practice")
public class Practice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String title;
    String description;
    Integer timeLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "practice_type")
    PracticeType practiceType;

}
