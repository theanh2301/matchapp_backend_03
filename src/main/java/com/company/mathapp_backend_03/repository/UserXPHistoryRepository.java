package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.UserXPHistory;
import com.company.mathapp_backend_03.model.enums.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserXPHistoryRepository extends JpaRepository<UserXPHistory, Integer> {

    List<UserXPHistory> findByUserId(Integer userId);

    boolean existsByUserIdAndSourcedIdAndSource(Integer userId, Integer flashcardId, Source source);

    Optional<UserXPHistory> findByUserIdAndSourcedIdAndSource(Integer id, Integer id1, Source source);
}
