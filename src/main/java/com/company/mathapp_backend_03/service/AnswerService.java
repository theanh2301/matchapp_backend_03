package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.Answer;
import com.company.mathapp_backend_03.entity.Question;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.exception.NotFoundException;
import com.company.mathapp_backend_03.model.request.ListAnswerRequest;
import com.company.mathapp_backend_03.model.response.AnswerResponse;
import com.company.mathapp_backend_03.repository.AnswerRepository;
import com.company.mathapp_backend_03.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public List<AnswerResponse> getAnswersByQuestionId(Integer id) {
        List<Answer> answers = answerRepository.findAnswerByQuestionId(id);

        return answers.stream().map(a -> new AnswerResponse(
                a.getId(),
                a.getContent(),
                a.getIsCorrect()
        )).toList();
    }

    @Transactional
    public void addAnswers(ListAnswerRequest request) {

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new BadRequestException("Question not found"));

        List<Answer> existingAnswers = answerRepository.findByQuestion(question);
        if (!existingAnswers.isEmpty()) {
            throw new BadRequestException("Question already has answers");
        }

        if (request.getAnswers() == null || request.getAnswers().size() != 4) {
            throw new BadRequestException("Must provide exactly 4 answers");
        }

        long correctCount = request.getAnswers().stream()
                .filter(AnswerResponse::getIsCorrect)
                .count();

        if (correctCount != 1) {
            throw new BadRequestException("Must have exactly 1 correct answer");
        }

        List<Answer> answers = request.getAnswers().stream()
                .map(item -> Answer.builder()
                        .content(item.getContent())
                        .isCorrect(item.getIsCorrect())
                        .question(question)
                        .build())
                .toList();

        answerRepository.saveAll(answers);
    }

    @Transactional
    public void updateAnswers(ListAnswerRequest request) {

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new BadRequestException("Question not found"));

        if (request.getAnswers() == null || request.getAnswers().size() != 4) {
            throw new BadRequestException("Must provide exactly 4 answers");
        }

        long correctCount = request.getAnswers().stream()
                .filter(AnswerResponse::getIsCorrect)
                .count();

        if (correctCount != 1) {
            throw new BadRequestException("Must have exactly 1 correct answer");
        }

        Set<String> contents = request.getAnswers().stream()
                .map(AnswerResponse::getContent)
                .collect(Collectors.toSet());

        if (contents.size() < 4) {
            throw new BadRequestException("Duplicate answers are not allowed");
        }

        List<Answer> updatedAnswers = new ArrayList<>();

        for (AnswerResponse item : request.getAnswers()) {

            Answer answer = answerRepository.findById(item.getId())
                    .orElseThrow(() -> new BadRequestException("Answer not found"));

            if (!answer.getQuestion().getId().equals(question.getId())) {
                throw new BadRequestException("Answer does not belong to this question");
            }

            answer.setContent(item.getContent());
            answer.setIsCorrect(item.getIsCorrect());

            updatedAnswers.add(answer);
        }

        answerRepository.saveAll(updatedAnswers);
    }

    public void deleteAnswersByQuestion(Integer questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        answerRepository.deleteByQuestion(question);
    }
}
