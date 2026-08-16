package com.tradingjournal.presentation.journal;

import com.tradingjournal.application.journal.LessonService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.presentation.dto.CreateLessonRequest;
import com.tradingjournal.presentation.dto.LessonDTO;
import com.tradingjournal.presentation.dto.UpdateLessonRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping
    public ResponseEntity<List<LessonDTO>> getLessons(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String tag) {
        return ResponseEntity.ok(lessonService.getLessons(user, tag));
    }

    @PostMapping
    public ResponseEntity<LessonDTO> createLesson(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateLessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(user, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonDTO> updateLesson(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLessonRequest request) {
        return ResponseEntity.ok(lessonService.updateLesson(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        lessonService.deleteLesson(user, id);
        return ResponseEntity.noContent().build();
    }
}
