package com.company.mathapp_backend_03.model.request;

import com.company.mathapp_backend_03.model.response.AnswerResponse;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListAnswerRequest {
    private List<AnswerResponse> answers;
    private Integer questionId;
}



