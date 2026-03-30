package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.QuizAnswer;
import com.company.mathapp_backend_03.entity.QuizQuestion;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.exception.NotFoundException;
import com.company.mathapp_backend_03.model.request.ListAnswerRequest;
import com.company.mathapp_backend_03.model.response.QuizAnswerResponse;
import com.company.mathapp_backend_03.repository.QuizAnswerRepository;
import com.company.mathapp_backend_03.repository.QuizQuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAnswerService {
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    public List<QuizAnswerResponse> getAnswersByQuestionId(Integer id) {
        List<QuizAnswer> quizAnswers = quizAnswerRepository.findAnswerByQuizQuestionId(id);

        return quizAnswers.stream().map(a -> new QuizAnswerResponse(
                a.getId(),
                a.getContent(),
                a.getIsCorrect(),
                a.getDescription()
        )).toList();
    }

    @Transactional
    public void addAnswers(ListAnswerRequest request) {

        QuizQuestion quizQuestion = quizQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new BadRequestException("Question not found"));

        List<QuizAnswer> existingQuizAnswers = quizAnswerRepository.findByQuizQuestion(quizQuestion);
        if (!existingQuizAnswers.isEmpty()) {
            throw new BadRequestException("Question already has answers");
        }

        if (request.getAnswers() == null || request.getAnswers().size() != 4) {
            throw new BadRequestException("Must provide exactly 4 answers");
        }

        long correctCount = request.getAnswers().stream()
                .filter(QuizAnswerResponse::getIsCorrect)
                .count();

        if (correctCount != 1) {
            throw new BadRequestException("Must have exactly 1 correct answer");
        }

        List<QuizAnswer> quizAnswers = request.getAnswers().stream()
                .map(item -> QuizAnswer.builder()
                        .content(item.getContent())
                        .isCorrect(item.getIsCorrect())
                        .quizQuestion(quizQuestion)
                        .build())
                .toList();

        quizAnswerRepository.saveAll(quizAnswers);
    }

    @Transactional
    public void updateAnswers(ListAnswerRequest request) {

        QuizQuestion quizQuestion = quizQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new BadRequestException("Question not found"));

        if (request.getAnswers() == null || request.getAnswers().size() != 4) {
            throw new BadRequestException("Must provide exactly 4 answers");
        }

        long correctCount = request.getAnswers().stream()
                .filter(QuizAnswerResponse::getIsCorrect)
                .count();

        if (correctCount != 1) {
            throw new BadRequestException("Must have exactly 1 correct answer");
        }

        Set<String> contents = request.getAnswers().stream()
                .map(QuizAnswerResponse::getContent)
                .collect(Collectors.toSet());

        if (contents.size() < 4) {
            throw new BadRequestException("Duplicate answers are not allowed");
        }

        List<QuizAnswer> updatedQuizAnswers = new ArrayList<>();

        for (QuizAnswerResponse item : request.getAnswers()) {

            QuizAnswer quizAnswer = quizAnswerRepository.findById(item.getId())
                    .orElseThrow(() -> new BadRequestException("Answer not found"));

            if (!quizAnswer.getQuizQuestion().getId().equals(quizQuestion.getId())) {
                throw new BadRequestException("Answer does not belong to this question");
            }

            quizAnswer.setContent(item.getContent());
            quizAnswer.setIsCorrect(item.getIsCorrect());

            updatedQuizAnswers.add(quizAnswer);
        }

        quizAnswerRepository.saveAll(updatedQuizAnswers);
    }

    public void deleteAnswersByQuestion(Integer questionId) {
        QuizQuestion quizQuestion = quizQuestionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        quizAnswerRepository.deleteByQuizQuestion(quizQuestion);
    }
}
