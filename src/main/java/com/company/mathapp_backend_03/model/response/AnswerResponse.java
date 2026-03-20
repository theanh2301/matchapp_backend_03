package com.company.mathapp_backend_03.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponse {
    Integer id;
    String content;
    Boolean isCorrect;
}
