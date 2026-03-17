package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.Chapter;
import com.company.mathapp_backend_03.entity.Subject;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.model.request.SubjectRequest;
import com.company.mathapp_backend_03.model.response.SubjectResponse;
import com.company.mathapp_backend_03.repository.ChapterRepository;
import com.company.mathapp_backend_03.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;

    public List<SubjectResponse> getAllSubjects() {

        List<Subject> subjects = subjectRepository.findAll();

        return subjects.stream()
                .map(s -> new SubjectResponse(
                        s.getId(),
                        s.getSubjectName(),
                        s.getSubjectClass(),
                        s.getIcon()
                ))
                .toList();
    }

    public void addSubject(SubjectRequest subjectRequest) {
        Optional<Subject> existingSubject = subjectRepository.
                findBySubjectNameIgnoreCaseAndSubjectClass(
                        subjectRequest.getSubjectName(),
                        subjectRequest.getSubjectClass()
                );

        if (existingSubject.isPresent()) {
            throw new BadRequestException("Subject in this class already exist");
        }

        Subject subject = Subject.builder()
                .subjectName(subjectRequest.getSubjectName())
                .subjectClass(subjectRequest.getSubjectClass())
                .icon(subjectRequest.getIcon())
                .build();

        subjectRepository.save(subject);
    }

    public void updateSubject(Integer id, SubjectRequest subjectRequest) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Subject not found"));

        Optional<Subject> existingSubject =
                subjectRepository.findBySubjectNameIgnoreCaseAndSubjectClass(
                        subjectRequest.getSubjectName(),
                        subjectRequest.getSubjectClass()
                );

        if (existingSubject.isPresent() && !existingSubject.get().getId().equals(id)) {
            throw new BadRequestException("Subject already exists");
        }

        subject.setSubjectName(subjectRequest.getSubjectName());
        subject.setSubjectClass(subjectRequest.getSubjectClass());
        subject.setIcon(subjectRequest.getIcon());

        subjectRepository.save(subject);
    }

    public void deleteSubject(Integer id) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Subject not found"));

        if (chapterRepository.existsBySubjectId(id)) {
            throw new BadRequestException("Cannot delete subject because it contains chapter");
        }

        subjectRepository.delete(subject);
    }
}
