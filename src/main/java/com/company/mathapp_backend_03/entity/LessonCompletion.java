package com.company.mathapp_backend_03.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lesson_completion")
public class LessonCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    private Integer totalXp;

    private Boolean isFlashcardCompleted;
    private Boolean isMatchCardCompleted;
    private Boolean isQuizCompleted;

    private Boolean isCompleted;

    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;

}
