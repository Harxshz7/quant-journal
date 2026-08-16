package com.tradingjournal.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "lessons_learned")
public class LessonLearned {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "lesson_tags", joinColumns = @JoinColumn(name = "lesson_id"))
    @Column(name = "tag")
    private Set<String> tags = new java.util.HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_trade_id")
    private Trade sourceTrade;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public LessonLearned() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public Trade getSourceTrade() { return sourceTrade; }
    public void setSourceTrade(Trade sourceTrade) { this.sourceTrade = sourceTrade; }
    public Instant getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
