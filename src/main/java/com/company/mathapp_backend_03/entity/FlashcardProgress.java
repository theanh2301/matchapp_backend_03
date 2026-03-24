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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "flashcard_progress")
public class FlashcardProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    Boolean isKnown;
    LocalDateTime lastReviewed;
    Integer totalXP;

    @ManyToOne
    @JoinColumn(name = "flashcard_id")
    Flashcard flashcard;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
