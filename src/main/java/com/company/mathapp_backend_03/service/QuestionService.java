package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.Answer;
import com.company.mathapp_backend_03.entity.Lesson;
import com.company.mathapp_backend_03.entity.Question;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.exception.ConflictException;
import com.company.mathapp_backend_03.exception.NotFoundException;
import com.company.mathapp_backend_03.model.request.AnswerRequest;
import com.company.mathapp_backend_03.model.request.QuestionRequest;
import com.company.mathapp_backend_03.model.response.AnswerResponse;
import com.company.mathapp_backend_03.model.response.QuestionResponse;
import com.company.mathapp_backend_03.repository.AnswerRepository;
import com.company.mathapp_backend_03.repository.LessonRepository;
import com.company.mathapp_backend_03.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final AnswerRepository  answerRepository;

    public List<QuestionResponse> getQuestionByLessonId(Integer id) {
        List<Question> questions = questionRepository.findByLessonId(id);

        return questions.stream().map(q -> {

            List<AnswerResponse> answers = answerRepository
                    .findAnswerByQuestionId(q.getId())
                    .stream()
                    .map(a -> new AnswerResponse(
                            a.getId(),
                            a.getContent(),
                            a.getIsCorrect()
                    ))
                    .toList();

            return new QuestionResponse(
                    q.getId(),
                    q.getContent(),
                    q.getTypeQuestion(),
                    q.getXpReward(),
                    answers
            );

        }).toList();
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

    @Transactional
    public void addQuestionAndAnswer(QuestionRequest questionRequest) {

        Lesson lesson = lessonRepository.findById(questionRequest.getLessonId())
                .orElseThrow(() -> new BadRequestException("Lesson not found"));

        if (questionRepository.findByContentAndTypeQuestionAndLesson(
                questionRequest.getContent(),
                questionRequest.getTypeQuestion(),
                lesson
        ).isPresent()) {
            throw new BadRequestException("Question already exists in this lesson");
        }

        validateAnswers(questionRequest.getAnswers());

        Question question = Question.builder()
                .content(questionRequest.getContent().trim())
                .typeQuestion(questionRequest.getTypeQuestion())
                .xpReward(questionRequest.getXpReward())
                .lesson(lesson)
                .build();

        questionRepository.save(question);

        List<Answer> answers = questionRequest.getAnswers().stream()
                .map(item -> Answer.builder()
                        .content(item.getContent().trim())
                        .isCorrect(item.getIsCorrect())
                        .question(question)
                        .build())
                .toList();

        answerRepository.saveAll(answers);
    }

    @Transactional
    public void updateQuestionAndAnswer(Integer id, QuestionRequest questionRequest) {

        // ===== 1. Validate lesson + question =====
        Lesson lesson = lessonRepository.findById(questionRequest.getLessonId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // ===== 2. Check duplicate question =====
        if (questionRepository.existsByContentAndTypeQuestionAndLessonAndIdNot(
                questionRequest.getContent().trim(),
                questionRequest.getTypeQuestion(),
                lesson,
                id)) {
            throw new ConflictException("Question already exists in this lesson");
        }

        // ===== 3. Validate answers (reuse) =====
        validateAnswers(questionRequest.getAnswers());

        List<Answer> currentAnswers = answerRepository.findAnswerByQuestionId(id);

        // ===== 4. Check dữ liệu có thay đổi không =====
        boolean isQuestionChanged =
                !question.getContent().equals(questionRequest.getContent().trim()) ||
                        !question.getTypeQuestion().equals(questionRequest.getTypeQuestion()) ||
                        !Objects.equals(question.getXpReward(), questionRequest.getXpReward()) ||
                        !question.getLesson().getId().equals(questionRequest.getLessonId());

        boolean isAnswerChanged = false;

        if (currentAnswers.size() != questionRequest.getAnswers().size()) {
            isAnswerChanged = true;
        } else {
            Map<Integer, AnswerRequest> requestMap = questionRequest.getAnswers().stream()
                    .filter(a -> a.getId() != null)
                    .collect(Collectors.toMap(AnswerRequest::getId, a -> a));

            for (Answer current : currentAnswers) {
                AnswerRequest req = requestMap.get(current.getId());

                if (req == null) {
                    isAnswerChanged = true;
                    break;
                }

                if (!current.getContent().equals(req.getContent().trim()) ||
                        !Objects.equals(current.getIsCorrect(), req.getIsCorrect())) {
                    isAnswerChanged = true;
                    break;
                }
            }
        }

        if (!isQuestionChanged && !isAnswerChanged) {
            throw new BadRequestException("No changes detected");
        }

        // ===== 5. Update question =====
        question.setContent(questionRequest.getContent().trim());
        question.setTypeQuestion(questionRequest.getTypeQuestion());
        question.setXpReward(questionRequest.getXpReward());
        question.setLesson(lesson);

        // ===== 6. Xử lý answers =====
        Map<Integer, Answer> currentMap = currentAnswers.stream()
                .collect(Collectors.toMap(Answer::getId, a -> a));

        List<Answer> newAnswers = new ArrayList<>();

        for (AnswerRequest aReq : questionRequest.getAnswers()) {

            if (aReq.getId() != null && !currentMap.containsKey(aReq.getId())) {
                throw new BadRequestException("Answer ID does not belong to this question");
            }

            // UPDATE
            if (aReq.getId() != null && currentMap.containsKey(aReq.getId())) {
                Answer existing = currentMap.get(aReq.getId());
                existing.setContent(aReq.getContent().trim());
                existing.setIsCorrect(aReq.getIsCorrect());

                newAnswers.add(existing);
                currentMap.remove(aReq.getId());
            }
            // INSERT
            else {
                Answer newA = new Answer();
                newA.setContent(aReq.getContent().trim());
                newA.setIsCorrect(aReq.getIsCorrect());
                newA.setQuestion(question);

                newAnswers.add(newA);
            }
        }

        // DELETE những answer không còn
        currentMap.values().forEach(answerRepository::delete);

        // SAVE
        answerRepository.saveAll(newAnswers);
        questionRepository.save(question);
    }

    private void validateAnswers(List<AnswerRequest> answers) {

        if (answers == null || answers.size() != 4) {
            throw new BadRequestException("Must provide exactly 4 answers");
        }

        Set<String> contents = new HashSet<>();

        for (AnswerRequest a : answers) {

            if (a.getContent() == null || a.getContent().trim().isEmpty()) {
                throw new BadRequestException("Answer content must not be empty");
            }

            String normalized = a.getContent().trim().toLowerCase();

            if (!contents.add(normalized)) {
                throw new BadRequestException("Duplicate answer content");
            }
        }

        long correctCount = answers.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .count();

        if (correctCount != 1) {
            throw new BadRequestException("Must have exactly 1 correct answer");
        }
    }

    @Transactional
    public void deleteQuestionAnswer(Integer id) {

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        answerRepository.deleteByQuestionId(id);

        questionRepository.delete(question);
    }
}
