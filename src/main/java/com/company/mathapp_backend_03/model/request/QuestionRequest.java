package com.company.mathapp_backend_03.model.request;

import com.company.mathapp_backend_03.model.enums.TypeQuestion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    String content;
    TypeQuestion typeQuestion;
    Integer xpReward;
    Integer LessonId;
}
