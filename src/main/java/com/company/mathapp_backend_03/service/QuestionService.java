package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.Lesson;
import com.company.mathapp_backend_03.entity.Question;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.exception.ConflictException;
import com.company.mathapp_backend_03.exception.NotFoundException;
import com.company.mathapp_backend_03.model.request.QuestionRequest;
import com.company.mathapp_backend_03.model.response.QuestionResponse;
import com.company.mathapp_backend_03.repository.AnswerRepository;
import com.company.mathapp_backend_03.repository.LessonRepository;
import com.company.mathapp_backend_03.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final AnswerRepository  answerRepository;

    public List<QuestionResponse> getQuestionByLessonId(Integer id) {
        List<Question> questions = questionRepository.findByLessonId(id);

        return questions.stream().map(q-> new QuestionResponse(
                q.getId(),
                q.getContent(),
                q.getTypeQuestion(),
                q.getXpReward()

        )).toList();
    }

    public void addQuestion(QuestionRequest questionRequest) {
        Lesson lesson = lessonRepository.findById(questionRequest.getLessonId())
                .orElseThrow(() -> new BadRequestException("Lesson not found"));

        Optional<Question> existingQuestion = questionRepository.findByContentAndTypeQuestionAndLesson(
                questionRequest.getContent(),
                questionRequest.getTypeQuestion(),
                lesson
            );

        if (existingQuestion.isPresent()) {
            throw new BadRequestException("Question already exists in this lesson");
        }

        Question question = Question.builder()
                .content(questionRequest.getContent())
                .typeQuestion(questionRequest.getTypeQuestion())
                .xpReward(questionRequest.getXpReward())
                .lesson(lesson)
                .build();
        questionRepository.save(question);

    }

    public void updateQuestion(Integer id, QuestionRequest questionRequest) {

        Lesson lesson = lessonRepository.findById(questionRequest.getLessonId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        if (questionRepository.existsByContentAndTypeQuestionAndLessonAndIdNot(
                questionRequest.getContent(),
                questionRequest.getTypeQuestion(),
                lesson,
                id)) {
            throw new ConflictException("Question already exists in this lesson");
        }

        if (question.getContent().equals(questionRequest.getContent())
                && question.getTypeQuestion().equals(questionRequest.getTypeQuestion())
                && Objects.equals(question.getXpReward(), questionRequest.getXpReward())
                && question.getLesson().getId().equals(lesson.getId())) {
            throw new BadRequestException("No changes detected");
        }

        question.setContent(questionRequest.getContent());
        question.setTypeQuestion(questionRequest.getTypeQuestion());
        question.setXpReward(questionRequest.getXpReward());
        question.setLesson(lesson);

        questionRepository.save(question);
    }

    public void deleteQuestion(Integer id) {

        if (!questionRepository.existsById(id)) {
            throw new NotFoundException("Question not found");
        }

        if (answerRepository.existsByQuestionId(id)) {
            throw new ConflictException("Cannot delete question because it contains answers");
        }

        try {
            questionRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Cannot delete question due to data constraints");
        }
    }

}
