package com.company.mathapp_backend_03.model.request;

import com.company.mathapp_backend_03.model.enums.TypeQuestion;
import com.company.mathapp_backend_03.model.response.AnswerResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionRequest {
    @NotBlank(message = "content cannot be empty")
    String content;
    TypeQuestion typeQuestion;
    @NotNull(message = "xpReward cannot be null")
    Integer xpReward;
    @NotNull(message = "lessonId cannot be null")
    Integer lessonId;

    @NotNull(message = "answer cannot be null")
    List<AnswerRequest> answers;
}
