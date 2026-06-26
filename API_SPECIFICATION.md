# Trading Journal Application - API Specification

## 1. API Overview

- **Base URL**: `http://localhost:8080/api/v1` (Development)
- **Production URL**: `https://api.tradingjournal.com/api/v1`
- **Format**: JSON
- **Authentication**: JWT Bearer Token
- **Rate Limiting**: 1000 requests/hour per user

---

## 2. Authentication Endpoints

### 2.1 User Registration

```
POST /auth/register
Content-Type: application/json

Request Body:
{
  "username": "trader123",
  "email": "trader@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}

Response (201 Created):
{
  "userId": "uuid-string",
  "username": "trader123",
  "email": "trader@example.com",
  "message": "User registered successfully",
  "createdAt": "2026-06-26T10:30:00Z"
}

Error Responses:
400 - Bad Request: Invalid input data
409 - Conflict: Username or email already exists
```

### 2.2 User Login

```
POST /auth/login
Content-Type: application/json

Request Body:
{
  "username": "trader123",
  "password": "SecurePass123!"
}

Response (200 OK):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "userId": "uuid-string",
    "username": "trader123",
    "email": "trader@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}

Error Responses:
401 - Unauthorized: Invalid credentials
```

### 2.3 Refresh Token

```
POST /auth/refresh-token
Content-Type: application/json

Request Body:
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response (200 OK):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}

Error Responses:
401 - Unauthorized: Invalid or expired refresh token
```

### 2.4 Logout

```
POST /auth/logout
Authorization: Bearer {accessToken}

Response (204 No Content)

Error Responses:
401 - Unauthorized: Invalid token
```

### 2.5 Get Current User

```
GET /auth/me
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "userId": "uuid-string",
  "username": "trader123",
  "email": "trader@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER",
  "accountStatus": "ACTIVE",
  "lastLoginAt": "2026-06-26T10:30:00Z",
  "createdAt": "2026-06-25T12:00:00Z"
}

Error Responses:
401 - Unauthorized: Invalid token
```

---

## 3. User Management Endpoints

### 3.1 Get User Profile

```
GET /users/{userId}
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "userId": "uuid-string",
  "username": "trader123",
  "email": "trader@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2026-06-25T12:00:00Z"
}

Error Responses:
401 - Unauthorized
403 - Forbidden: Cannot access other user's profile
404 - Not Found: User not found
```

### 3.2 Update User Profile

```
PUT /users/{userId}
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "newemail@example.com"
}

Response (200 OK):
{
  "userId": "uuid-string",
  "username": "trader123",
  "email": "newemail@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "updatedAt": "2026-06-26T10:30:00Z"
}

Error Responses:
400 - Bad Request: Invalid input
401 - Unauthorized
404 - Not Found
```

### 3.3 Change Password

```
POST /users/{userId}/change-password
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewPass123!",
  "confirmPassword": "NewPass123!"
}

Response (200 OK):
{
  "message": "Password changed successfully"
}

Error Responses:
400 - Bad Request: Invalid password format
401 - Unauthorized: Current password incorrect
404 - Not Found
```

---

## 4. Trade Management Endpoints

### 4.1 Create Trade

```
POST /trades
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "ticker": "AAPL",
  "positionType": "LONG",
  "entryDate": "2026-06-26T10:30:00Z",
  "entryPrice": 150.50,
  "quantity": 100,
  "exitDate": "2026-06-27T14:00:00Z",
  "exitPrice": 152.75,
  "riskAmount": 50.00,
  "rewardAmount": 225.00,
  "tradingStrategy": "Breakout",
  "setupDescription": "Price broke above resistance at 150",
  "notes": "Good follow-through, hit target at 152.75"
}

Response (201 Created):
{
  "tradeId": "uuid-string",
  "userId": "uuid-string",
  "ticker": "AAPL",
  "positionType": "LONG",
  "status": "CLOSED",
  "entryDate": "2026-06-26T10:30:00Z",
  "entryPrice": 150.50,
  "quantity": 100,
  "exitDate": "2026-06-27T14:00:00Z",
  "exitPrice": 152.75,
  "grossPnL": 225.00,
  "netPnL": 225.00,
  "pnlPercentage": 1.49,
  "riskAmount": 50.00,
  "rewardAmount": 225.00,
  "riskRewardRatio": 4.5,
  "winLoss": "WIN",
  "tradingStrategy": "Breakout",
  "notes": "Good follow-through, hit target at 152.75",
  "createdAt": "2026-06-26T10:30:00Z"
}

Error Responses:
400 - Bad Request: Invalid input
401 - Unauthorized
422 - Unprocessable Entity: Validation errors
```

