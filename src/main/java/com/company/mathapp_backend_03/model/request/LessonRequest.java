package com.company.mathapp_backend_03.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LessonRequest {
    String lessonName;
    String description;
    Integer chapterId;

}
