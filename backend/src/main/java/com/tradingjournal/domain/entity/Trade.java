package com.tradingjournal.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ticker", nullable = false)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_type", nullable = false)
    private PositionType positionType;

    @Column(name = "entry_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal entryPrice;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "stop_loss", precision = 19, scale = 4)
    private BigDecimal stopLoss;

    @Column(name = "strategy")
    private String strategy;

    @Column(name = "exit_price", precision = 19, scale = 4)
    private BigDecimal exitPrice;

    @Column(name = "exit_date")
    private Instant exitDate;

    @Column(name = "fees", nullable = false, precision = 19, scale = 4)
    private BigDecimal fees = BigDecimal.ZERO;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private TradeSource source = TradeSource.MANUAL;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @Lob
    @Column(name = "post_trade_reflection")
    private String postTradeReflection;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "trade_mistake_tags", joinColumns = @JoinColumn(name = "trade_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "mistake_tag")
    private Set<MistakeTag> mistakeTags = new HashSet<>();

    @Column(name = "setup_quality")
    private Integer setupQuality;

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeChecklistItem> checklistItems = new ArrayList<>();

    public Trade() {
    }

    public Trade(JournalEntry journalEntry, String ticker, PositionType positionType, BigDecimal entryPrice, BigDecimal quantity) {
        this.journalEntry = journalEntry;
        this.ticker = ticker;
        this.positionType = positionType;
        this.entryPrice = entryPrice;
        this.quantity = quantity;
        this.source = TradeSource.MANUAL;
        this.fees = BigDecimal.ZERO;
        this.deleted = false;
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
    public UUID getTradeId() { return id; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public PositionType getPositionType() { return positionType; }
    public void setPositionType(PositionType positionType) { this.positionType = positionType; }
    public BigDecimal getEntryPrice() { return entryPrice; }
    public void setEntryPrice(BigDecimal entryPrice) { this.entryPrice = entryPrice; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getStopLoss() { return stopLoss; }
    public void setStopLoss(BigDecimal stopLoss) { this.stopLoss = stopLoss; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public BigDecimal getExitPrice() { return exitPrice; }
    public void setExitPrice(BigDecimal exitPrice) { this.exitPrice = exitPrice; }
    public Instant getExitDate() { return exitDate; }
    public void setExitDate(Instant exitDate) { this.exitDate = exitDate; }
    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public TradeSource getSource() { return source; }
    public void setSource(TradeSource source) { this.source = source; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public JournalEntry getJournalEntry() { return journalEntry; }
    public void setJournalEntry(JournalEntry journalEntry) { this.journalEntry = journalEntry; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getPostTradeReflection() { return postTradeReflection; }
    public void setPostTradeReflection(String postTradeReflection) { this.postTradeReflection = postTradeReflection; }
    public Set<MistakeTag> getMistakeTags() { return mistakeTags; }
    public void setMistakeTags(Set<MistakeTag> mistakeTags) { this.mistakeTags = mistakeTags; }
    public Integer getSetupQuality() { return setupQuality; }
    public void setSetupQuality(Integer setupQuality) { this.setupQuality = setupQuality; }
    public List<TradeChecklistItem> getChecklistItems() { return checklistItems; }
    public void setChecklistItems(List<TradeChecklistItem> checklistItems) { this.checklistItems = checklistItems; }
}
