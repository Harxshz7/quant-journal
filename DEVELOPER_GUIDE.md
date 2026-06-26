# Quick Reference Guide for Developers

## Before You Start Coding

1. **Read These Documents** (in order):
   - `README.md` - Project overview
   - `ARCHITECTURE.md` - System design
   - `FOLDER_STRUCTURE.md` - Where to put code
   - `DATABASE_SCHEMA.md` - Data model

2. **Understand the Layers**:
   - **Presentation**: Controllers, receive HTTP requests, return responses
   - **Application**: Services, implement business logic, DTOs, mappers
   - **Domain**: Entities, enums, exceptions, business rules
   - **Infrastructure**: Repositories, databases, file storage

3. **Remember**: Layers only depend on layers below them!

---

## Backend Development Quick Start

### Creating a New Feature (e.g., Trade Management)

**Step 1: Create Domain Layer (Domain)**
```java
// Trade.java - Entity
@Entity
@Table(name = "trades")
public class Trade {
    @Id
    private UUID tradeId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String ticker;
    private PositionType positionType; // LONG, SHORT
    private BigDecimal entryPrice;
    // ... other fields
}

// PositionType.java - Enum
public enum PositionType {
    LONG, SHORT
}

// TradeNotFoundException.java - Exception
public class TradeNotFoundException extends BusinessException {
    public TradeNotFoundException(UUID tradeId) {
        super("Trade not found: " + tradeId);
    }
}
```

**Step 2: Create Infrastructure Layer (Repository)**
```java
// TradeRepository.java
@Repository
public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findByUserId(UUID userId);
    Optional<Trade> findByIdAndUserId(UUID tradeId, UUID userId);
    // Add custom methods for complex queries
}
```

**Step 3: Create Application Layer (Service)**
```java
// TradeService.java
@Service
@Transactional
public class TradeService {
    @Autowired
    private TradeRepository tradeRepository;
    
    public Trade createTrade(CreateTradeRequest request, UUID userId) {
        // Validation
        tradeValidator.validate(request);
        
        // Create entity
        Trade trade = new Trade();
        trade.setUserId(userId);
        trade.setTicker(request.getTicker());
        // ... set other fields
        
        // Calculate P&L if closed
        if (trade.getStatus() == TradeStatus.CLOSED) {
            calculatePnL(trade);
        }
        
        // Save
        return tradeRepository.save(trade);
    }
    
    public Trade getTrade(UUID tradeId, UUID userId) {
        return tradeRepository.findByIdAndUserId(tradeId, userId)
            .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }
}

// TradeMapper.java
@Component
public class TradeMapper {
    public TradeDTO toDTO(Trade entity) {
        return TradeDTO.builder()
            .tradeId(entity.getTradeId())
            .ticker(entity.getTicker())
            .build();
    }
    
    public Trade toEntity(CreateTradeRequest request) {
        // Convert DTO to entity
        return new Trade();
    }
}
```

**Step 4: Create Presentation Layer (Controller)**
```java
// TradeController.java
@RestController
@RequestMapping("/api/v1/trades")
public class TradeController {
    @Autowired
    private TradeService tradeService;
    
    @Autowired
    private TradeMapper tradeMapper;
    
    @PostMapping
    public ResponseEntity<TradeDTO> createTrade(
            @Valid @RequestBody CreateTradeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        Trade trade = tradeService.createTrade(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(tradeMapper.toDTO(trade));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TradeDTO> getTrade(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        Trade trade = tradeService.getTrade(id, userId);
        return ResponseEntity.ok(tradeMapper.toDTO(trade));
    }
}
```

**Step 5: Create DTOs**
```java
// CreateTradeRequest.java
@Data
@Builder
public class CreateTradeRequest {
    @NotBlank(message = "Ticker is required")
    private String ticker;
    
    @NotNull(message = "Position type is required")
    private PositionType positionType;
    
    @NotNull(message = "Entry price is required")
    @Positive(message = "Entry price must be positive")
    private BigDecimal entryPrice;
    
    // ... other fields
}

// TradeDTO.java
@Data
@Builder
public class TradeDTO {
    private UUID tradeId;
    private String ticker;
    private PositionType positionType;
    private BigDecimal entryPrice;
    private BigDecimal grossPnL;
    // ... other fields
}
```

