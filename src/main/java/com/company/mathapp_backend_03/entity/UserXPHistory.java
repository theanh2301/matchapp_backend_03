package com.company.mathapp_backend_03.entity;

import com.company.mathapp_backend_03.model.enums.Source;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "xp_history")
public class UserXPHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    Integer xp;
    Source source;
    Integer sourcedId;
    LocalDate EarnedAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    User user;
}