### 4.2 Get All Trades (Paginated)

```
GET /trades?page=0&size=20&sort=entryDate,desc&status=CLOSED&ticker=AAPL
Authorization: Bearer {accessToken}

Query Parameters:
- page: Page number (0-indexed, default: 0)
- size: Items per page (default: 20, max: 100)
- sort: Sort field and direction (entryDate,desc)
- status: Filter by status (OPEN, CLOSED, PENDING, CANCELLED)
- ticker: Filter by ticker symbol
- dateFrom: Filter trades after this date (ISO 8601)
- dateTo: Filter trades before this date (ISO 8601)

Response (200 OK):
{
  "content": [
    {
      "tradeId": "uuid-string",
      "ticker": "AAPL",
      "positionType": "LONG",
      "status": "CLOSED",
      "entryDate": "2026-06-26T10:30:00Z",
      "entryPrice": 150.50,
      "exitPrice": 152.75,
      "quantity": 100,
      "grossPnL": 225.00,
      "winLoss": "WIN"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}

Error Responses:
401 - Unauthorized
400 - Bad Request: Invalid query parameters
```

### 4.3 Get Trade by ID

```
GET /trades/{tradeId}
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "tradeId": "uuid-string",
  "userId": "uuid-string",
  "ticker": "AAPL",
  "positionType": "LONG",
  "status": "CLOSED",
  "entryDate": "2026-06-26T10:30:00Z",
  "entryPrice": 150.50,
  "quantity": 100,
  "exitDate": "2026-06-27T14:00:00Z",
  "exitPrice": 152.75,
  "grossPnL": 225.00,
  "netPnL": 225.00,
  "pnlPercentage": 1.49,
  "riskAmount": 50.00,
  "rewardAmount": 225.00,
  "riskRewardRatio": 4.5,
  "winLoss": "WIN",
  "tradingStrategy": "Breakout",
  "notes": "Good follow-through",
  "screenshots": [
    {
      "screenshotId": "uuid-string",
      "fileName": "trade-setup.png",
      "fileSize": 102400,
      "uploadDate": "2026-06-26T10:35:00Z"
    }
  ],
  "createdAt": "2026-06-26T10:30:00Z",
  "updatedAt": "2026-06-27T14:00:00Z"
}

Error Responses:
401 - Unauthorized
403 - Forbidden: User doesn't own this trade
404 - Not Found
```

### 4.4 Update Trade

```
PUT /trades/{tradeId}
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "exitDate": "2026-06-28T10:00:00Z",
  "exitPrice": 155.00,
  "notes": "Updated notes",
  "status": "CLOSED"
}

Response (200 OK):
{
  "tradeId": "uuid-string",
  "ticker": "AAPL",
  "exitDate": "2026-06-28T10:00:00Z",
  "exitPrice": 155.00,
  "grossPnL": 450.00,
  "netPnL": 450.00,
  "updatedAt": "2026-06-28T10:00:00Z"
}

Error Responses:
400 - Bad Request
401 - Unauthorized
403 - Forbidden
404 - Not Found
```

### 4.5 Delete Trade

```
DELETE /trades/{tradeId}
Authorization: Bearer {accessToken}

Response (204 No Content)

Error Responses:
401 - Unauthorized
403 - Forbidden
404 - Not Found
```

### 4.6 Get Trades by Ticker

```
GET /trades/ticker/{ticker}
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "ticker": "AAPL",
  "totalTrades": 25,
  "winRate": 64.0,
  "totalPnL": 5250.00,
  "trades": [
    {
      "tradeId": "uuid-string",
      "entryDate": "2026-06-26T10:30:00Z",
      "exitPrice": 152.75,
      "quantity": 100,
      "grossPnL": 225.00,
      "winLoss": "WIN"
    }
  ]
}

Error Responses:
401 - Unauthorized
```

---

## 5. Screenshot Management Endpoints

### 5.1 Upload Screenshot

```
POST /trades/{tradeId}/screenshots
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data

Form Data:
- file: [binary image file] (max 10MB, types: jpg, png, gif, webp)

Response (201 Created):
{
  "screenshotId": "uuid-string",
  "tradeId": "uuid-string",
  "fileName": "trade-setup-1234567890.png",
  "fileSize": 102400,
  "fileType": "image/png",
  "originalWidth": 1920,
  "originalHeight": 1080,
  "uploadDate": "2026-06-26T10:35:00Z"
}

Error Responses:
400 - Bad Request: Invalid file
401 - Unauthorized
404 - Not Found: Trade not found
413 - Payload Too Large: File exceeds 10MB
415 - Unsupported Media Type: Invalid image type
```