---

## Frontend Development Quick Start

### Creating a New Page (e.g., Trade History)

**Step 1: Create Page Component**
```jsx
// pages/TradeHistory/TradeHistory.jsx
import React, { useState, useEffect } from 'react';
import TradeTable from './components/TradeTable';
import TradeFilters from './components/TradeFilters';

export default function TradeHistory() {
  const [trades, setTrades] = useState([]);
  const [filters, setFilters] = useState({});
  const [loading, setLoading] = useState(false);
  
  useEffect(() => {
    fetchTrades();
  }, [filters]);
  
  const fetchTrades = async () => {
    setLoading(true);
    try {
      const response = await tradeApi.getTrades(filters);
      setTrades(response.content);
    } catch (error) {
      console.error('Failed to fetch trades:', error);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="trade-history">
      <h1>Trade History</h1>
      <TradeFilters onFilterChange={setFilters} />
      {loading ? <Loading /> : <TradeTable trades={trades} />}
    </div>
  );
}
```

**Step 2: Create Sub-components**
```jsx
// pages/TradeHistory/components/TradeTable.jsx
export default function TradeTable({ trades }) {
  return (
    <table className="trades-table">
      <thead>
        <tr>
          <th>Ticker</th>
          <th>Entry Date</th>
          <th>Entry Price</th>
          <th>Exit Price</th>
          <th>P&L</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        {trades.map(trade => (
          <tr key={trade.tradeId}>
            <td>{trade.ticker}</td>
            <td>{formatDate(trade.entryDate)}</td>
            <td>${trade.entryPrice}</td>
            <td>${trade.exitPrice}</td>
            <td className={trade.grossPnL > 0 ? 'positive' : 'negative'}>
              ${trade.grossPnL}
            </td>
            <td>{trade.status}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```

**Step 3: Create Custom Hook**
```jsx
// pages/TradeHistory/hooks/useTradeHistory.js
import { useState, useEffect } from 'react';
import { tradeApi } from '../../../api/tradeApi';

export function useTradeHistory() {
  const [trades, setTrades] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const fetchTrades = async (filters = {}) => {
    setLoading(true);
    try {
      const response = await tradeApi.getTrades(filters);
      setTrades(response.content);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  return { trades, loading, error, fetchTrades };
}
```

**Step 4: Create API Client**
```jsx
// api/tradeApi.js
import client from './client';

export const tradeApi = {
  getTrades: (filters = {}) => 
    client.get('/trades', { params: filters }),
  
  getTrade: (id) => 
    client.get(`/trades/${id}`),
  
  createTrade: (data) => 
    client.post('/trades', data),
  
  updateTrade: (id, data) => 
    client.put(`/trades/${id}`, data),
  
  deleteTrade: (id) => 
    client.delete(`/trades/${id}`)
};
```

---

## Common Patterns to Follow

### Backend Patterns

**1. Service with Validation**
```java
@Service
public class TradeService {
    public Trade createTrade(CreateTradeRequest request) {
        // 1. Validate input
        tradeValidator.validate(request);
        
        // 2. Create entity
        Trade trade = mapper.toEntity(request);
        
        // 3. Apply business logic
        trade.calculatePnL();
        
        // 4. Save to database
        return tradeRepository.save(trade);
        
        // 5. Publish event (optional)
        // eventPublisher.publish(new TradeCreatedEvent(trade));
    }
}
```

**2. Controller with Proper Responses**
```java
@RestController
public class TradeController {
    @PostMapping
    public ResponseEntity<TradeDTO> create(
            @Valid @RequestBody CreateTradeRequest request) {
        Trade trade = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapper.toDTO(trade));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TradeDTO> get(@PathVariable UUID id) {
        Trade trade = service.getOrThrow(id);
        return ResponseEntity.ok(mapper.toDTO(trade));
    }
}
```

