package com.company.mathapp_backend_03.model.response;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashcardProgressResponse {
    Integer id;
    Boolean isKnown;
    LocalDate lastReviewed;
    Integer totalXP;
}
