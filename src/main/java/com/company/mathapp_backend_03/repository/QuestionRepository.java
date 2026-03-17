package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    boolean existsByLessonId(Integer id);
}
