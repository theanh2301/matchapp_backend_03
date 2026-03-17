package com.company.mathapp_backend_03.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "lesson_completion")
public class LessonCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String status;
    Float Accuracy;
    LocalDate LastAccessed;
    Integer TimeSpent;
    LocalDate CompletedAt;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;


}
