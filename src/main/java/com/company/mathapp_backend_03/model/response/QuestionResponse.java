package com.company.mathapp_backend_03.model.response;

import com.company.mathapp_backend_03.model.enums.TypeQuestion;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    Integer id;
    String content;
    TypeQuestion typeQuestion;
    Integer xpReward;
    List<AnswerResponse> answers;
}
