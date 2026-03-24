package com.company.mathapp_backend_03.entity;

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
@Table(name = "match_card_result")
public class MatchCardResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    Integer totalPairs;
    Integer correctPairs;
    Integer timeTaken;
    Integer totalXP;

    @ManyToOne
    @JoinColumn(name = "match_card_id")
    MatchCard matchCard;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

}
