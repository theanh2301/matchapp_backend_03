package com.company.mathapp_backend_03.model.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PracticeAnswerResponse {
    Integer id;
    String content;
    Boolean isCorrect;
    String description;
}
