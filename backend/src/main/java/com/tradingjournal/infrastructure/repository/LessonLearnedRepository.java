package com.tradingjournal.infrastructure.repository;

import com.tradingjournal.domain.entity.LessonLearned;
import com.tradingjournal.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonLearnedRepository extends JpaRepository<LessonLearned, UUID> {
    List<LessonLearned> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT l FROM LessonLearned l WHERE l.user = :user AND :tag MEMBER OF l.tags ORDER BY l.createdAt DESC")
    List<LessonLearned> findByUserAndTag(@Param("user") User user, @Param("tag") String tag);
}
