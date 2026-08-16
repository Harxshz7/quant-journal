package com.tradingjournal.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "trade_checklist_items")
public class TradeChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "checked", nullable = false)
    private boolean checked = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    public TradeChecklistItem() {}

    public TradeChecklistItem(Trade trade, String text) {
        this.trade = trade;
        this.text = text;
        this.checked = false;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
    public Trade getTrade() { return trade; }
    public void setTrade(Trade trade) { this.trade = trade; }
}