### 5.2 Get Trade Screenshots

```
GET /trades/{tradeId}/screenshots
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "tradeId": "uuid-string",
  "screenshots": [
    {
      "screenshotId": "uuid-string",
      "fileName": "trade-setup-1234567890.png",
      "fileSize": 102400,
      "fileType": "image/png",
      "originalWidth": 1920,
      "originalHeight": 1080,
      "uploadDate": "2026-06-26T10:35:00Z"
    }
  ]
}

Error Responses:
401 - Unauthorized
404 - Not Found
```

### 5.3 Download Screenshot

```
GET /screenshots/{screenshotId}/download
Authorization: Bearer {accessToken}

Response (200 OK):
Binary image data

Headers:
Content-Type: image/png
Content-Disposition: attachment; filename="trade-setup.png"
Content-Length: 102400

Error Responses:
401 - Unauthorized
404 - Not Found
```

### 5.4 Delete Screenshot

```
DELETE /screenshots/{screenshotId}
Authorization: Bearer {accessToken}

Response (204 No Content)

Error Responses:
401 - Unauthorized
403 - Forbidden
404 - Not Found
```

---

## 6. Statistics & Analytics Endpoints

### 6.1 Get Dashboard Overview

```
GET /dashboard/overview
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "totalTrades": 150,
  "openTrades": 5,
  "closedTrades": 145,
  "winRate": 64.13,
  "totalGrossPnL": 12500.00,
  "totalNetPnL": 12000.00,
  "averageWinPercentage": 2.50,
  "averageLossPercentage": -1.25,
  "profitFactor": 2.15,
  "largestWin": 750.00,
  "largestLoss": -300.00,
  "consecutiveWins": 3,
  "consecutiveLosses": 2
}

Error Responses:
401 - Unauthorized
```

### 6.2 Get Statistics Summary

```
GET /statistics/summary
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "userId": "uuid-string",
  "totalTrades": 150,
  "winningTrades": 96,
  "losingTrades": 54,
  "breakEvenTrades": 0,
  "winRate": 64.0,
  "totalGrossPnL": 12500.00,
  "totalNetPnL": 12000.00,
  "largestWin": 750.00,
  "largestLoss": -300.00,
  "averageWin": 130.21,
  "averageLoss": -222.22,
  "profitFactor": 2.15,
  "totalRiskAmount": 5600.00,
  "totalRewardAmount": 24150.00,
  "averageRiskRewardRatio": 4.31,
  "maxConsecutiveWins": 8,
  "maxConsecutiveLosses": 5,
  "lastCalculatedAt": "2026-06-26T10:30:00Z"
}

Error Responses:
401 - Unauthorized
```

### 6.3 Get Win Rate Analysis

```
GET /statistics/win-rate
Authorization: Bearer {accessToken}

Query Parameters:
- period: daily, weekly, monthly, yearly, all (default: all)
- dateFrom: ISO 8601 date
- dateTo: ISO 8601 date

Response (200 OK):
{
  "period": "monthly",
  "winRate": 64.0,
  "totalTrades": 150,
  "winningTrades": 96,
  "losingTrades": 54,
  "breakEvenTrades": 0,
  "trendData": [
    {
      "month": "2026-05",
      "trades": 25,
      "wins": 16,
      "losses": 9,
      "winRate": 64.0
    },
    {
      "month": "2026-06",
      "trades": 20,
      "wins": 13,
      "losses": 7,
      "winRate": 65.0
    }
  ]
}

Error Responses:
401 - Unauthorized
```

### 6.4 Get P&L Analysis

```
GET /statistics/pnl
Authorization: Bearer {accessToken}

Query Parameters:
- period: daily, weekly, monthly, yearly, all
- dateFrom: ISO 8601 date
- dateTo: ISO 8601 date

Response (200 OK):
{
  "period": "monthly",
  "totalGrossPnL": 12500.00,
  "totalNetPnL": 12000.00,
  "averageTradeSize": 80.00,
  "largestWin": 750.00,
  "largestLoss": -300.00,
  "averageWin": 130.21,
  "averageLoss": -222.22,
  "profitFactor": 2.15,
  "pnlPercentage": 2.40,
  "trendData": [
    {
      "date": "2026-05",
      "pnl": 3200.00,
      "trades": 25,
      "winRate": 64.0
    },
    {
      "date": "2026-06",
      "pnl": 1500.00,
      "trades": 20,
      "winRate": 65.0
    }
  ]
}

Error Responses:
401 - Unauthorized
```

### 6.5 Get Risk/Reward Analysis

