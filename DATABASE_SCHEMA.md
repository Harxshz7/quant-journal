# Trading Journal Application - Database Schema Design

## 1. Entity-Relationship Diagram

```
┌──────────────┐
│    Users     │
├──────────────┤
│ user_id (PK) │◄─┐
│ username     │  │
│ email        │  │
│ password     │  │
│ first_name   │  │
│ last_name    │  │
│ created_at   │  │
│ updated_at   │  │
└──────────────┘  │
                  │  ┌──────────────┐
                  └──┤    Trades    │
                     ├──────────────┤
                     │ trade_id (PK)│
                     │ user_id (FK) │
                     │ ticker       │
                     │ entry_date   │
                     │ exit_date    │
                     │ entry_price  │
                     │ exit_price   │
                     │ quantity     │
                     │ position_type
                     │ status       │
                     │ pnl          │
                     │ risk_reward  │
                     │ notes        │
                     │ created_at   │
                     │ updated_at   │
                     └──────────────┘
                            │
                            ├─ Screenshots (1:Many)
                            │
                            └─ Trade Statistics (1:1)
```

## 2. Tables Design

### 2.1 Users Table

```sql
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' 
        CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT username_length CHECK (length(username) >= 3),
    CONSTRAINT email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
```

**Columns**:
- `user_id`: Unique identifier (UUID for distributed systems)
- `username`: Unique login name
- `email`: Unique email address
- `password`: Bcrypt hashed password
- `first_name/last_name`: User's full name
- `role`: RBAC role (USER or ADMIN)
- `account_status`: Account state management
- `last_login_at`: Track user activity
- `created_at/updated_at`: Audit timestamps
- `created_by/updated_by`: Audit tracking

---

### 2.2 Trades Table

```sql
CREATE TABLE trades (
    trade_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Trade Details
    ticker VARCHAR(10) NOT NULL,
    position_type VARCHAR(10) NOT NULL CHECK (position_type IN ('LONG', 'SHORT')),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' 
        CHECK (status IN ('OPEN', 'CLOSED', 'PENDING', 'CANCELLED')),
    
    -- Entry Information
    entry_date TIMESTAMP NOT NULL,
    entry_price DECIMAL(15,4) NOT NULL,
    quantity DECIMAL(15,2) NOT NULL CHECK (quantity > 0),
    
    -- Exit Information
    exit_date TIMESTAMP,
    exit_price DECIMAL(15,4),
    
    -- Calculations
    gross_pnl DECIMAL(15,2),
    net_pnl DECIMAL(15,2),
    pnl_percentage DECIMAL(10,4),
    risk_amount DECIMAL(15,2),
    reward_amount DECIMAL(15,2),
    risk_reward_ratio DECIMAL(10,4),
    
    -- Analysis
    win_loss VARCHAR(10) CHECK (win_loss IN ('WIN', 'LOSS', 'BREAK_EVEN', 'OPEN')),
    trading_strategy VARCHAR(100),
    setup_description TEXT,
    
    -- Notes & Comments
    notes TEXT,
    lessons_learned TEXT,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT valid_entry_price CHECK (entry_price > 0),
    CONSTRAINT valid_exit_price CHECK (exit_price IS NULL OR exit_price > 0),
    CONSTRAINT valid_risk_amount CHECK (risk_amount IS NULL OR risk_amount >= 0),
    CONSTRAINT valid_reward_amount CHECK (reward_amount IS NULL OR reward_amount >= 0),
    CONSTRAINT valid_pnl_only_closed CHECK (
        (status = 'CLOSED' AND exit_date IS NOT NULL AND exit_price IS NOT NULL)
        OR status != 'CLOSED'
    )
);

CREATE INDEX idx_trades_user_id ON trades(user_id);
CREATE INDEX idx_trades_entry_date ON trades(entry_date);
CREATE INDEX idx_trades_status ON trades(status);
CREATE INDEX idx_trades_ticker ON trades(ticker);
CREATE INDEX idx_trades_user_status ON trades(user_id, status);
CREATE INDEX idx_trades_win_loss ON trades(win_loss);
```

**Columns**:
- `trade_id`: Unique trade identifier
- `user_id`: Foreign key to users table
- `ticker`: Stock/currency symbol (e.g., AAPL, EUR/USD)
- `position_type`: LONG or SHORT
- `status`: Trade lifecycle state
- `entry_date/exit_date`: Trade timing
- `entry_price/exit_price`: Entry and exit price points
- `quantity`: Number of shares/contracts
- `gross_pnl/net_pnl`: Profit/Loss calculations
- `pnl_percentage`: Return percentage
- `risk_reward_ratio`: Risk/Reward metric
- `win_loss`: Calculated outcome
- `trading_strategy`: Strategy used (e.g., Breakout, Reversal)
- `notes/lessons_learned`: Trade analysis
- Audit fields for compliance

