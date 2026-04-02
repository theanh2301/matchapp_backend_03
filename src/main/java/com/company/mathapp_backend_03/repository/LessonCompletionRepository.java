package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.LessonCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Integer> {

    Optional<LessonCompletion> findByUserIdAndLessonId(Integer userId, Integer lessonId);

}