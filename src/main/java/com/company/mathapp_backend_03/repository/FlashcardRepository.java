package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {
    boolean existsByLessonId(Integer id);
}