**Indexes**:
- Composite index on (user_id, status) for efficient filtering
- Separate indexes on frequently queried columns

---

### 2.3 Trade Screenshots Table

```sql
CREATE TABLE trade_screenshots (
    screenshot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_id UUID NOT NULL REFERENCES trades(trade_id) ON DELETE CASCADE,
    
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    
    original_width INT,
    original_height INT,
    
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    CONSTRAINT valid_file_type CHECK (file_type IN ('image/jpeg', 'image/png', 'image/gif', 'image/webp')),
    CONSTRAINT valid_file_size CHECK (file_size <= 10485760) -- 10MB limit
);

CREATE INDEX idx_screenshots_trade_id ON trade_screenshots(trade_id);
CREATE INDEX idx_screenshots_upload_date ON trade_screenshots(upload_date);
```

**Columns**:
- `screenshot_id`: Unique screenshot identifier
- `trade_id`: Foreign key to trades
- `file_name`: Original uploaded filename
- `file_path`: Path to stored file (S3/local storage)
- `file_size`: Size in bytes for quota management
- `file_type`: MIME type validation
- `original_width/height`: Image dimensions for UI optimization
- `upload_date`: When uploaded
- `created_by`: Who uploaded

---

### 2.4 Trade Statistics Table (Denormalized for Performance)

```sql
CREATE TABLE trade_statistics (
    stats_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Win/Loss Statistics
    total_trades INT DEFAULT 0,
    winning_trades INT DEFAULT 0,
    losing_trades INT DEFAULT 0,
    break_even_trades INT DEFAULT 0,
    win_rate DECIMAL(5,2) DEFAULT 0,
    
    -- P&L Statistics
    total_gross_pnl DECIMAL(15,2) DEFAULT 0,
    total_net_pnl DECIMAL(15,2) DEFAULT 0,
    largest_win DECIMAL(15,2),
    largest_loss DECIMAL(15,2),
    average_win DECIMAL(15,2),
    average_loss DECIMAL(15,2),
    profit_factor DECIMAL(10,4),
    
    -- Risk/Reward Statistics
    total_risk_amount DECIMAL(15,2) DEFAULT 0,
    total_reward_amount DECIMAL(15,2) DEFAULT 0,
    average_risk_reward_ratio DECIMAL(10,4),
    
    -- Consecutive Statistics
    consecutive_wins INT DEFAULT 0,
    consecutive_losses INT DEFAULT 0,
    max_consecutive_wins INT DEFAULT 0,
    max_consecutive_losses INT DEFAULT 0,
    
    -- Other Metrics
    avg_trade_duration INTERVAL,
    win_loss_ratio DECIMAL(10,4),
    
    last_calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_win_rate CHECK (win_rate >= 0 AND win_rate <= 100)
);

CREATE INDEX idx_stats_user_id ON trade_statistics(user_id);
```

**Purpose**:
- Denormalized table for rapid dashboard queries
- Calculated and cached from trades table
- Updated after each trade operation
- Reduces query complexity for analytics

---

### 2.5 Trading Journal / Journal Entries Table (Optional but Recommended)

```sql
CREATE TABLE journal_entries (
    entry_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    trade_id UUID REFERENCES trades(trade_id) ON DELETE SET NULL,
    
    entry_type VARCHAR(50) NOT NULL 
        CHECK (entry_type IN ('TRADE_ENTRY', 'REFLECTION', 'MARKET_ANALYSIS', 'LEARNING')),
    
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    
    mood VARCHAR(20) CHECK (mood IN ('EXCELLENT', 'GOOD', 'NEUTRAL', 'POOR', 'TERRIBLE')),
    market_condition VARCHAR(50),
    
    is_private BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX idx_journal_user_id ON journal_entries(user_id);
CREATE INDEX idx_journal_trade_id ON journal_entries(trade_id);
CREATE INDEX idx_journal_created_at ON journal_entries(created_at);
```

---

### 2.6 Trading Performance History Table (For Trend Analysis)

```sql
CREATE TABLE trading_performance_history (
    history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
    period_start_date DATE NOT NULL,
    period_end_date DATE NOT NULL,
    
    -- Monthly/Weekly/Daily snapshot
    period_type VARCHAR(20) NOT NULL CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    
    trades_count INT,
    wins_count INT,
    losses_count INT,
    win_rate DECIMAL(5,2),
    period_pnl DECIMAL(15,2),
    period_return_percentage DECIMAL(10,4),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_perf_history_user_date ON trading_performance_history(user_id, period_start_date);
```

