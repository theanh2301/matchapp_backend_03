package com.company.mathapp_backend_03.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchCardResultRequest {
    @NotNull(message = "totalPairs cannot be null")
    Integer totalPairs;
    @NotNull(message = "correctPairs cannot be null")
    Integer correctPairs;
    @NotNull(message = "timeTaken cannot be null")
    Integer timeTaken;
    @NotNull(message = "totalXP cannot be null")
    Integer totalXP;
    @NotNull(message = "matchCardId cannot be null")
    Integer matchCardId;
    @NotNull(message = "userId cannot be null")
    Integer userId;
}
