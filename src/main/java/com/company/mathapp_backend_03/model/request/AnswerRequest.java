package com.company.mathapp_backend_03.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
    private Integer id;
    private String content;
    private Boolean isCorrect;

}
