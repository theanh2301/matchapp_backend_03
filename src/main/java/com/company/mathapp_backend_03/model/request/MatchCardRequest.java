package com.company.mathapp_backend_03.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchCardRequest {
    @NotNull(message = "pairId cannot be null")
    Integer pairId;
    @NotBlank(message = "content cannot be empty")
    String content;
    @NotNull(message = "xpReward cannot be null")
    Integer xpReward;
    @NotNull(message = "lessonId cannot be null")
    Integer lessonId;
}
