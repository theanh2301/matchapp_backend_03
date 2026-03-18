package com.company.mathapp_backend_03.model.response;

import com.company.mathapp_backend_03.model.enums.TypeQuestion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    Integer id;
    String content;
    TypeQuestion typeQuestion;
    Integer xpReward;

}
