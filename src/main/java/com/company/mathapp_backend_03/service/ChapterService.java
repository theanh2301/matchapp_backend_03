package com.company.mathapp_backend_03.service;

import com.company.mathapp_backend_03.entity.Chapter;
import com.company.mathapp_backend_03.entity.Lesson;
import com.company.mathapp_backend_03.entity.Subject;
import com.company.mathapp_backend_03.exception.BadRequestException;
import com.company.mathapp_backend_03.model.request.ChapterRequest;
import com.company.mathapp_backend_03.model.request.SubjectRequest;
import com.company.mathapp_backend_03.model.response.ChapterResponse;
import com.company.mathapp_backend_03.repository.ChapterRepository;
import com.company.mathapp_backend_03.repository.LessonRepository;
import com.company.mathapp_backend_03.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;

    public List<ChapterResponse> getChaptersBySubjectId(Integer id) {
        List<Chapter> chapters = chapterRepository.findChapterBySubjectId(id);

        return chapters.stream().map(c-> new ChapterResponse(
            c.getId(),
            c.getChapterName(),
            c.getDescription()
        )).toList();
    }

    /*public void addChapter(ChapterRequest chapterRequest) {

        Optional<Chapter> existingChapter = chapterRepository.findChapterByChapterName(
                chapterRequest.getChapterName()
        );

        if (existingChapter.isPresent()) {
            throw new BadRequestException("Chapter already exist");
        }

        Chapter chapter = Chapter.builder()
                .chapterName(chapterRequest.getChapterName())
                .description(chapterRequest.getDescription())
                .build();

        chapterRepository.save(chapter);

    }*/

    public void addChapter(ChapterRequest chapterRequest) {

        Subject subject = subjectRepository.findById(chapterRequest.getSubjectId())
                .orElseThrow(() -> new BadRequestException("Subject not found"));

        Optional<Chapter> existingChapter = chapterRepository.findByChapterNameAndSubject(
                        chapterRequest.getChapterName(),
                        subject
                );

        if (existingChapter.isPresent()) {
            throw new BadRequestException("Chapter already exists in this subject");
        }

        Chapter chapter = Chapter.builder()
                .chapterName(chapterRequest.getChapterName())
                .description(chapterRequest.getDescription())
                .subject(subject)
                .build();

        chapterRepository.save(chapter);
    }

    public void updateChapter(Integer id, ChapterRequest chapterRequest) {

        Subject subject = subjectRepository.findById(chapterRequest.getSubjectId())
                .orElseThrow(() -> new BadRequestException("Subject not found"));

        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Chapter not found"));

        Optional<Chapter> existingChapter = chapterRepository.findByChapterNameAndSubject(
                        chapterRequest.getChapterName(),
                        subject
                );

        if (existingChapter.isPresent()) {
            throw new BadRequestException("Chapter already exists in this subject");
        }

        chapter.setChapterName(chapterRequest.getChapterName());
        chapter.setDescription(chapterRequest.getDescription());

        chapterRepository.save(chapter);
    }

    public void deleteChapter(Integer id) {

        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Chapter not found"));

        if (lessonRepository.existsByChapterId(id)) {
            throw new BadRequestException("Cannot delete chapter because it contains lessons");
        }

        chapterRepository.delete(chapter);
    }
}