```
GET /statistics/risk-reward
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "totalRiskAmount": 5600.00,
  "totalRewardAmount": 24150.00,
  "averageRiskRewardRatio": 4.31,
  "riskRewardDistribution": [
    {
      "ratio": "1:1",
      "count": 15,
      "percentage": 10.0
    },
    {
      "ratio": "1:2",
      "count": 35,
      "percentage": 23.3
    },
    {
      "ratio": "1:3+",
      "count": 100,
      "percentage": 66.7
    }
  ]
}

Error Responses:
401 - Unauthorized
```

### 6.6 Get Recent Trades

```
GET /dashboard/recent-trades?limit=5
Authorization: Bearer {accessToken}

Query Parameters:
- limit: Number of recent trades (default: 5, max: 20)

Response (200 OK):
{
  "trades": [
    {
      "tradeId": "uuid-string",
      "ticker": "AAPL",
      "positionType": "LONG",
      "entryDate": "2026-06-26T10:30:00Z",
      "exitDate": "2026-06-27T14:00:00Z",
      "entryPrice": 150.50,
      "exitPrice": 152.75,
      "quantity": 100,
      "grossPnL": 225.00,
      "winLoss": "WIN"
    }
  ]
}

Error Responses:
401 - Unauthorized
```

### 6.7 Get Performance History

```
GET /statistics/performance-history
Authorization: Bearer {accessToken}

Query Parameters:
- periodType: daily, weekly, monthly (default: monthly)
- dateFrom: ISO 8601 date
- dateTo: ISO 8601 date

Response (200 OK):
{
  "periodType": "monthly",
  "data": [
    {
      "period": "2026-05",
      "tradesCount": 25,
      "winsCount": 16,
      "lossesCount": 9,
      "winRate": 64.0,
      "periodPnL": 3200.00,
      "periodReturnPercentage": 2.50
    },
    {
      "period": "2026-06",
      "tradesCount": 20,
      "winsCount": 13,
      "lossesCount": 7,
      "winRate": 65.0,
      "periodPnL": 1500.00,
      "periodReturnPercentage": 1.25
    }
  ]
}

Error Responses:
401 - Unauthorized
```

---

## 7. Error Response Format

All error responses follow this standardized format:

```json
{
  "timestamp": "2026-06-26T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/trades",
  "errors": [
    {
      "field": "entryPrice",
      "message": "Entry price must be greater than 0",
      "rejectedValue": "-100"
    },
    {
      "field": "quantity",
      "message": "Quantity must be greater than 0",
      "rejectedValue": "0"
    }
  ]
}
```

---

## 8. HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET/PUT request |
| 201 | Created | Successful POST request (resource created) |
| 204 | No Content | Successful DELETE request |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing/invalid authentication |
| 403 | Forbidden | User doesn't have permission |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists (duplicate) |
| 413 | Payload Too Large | File too large for upload |
| 415 | Unsupported Media Type | Invalid file type |
| 422 | Unprocessable Entity | Validation failed |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Service temporarily down |

---

## 9. Authentication & Authorization

### JWT Token Structure

```
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "user-id",
  "username": "trader123",
  "role": "USER",
  "iat": 1624699800,
  "exp": 1624703400
}

Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

### Using JWT in Requests

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLWlkIiwibmFtZSI6IlRyYWRlciIsImlhdCI6MTUxNjIzOTAyMn0.signature
```

### Token Expiration & Refresh

- Access Token: 1 hour validity
- Refresh Token: 7 days validity
- Automatic renewal via `/auth/refresh-token`

---

## 10. Rate Limiting

```
Rate Limit: 1000 requests/hour per user
Rate Limit Headers:
  X-RateLimit-Limit: 1000
  X-RateLimit-Remaining: 950
  X-RateLimit-Reset: 1624703400

429 Response (Too Many Requests):
{
  "timestamp": "2026-06-26T10:30:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Retry after 3600 seconds"
}
```

---

## 11. CORS Configuration

```
Allowed Origins: http://localhost:3000 (dev), https://tradingjournal.com (prod)
Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: Content-Type, Authorization
Exposed Headers: X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset
Allow Credentials: true
Max Age: 3600 seconds
```

---

## 12. API Versioning

- Current Version: v1 (`/api/v1`)
- Version in URL path
- Future versions: `/api/v2`, `/api/v3`
- Backward compatibility maintained for at least 2 major versions

---

## 13. API Documentation

- Swagger UI available at: `http://localhost:8080/swagger-ui.html`
- OpenAPI specification: `http://localhost:8080/v3/api-docs`
- Interactive documentation with "Try it out" feature

