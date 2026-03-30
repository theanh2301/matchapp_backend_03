package com.company.mathapp_backend_03.model.response;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizQuestionResponse {
    Integer id;
    String content;
    Integer xpReward;
    List<QuizAnswerResponse> answers;
}
