package com.company.mathapp_backend_03.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchCardBatchRequest {

    @NotNull(message = "userId cannot be null")
    private Integer userId;

    @NotNull
    @Size(min = 1, message = "Danh sách không được rỗng")
    private List<MatchCardResultRequest> results;

}