---

## 3. Relationships Summary

| Relationship | Type | Purpose |
|-------------|------|---------|
| User → Trades | 1:M | One user has multiple trades |
| Trade → Screenshots | 1:M | One trade can have multiple screenshots |
| User → Statistics | 1:1 | Each user has one statistics record |
| User → Journal Entries | 1:M | Users can write multiple journal entries |
| Trade → Journal Entries | 1:M | Multiple journal entries can reference one trade |
| User → Performance History | 1:M | Track user performance over time |

---

## 4. Key Constraints & Validations

| Constraint | Purpose |
|-----------|---------|
| NOT NULL | Enforce required fields |
| UNIQUE | Prevent duplicates |
| FOREIGN KEY | Maintain referential integrity |
| CHECK | Business rule validation |
| Computed Fields | Automatically calculated values (P&L) |

---

## 5. Indexing Strategy

### Frequently Queried Patterns

```sql
-- Get all trades for a user
SELECT * FROM trades WHERE user_id = ? ORDER BY entry_date DESC;
-- Index: (user_id, entry_date DESC)

-- Get open trades
SELECT * FROM trades WHERE user_id = ? AND status = 'OPEN';
-- Index: (user_id, status)

-- Search by date range
SELECT * FROM trades WHERE user_id = ? AND entry_date BETWEEN ? AND ?;
-- Index: (user_id, entry_date)

-- Get user statistics
SELECT * FROM trade_statistics WHERE user_id = ?;
-- Index: user_id (UNIQUE)

-- Dashboard queries
SELECT * FROM trades WHERE user_id = ? AND status = 'CLOSED' 
    AND exit_date >= DATE_TRUNC('month', CURRENT_DATE);
-- Index: (user_id, status, exit_date)
```

### Index Recommendations

```
High Priority (Must Have):
- (user_id, status)
- (user_id, entry_date DESC)
- (user_id) on trade_statistics
- user_id on all related tables

Medium Priority (Performance):
- ticker (for ticker analysis)
- win_loss (for statistics queries)
- upload_date on screenshots

Low Priority (Convenience):
- username on users
- email on users
```

---

## 6. Query Performance Optimization

### Denormalization Rationale

The `trade_statistics` table is intentionally denormalized to avoid expensive aggregations:

```sql
-- Without denormalization (expensive):
SELECT 
    COUNT(*) as total_trades,
    COUNT(CASE WHEN win_loss = 'WIN' THEN 1 END) as winning_trades,
    AVG(net_pnl) as avg_pnl
FROM trades
WHERE user_id = ?;

-- With denormalization (fast):
SELECT 
    total_trades,
    winning_trades,
    (winning_trades::DECIMAL / total_trades) * 100 as win_rate
FROM trade_statistics
WHERE user_id = ?;
```

---

## 7. Data Integrity Rules

### Business Rules Implemented at DB Level

1. **Trade Closure Rule**: Cannot close a trade without exit_date and exit_price
2. **Quantity Rule**: Quantity must always be positive
3. **Price Validation**: Prices must be positive
4. **File Upload**: Max 10MB, limited file types
5. **PnL Calculation**: Only calculated for closed trades
6. **Audit Trail**: created_by and updated_by tracked for compliance

---

## 8. Backup & Recovery Strategy

```
Backup Schedule:
- Hourly: Transaction log backups
- Daily: Full database backup at 2 AM UTC
- Weekly: Archive backup to cold storage
- Monthly: Long-term retention backup

Recovery Options:
- Point-in-time recovery up to 30 days
- Full database restore
- Table-level recovery
```

---

## 9. Data Migration Path

For multi-tenant deployment in future:

```sql
-- Add tenant_id column
ALTER TABLE users ADD COLUMN tenant_id UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE trades ADD COLUMN tenant_id UUID;
-- Backfill tenant_id from users
UPDATE trades SET tenant_id = (SELECT tenant_id FROM users WHERE user_id = trades.user_id);
-- Add constraints
ALTER TABLE trades ADD CONSTRAINT fk_trades_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id);
```

---

## 10. Growth Projections

### Data Volume Estimates

```
Assumptions:
- 10,000 active users
- Average 50 trades per user per year
- 2 screenshots per trade
- 1 year retention

Storage Needed:
- Database: ~5GB
- Screenshots: ~50GB (at 5MB avg per screenshot)
- Total: ~55GB

Query Performance:
- User with 500 trades: Query time < 100ms
- Statistics calculation: < 50ms
- Dashboard load: < 500ms
```

