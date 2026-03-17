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
@Table(name = "match_game_result")
public class MatchGameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    Integer totalPairs;
    Integer correctPairs;
    Integer timeTaken;
    Integer totalXP;

    @ManyToOne
    @JoinColumn(name = "match_game_id")
    MatchGame matchGame;

    @ManyToOne
    @JoinColumn(name = "user _id")
    User user;

}