**3. Global Exception Handler**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TradeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            TradeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build());
    }
}
```

### Frontend Patterns

**1. Custom Hook Pattern**
```jsx
function useApi(url) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const response = await client.get(url);
        setData(response.data);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };
    
    fetchData();
  }, [url]);
  
  return { data, loading, error };
}
```

**2. Controlled Form Pattern**
```jsx
function TradeForm() {
  const [formData, setFormData] = useState({
    ticker: '',
    entryPrice: '',
    quantity: ''
  });
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    await tradeApi.createTrade(formData);
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <input 
        name="ticker" 
        value={formData.ticker} 
        onChange={handleChange}
      />
      {/* ... other inputs ... */}
    </form>
  );
}
```

---

## Testing Examples

### Backend Unit Test
```java
@SpringBootTest
public class TradeServiceTest {
    @Mock
    private TradeRepository tradeRepository;
    
    @InjectMocks
    private TradeService tradeService;
    
    @Test
    void testCreateTrade() {
        CreateTradeRequest request = CreateTradeRequest.builder()
            .ticker("AAPL")
            .entryPrice(BigDecimal.valueOf(150.50))
            .build();
        
        Trade trade = tradeService.createTrade(request);
        
        assertEquals("AAPL", trade.getTicker());
        verify(tradeRepository).save(any(Trade.class));
    }
}
```

### Frontend Component Test
```jsx
import { render, screen } from '@testing-library/react';
import TradeTable from './TradeTable';

describe('TradeTable', () => {
  it('renders trade data', () => {
    const trades = [{
      tradeId: '1',
      ticker: 'AAPL',
      entryPrice: 150.50
    }];
    
    render(<TradeTable trades={trades} />);
    
    expect(screen.getByText('AAPL')).toBeInTheDocument();
  });
});
```

---

## Database Access Patterns

**Querying with JPA**:
```java
// In Repository
List<Trade> findByUserId(UUID userId);
List<Trade> findByUserIdAndStatus(UUID userId, TradeStatus status);
Optional<Trade> findByIdAndUserId(UUID id, UUID userId);

@Query("SELECT t FROM Trade t WHERE t.user.id = ?1 ORDER BY t.entryDate DESC")
List<Trade> findRecentTrades(UUID userId, Pageable pageable);
```

**Using Pagination**:
```java
Page<Trade> trades = tradeRepository.findByUserId(
    userId, 
    PageRequest.of(0, 20, Sort.by("entryDate").descending())
);
```

---

## Common Git Workflow

```bash
# Create feature branch
git checkout -b feature/add-trade-management

# Make changes and commit
git add .
git commit -m "feat: add trade CRUD operations

- Implement TradeService with validation
- Create TradeController with REST endpoints
- Add database schema for trades table"

# Keep your branch updated
git pull origin main

# Push to remote
git push origin feature/add-trade-management

# Create Pull Request on GitHub
```

---

## Debugging Tips

### Backend Debugging
```bash
# Run with debug logging
export LOG_LEVEL=DEBUG
mvn spring-boot:run

# Use breakpoints in IDE
# Run with debugger: Debug As -> Java Application in Eclipse
```

### Frontend Debugging
```bash
# Use browser DevTools (F12)
# React DevTools extension for Chrome
# Console.log() for quick debugging
console.log('Trade data:', trade);
```

---

## Performance Best Practices

✅ **DO**:
- Use pagination for large queries
- Create database indexes on frequently queried columns
- Cache statistics (don't recalculate on every request)
- Use DTOs to avoid unnecessary data transfer
- Lazy load related entities with `@Lazy`

❌ **DON'T**:
- Load all entities without pagination
- Use `N+1` query problems
- Calculate statistics on every request
- Select unnecessary columns in queries
- Expose internal entity structure directly

---

## Documentation Standards

Every public method should have:
```java
/**
 * Creates a new trade record for the user.
 * 
 * @param request the trade creation request containing all required data
 * @param userId the ID of the user creating the trade
 * @return the created trade with calculated metrics
 * @throws ValidationException if trade data is invalid
 * @throws UserNotFoundException if user doesn't exist
 */
public Trade createTrade(CreateTradeRequest request, UUID userId) {
    // ...
}
```

---

## Helpful Resources

- Clean Code by Robert C. Martin
- Spring Boot in Action by Craig Walls
- React Documentation: https://react.dev
- PostgreSQL Documentation: https://www.postgresql.org/docs
- JWT Best Practices: https://tools.ietf.org/html/rfc7519

---

**Remember**: 
- Keep code simple and readable
- Write tests as you code
- Ask questions if unsure
- Document your decisions
- Review the architecture before coding

Good luck! 🚀

