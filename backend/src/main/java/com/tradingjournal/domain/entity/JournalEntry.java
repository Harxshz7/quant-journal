package com.tradingjournal.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "journal_entries",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_entry_date", columnNames = {"user_id", "entry_date"})
    }
)
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood")
    private Mood mood;

    @Column(name = "energy")
    private Integer energy;

    @Column(name = "market_bias")
    private String marketBias;

    @Column(name = "daily_goal")
    private String dailyGoal;

    @Column(name = "day_rating")
    private Integer dayRating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trade> trades = new ArrayList<>();

    public JournalEntry() {
    }

    public JournalEntry(User user, LocalDate entryDate, String notes) {
        this.user = user;
        this.entryDate = entryDate;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getJournalEntryId() { return id; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<Trade> getTrades() { return trades; }
    public void setTrades(List<Trade> trades) { this.trades = trades; }
    public Mood getMood() { return mood; }
    public void setMood(Mood mood) { this.mood = mood; }
    public Integer getEnergy() { return energy; }
    public void setEnergy(Integer energy) { this.energy = energy; }
    public String getMarketBias() { return marketBias; }
    public void setMarketBias(String marketBias) { this.marketBias = marketBias; }
    public String getDailyGoal() { return dailyGoal; }
    public void setDailyGoal(String dailyGoal) { this.dailyGoal = dailyGoal; }
    public Integer getDayRating() { return dayRating; }
    public void setDayRating(Integer dayRating) { this.dayRating = dayRating; }
}
