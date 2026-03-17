package com.company.mathapp_backend_03.model.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubjectRequest {
    String subjectName;
    Integer subjectClass;
    String icon;

}
