package com.company.mathapp_backend_03.repository;

import com.company.mathapp_backend_03.entity.UserXPHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserXPHistoryRepository extends JpaRepository<UserXPHistory, Integer> {

    List<UserXPHistory> findByUserId(Integer userId);
}
