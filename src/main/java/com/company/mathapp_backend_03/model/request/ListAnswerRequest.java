package com.company.mathapp_backend_03.model.request;

import com.company.mathapp_backend_03.model.response.AnswerResponse;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListAnswerRequest {
    @NotNull(message = "answer cannot be null")
    private List<AnswerResponse> answers;
    @NotNull(message = "questionId cannot be null")
    private Integer questionId;
}



