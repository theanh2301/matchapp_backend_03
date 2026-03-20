package com.company.mathapp_backend_03.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubjectRequest {
    @NotBlank(message = "subjectName cannot be empty")
    String subjectName;
    @NotNull(message = "subjectClass cannot be empty")
    Integer subjectClass;
    String icon;

}
