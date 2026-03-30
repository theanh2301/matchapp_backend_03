package com.company.mathapp_backend_03.rest;

import com.company.mathapp_backend_03.model.dto.SubjectOverviewDTO;
import com.company.mathapp_backend_03.model.request.SubjectRequest;
import com.company.mathapp_backend_03.model.response.ApiResponse;
import com.company.mathapp_backend_03.model.response.SubjectResponse;
import com.company.mathapp_backend_03.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectApi {

    private final SubjectService subjectService;

    @GetMapping
    public List<SubjectResponse> getSubjects() {
        return subjectService.getAllSubjects();
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<List<SubjectOverviewDTO>>> getSubjectOverviews(
            @RequestParam Integer userId,
            @RequestParam Integer subjectClass) {

        List<SubjectOverviewDTO> overviews = subjectService.getSubjectOverviews(userId, subjectClass);

        ApiResponse<List<SubjectOverviewDTO>> response = new ApiResponse<>(
                200,
                "Lấy danh sách môn học thành công",
                overviews
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addSubject(@Valid @RequestBody SubjectRequest request) {
        subjectService.addSubject(request);
        return ResponseEntity.ok("Subject created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable Integer id,
                                          @Valid @RequestBody SubjectRequest subjectRequest) {

        subjectService.updateSubject(id, subjectRequest);

        return ResponseEntity.ok("Subject updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Integer id) {

        subjectService.deleteSubject(id);

        return ResponseEntity.ok("Subject deleted successfully");
    }
}
