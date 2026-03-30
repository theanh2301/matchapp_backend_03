package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.PracticeQuestion;
import com.company.mathapp_backend_03.model.enums.Difficulty;
import com.company.mathapp_backend_03.model.response.*;
import com.company.mathapp_backend_03.repository.PracticeQuestionRepository;
import com.company.mathapp_backend_03.repository.PracticeAnswerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PracticeQuestionService {

    private final PracticeQuestionRepository practiceQuestionRepository;
    private final PracticeAnswerRepository practiceAnswerRepository;

    public List<PracticeQuestionResponse> getPracticeQuestionByPracticeIdAndDifficulty(Integer id, Difficulty difficulty) {
        List<PracticeQuestion> practiceQuestions = practiceQuestionRepository.findByPracticeIdAndDifficulty(id, difficulty);

        return practiceQuestions.stream().map(q -> {

            List<PracticeAnswerResponse> answers = practiceAnswerRepository
                    .findByPracticeQuestionId(q.getId())
                    .stream()
                    .map(a -> new PracticeAnswerResponse(
                            a.getId(),
                            a.getContent(),
                            a.getIsCorrect(),
                            a.getDescription()
                    ))
                    .toList();

            return new PracticeQuestionResponse(
                    q.getId(),
                    q.getContent(),
                    q.getXpReward(),
                    q.getDifficulty(),
                    answers
            );

        }).toList();
    }

    public List<PracticeQuestionResponse> getPracticeQuestionByPracticeId(Integer id) {
        List<PracticeQuestion> practiceQuestions = practiceQuestionRepository.findByPracticeId(id);

        return practiceQuestions.stream().map(q -> {

            List<PracticeAnswerResponse> answers = practiceAnswerRepository
                    .findByPracticeQuestionId(q.getId())
                    .stream()
                    .map(a -> new PracticeAnswerResponse(
                            a.getId(),
                            a.getContent(),
                            a.getIsCorrect(),
                            a.getDescription()
                    ))
                    .toList();

            return new PracticeQuestionResponse(
                    q.getId(),
                    q.getContent(),
                    q.getXpReward(),
                    q.getDifficulty(),
                    answers
            );

        }).toList();
    }

}
