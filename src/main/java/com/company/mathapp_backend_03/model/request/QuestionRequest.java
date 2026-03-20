package com.company.mathapp_backend_03.model.request;

import com.company.mathapp_backend_03.model.enums.TypeQuestion;
import com.company.mathapp_backend_03.model.response.AnswerResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionRequest {

    String content;
    TypeQuestion typeQuestion;
    Integer xpReward;
    Integer lessonId;

    List<AnswerRequest> answers;
}
