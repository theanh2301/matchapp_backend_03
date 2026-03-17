package com.company.mathapp_backend_03.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubjectResponse {

    Integer id;
    String subjectName;
    Integer subjectClass;
    String icon;

}