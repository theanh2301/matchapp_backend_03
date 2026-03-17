package com.company.mathapp_backend_03.entity;

import com.company.mathapp_backend_03.model.enums.TypeQuestion;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String context;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_question")
    TypeQuestion typeQuestion;
    Integer xpReward;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    Lesson lesson;

}
