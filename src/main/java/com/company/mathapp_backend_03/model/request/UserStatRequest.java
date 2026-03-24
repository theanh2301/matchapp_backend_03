package com.company.mathapp_backend_03.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserStatRequest {

    @NotNull(message = "totalXp cannot be null")
    Integer totalXP;
    @NotNull(message = "totalLesson cannot be null")
    Integer totalLesson;
    @NotNull(message = "streak cannot be null")
    Integer streakDay;
    @NotNull(message = "lastDaysStudy cannot be null")
    @Past(message = "lastDaysStudy cannot be in future")
    LocalDateTime lastDayStudy;
}
