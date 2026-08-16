package com.tradingjournal.application.journal;

import com.tradingjournal.domain.entity.*;
import com.tradingjournal.infrastructure.repository.LessonLearnedRepository;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.presentation.dto.CreateLessonRequest;
import com.tradingjournal.presentation.dto.LessonDTO;
import com.tradingjournal.presentation.dto.UpdateLessonRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LessonService {

    private final LessonLearnedRepository lessonRepository;
    private final TradeRepository tradeRepository;

    public LessonService(LessonLearnedRepository lessonRepository, TradeRepository tradeRepository) {
        this.lessonRepository = lessonRepository;
        this.tradeRepository = tradeRepository;
    }

    @Transactional(readOnly = true)
    public List<LessonDTO> getLessons(User user, String tag) {
        List<LessonLearned> lessons;
        if (tag != null && !tag.isBlank()) {
            lessons = lessonRepository.findByUserAndTag(user, tag.trim());
        } else {
            lessons = lessonRepository.findByUserOrderByCreatedAtDesc(user);
        }
        return lessons.stream().map(this::toDto).toList();
    }

    public LessonDTO createLesson(User user, CreateLessonRequest request) {
        LessonLearned lesson = new LessonLearned();
        lesson.setUser(user);
        lesson.setContent(request.content());
        if (request.tags() != null) lesson.setTags(request.tags());
        if (request.sourceTradeId() != null) {
            Trade trade = tradeRepository.findById(request.sourceTradeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source trade not found"));
            if (!trade.getJournalEntry().getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source trade not found");
            }
            lesson.setSourceTrade(trade);
        }
        LessonLearned saved = lessonRepository.save(lesson);
        return toDto(saved);
    }

    public LessonDTO updateLesson(User user, UUID id, UpdateLessonRequest request) {
        LessonLearned lesson = findOwnedOrThrow(user, id);
        lesson.setContent(request.content());
        if (request.tags() != null) lesson.setTags(request.tags());
        LessonLearned saved = lessonRepository.save(lesson);
        return toDto(saved);
    }

    public void deleteLesson(User user, UUID id) {
        LessonLearned lesson = findOwnedOrThrow(user, id);
        lessonRepository.delete(lesson);
    }

    private LessonLearned findOwnedOrThrow(User user, UUID id) {
        LessonLearned lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        if (!lesson.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found");
        }
        return lesson;
    }

    private LessonDTO toDto(LessonLearned lesson) {
        return new LessonDTO(
                lesson.getId(),
                lesson.getContent(),
                lesson.getTags(),
                lesson.getSourceTrade() != null ? lesson.getSourceTrade().getId() : null,
                lesson.getSourceTrade() != null ? lesson.getSourceTrade().getTicker() : null,
                lesson.getCreatedAt() != null ? lesson.getCreatedAt().toString() : null
        );
    }
}
