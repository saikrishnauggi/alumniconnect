# 🏗️ AlumniConnect - Visual Architecture & Features

## System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                         ALUMNICONNECT PLATFORM                       │
└──────────────────────────────────────────────────────────────────────┘

                           ┌──────────────────┐
                           │   React Frontend │
                           │   (Port 5173)    │
                           │  - Login Page    │
                           │  - Dashboard     │
                           │  - Profile Mgmt  │
                           │  - Browse Alumni │
                           │  - Connections   │
                           │  - Notifications │
                           └────────┬─────────┘
                                    │ HTTPS + JWT
                                    ▼
                    ┌───────────────────────────────┐
                    │      API GATEWAY (8080)       │
                    │  ✓ JWT Validation             │
                    │  ✓ Request Routing            │
                    │  ✓ CORS Handling              │
                    │  ✓ Load Balancing             │
                    └───────┬───────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
   │    USER     │  │   PROFILE   │  │ CONNECTION  │
   │  SERVICE    │  │   SERVICE   │  │  SERVICE    │
   │  (8081)     │  │  (8082)     │  │  (8083)     │
   │             │  │             │  │             │
   │ • Register  │  │ • Create    │  │ • Send Req  │
   │ • Login     │  │   Profile   │  │ • Accept    │
   │ • JWT Auth  │  │ • Update    │  │ • Reject    │
   │ • Validate  │  │   Info      │  │ • List      │
   └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
          │                │                │
          ├────────────────┼────────────────┤
          │                │                │
        ┌─┴──────┐       ┌─┴──────┐      ┌─┴──────┐
        │ MySQL  │       │ MySQL  │      │ MySQL  │
        │ (User) │       │(Profile)│     │(Conn.) │
        └────────┘       └────────┘      └────────┘

        ┌──────────────┐        ┌──────────────────┐
        │NOTIFICATION │        │  DISCOVERY SERVER│
        │  SERVICE     │        │   (Eureka 8761)  │
        │   (8084)     │        │                  │
        │              │        │ • Service        │
        │ • Send alerts│        │   Registry       │
        │ • Track      │        │ • Health checks  │
        │   events     │        │ • Load balancing │
        └──────┬───────┘        └──────────────────┘
               │
             ┌─┴──────┐
             │ MySQL  │
             │(Notif.)│
             └────────┘
```

---

## User Journey Flow

```
┌─────────────┐
│   New User  │
└──────┬──────┘
       │
       ▼
┌──────────────────────┐
│  Visit Application   │
│  (localhost:5173)    │
└──────┬───────────────┘
       │
       ▼
┌──────────────────────────────┐
│ Choose: Login or Register    │
└──────┬───────────────────────┘
       │
       ├─────────────────────────────────┐
       │                                 │
       ▼                                 ▼
┌──────────────────────┐    ┌──────────────────────┐
│  Registration Flow   │    │   Login Flow         │
│ 1. API Gateway       │    │ 1. API Gateway       │
│ 2. User Service      │    │ 2. User Service      │
│    (Create user)     │    │    (Validate creds)  │
│ 3. Return JWT Token  │    │ 3. Return JWT Token  │
└──────┬───────────────┘    └────────┬─────────────┘
       │                            │
       └──────────────┬─────────────┘
                      │
                      ▼
             ┌──────────────────────┐
             │ Dashboard Page       │
             │ (Authenticated)      │
             └────────┬─────────────┘
                      │
         ┌────────────┼────────────┐
         │            │            │
         ▼            ▼            ▼
   ┌─────────┐  ┌──────────┐ ┌──────────┐
   │View My  │  │ Browse   │ │  Send    │
   │Profile  │  │ Alumni   │ │Requests  │
   └────┬────┘  └────┬─────┘ └────┬─────┘
        │            │            │
        ▼            ▼            ▼
   [Profile Svc] [Conn. Svc] [Conn. Svc]
        │            │            │
        └────────────┴────────────┘
                     │
                     ▼
         [Notification Service]
              Alerts user of:
         • New connection requests
         • Profile views
         • Network activity
```

---

## Feature Matrix by Service

| Feature | Service | DB | Status |
|---------|---------|-----|--------|
| User Registration | User Service | user_db | ✅ |
| User Login | User Service | user_db | ✅ |
| JWT Token Generation | User Service | user_db | ✅ |
| Create/Update Profile | Profile Service | profile_db | ✅ |
| View Alumni Profiles | Profile Service | profile_db | ✅ |
| Send Connection Request | Connection Service | connection_db | ✅ |
| Accept/Reject Requests | Connection Service | connection_db | ✅ |
| View Connections | Connection Service | connection_db | ✅ |
| Send Notifications | Notification Service | notif_db | ✅ |
| Track User Activity | Notification Service | notif_db | ✅ |

---

## Request/Response Flow Example

### 1. Login Request
```
User Input: email + password
          ↓
API Gateway (validates format)
          ↓
User Service (validates credentials)
          ↓
JWT Token Generated
          ↓
Response: Token + User Info
          ↓
Frontend: Store token in context
          ↓
All future requests: Authorization: Bearer <token>
```

### 2. View Alumni Profile
```
Frontend: Fetch /api/profile/{id}
          ↓
API Gateway (verify JWT)
          ↓
Profile Service (authorize user)
          ↓
Query profile_db
          ↓
Return profile data
          ↓
Frontend: Display profile
```

### 3. Send Connection Request
```
Frontend: POST /api/connections/request
          ↓
API Gateway (verify JWT)
          ↓
Connection Service
          ├─ Create connection record
          ├─ Update connection_db
          └─ Trigger notification
                    ↓
            Notification Service
                    ↓
            Alert recipient user
```

---

## Technology Stack Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND LAYER                           │
│  React 19 | Vite | React Router | Axios | CSS              │
│  Running on: http://localhost:5173                          │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP/REST + JWT
┌──────────────────────┴──────────────────────────────────────┐
│                    API GATEWAY LAYER                        │
│  Spring Cloud Gateway | JWT Filter | CORS | Load Balance   │
│  Running on: http://localhost:8080                          │
└──────┬───────────────┬──────────────┬──────────────┬────────┘
       │               │              │              │
┌──────▼──┐    ┌───────▼───┐  ┌──────▼──┐  ┌───────▼────┐
│ User    │    │ Profile   │  │Connection│  │Notification│
│ Service │    │ Service   │  │ Service  │  │ Service    │
│ Port    │    │ Port      │  │ Port     │  │ Port       │
│ 8081    │    │ 8082      │  │ 8083     │  │ 8084       │
└────┬────┘    └────┬──────┘  └────┬─────┘  └────┬───────┘
     │             │              │             │
     │  ┌──────────┴──────────────┴─────────────┤
     │  │                                       │
     ▼  ▼                                       ▼
┌────────────────────────────────────────────────────────┐
│         SERVICE DISCOVERY LAYER (Eureka)              │
│  Service Registry | Health Checks | Load Balancing    │
│  Running on: http://localhost:8761                    │
└────────────────────────────────────────────────────────┘

┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐
│ MySQL  │  │ MySQL  │  │ MySQL  │  │ MySQL  │
│ User   │  │Profile │  │Conn.   │  │Notif.  │
│ DB     │  │ DB     │  │ DB     │  │ DB     │
└────────┘  └────────┘  └────────┘  └────────┘
```

---

## Environment & Ports

```
┌─────────────────────────────────────────────────────┐
│          LOCAL DEVELOPMENT ENVIRONMENT              │
├─────────────────────────────────────────────────────┤
│ Frontend      → localhost:5173                      │
│ API Gateway   → localhost:8080                      │
│ User Service  → localhost:8081                      │
│ Profile Svc   → localhost:8082                      │
│ Connection Svc→ localhost:8083                      │
│ Notification  → localhost:8084                      │
│ Eureka Server → localhost:8761                      │
│ MySQL         → localhost:3306                      │
├─────────────────────────────────────────────────────┤
│ Technologies:                                       │
│ • Java 21 / Spring Boot 3.x                         │
│ • React 19 / Vite                                   │
│ • MySQL 8.0                                         │
│ • Spring Cloud (Eureka, Gateway)                    │
│ • Maven & npm                                       │
└─────────────────────────────────────────────────────┘
```

---

## Key Design Principles

### 1. Single Responsibility
```
❌ User-Profile-Connection-Notification Monolith
✅ Each service owns ONE business capability
```

### 2. Database per Service
```
❌ All services → 1 shared database
✅ Each service → Own database
   Result: Independent scaling & evolution
```

### 3. API Gateway Pattern
```
❌ Frontend knows all service endpoints
✅ Frontend → 1 Gateway → N Services
   Result: Simplified client, centralized security
```

### 4. Service Discovery
```
❌ Hardcoded IP addresses
✅ Services register with Eureka
   Result: Dynamic scaling, auto health checks
```

### 5. Stateless Authentication
```
❌ Session-based (server maintains state)
✅ JWT tokens (stateless, scalable)
   Result: Can scale servers independently
```

---

## Why This Architecture?

| Challenge | Solution | Benefit |
|-----------|----------|---------|
| Monolith gets too large | Microservices | Easier to understand & maintain |
| Can't scale User Service | Independent deployment | Scale what needs scaling |
| Hard to find services | Service Discovery (Eureka) | Dynamic routing |
| Many clients break on change | API Gateway | Single contract point |
| Session state limits scale | JWT tokens | True horizontal scaling |
| All requests hit one point | Load balancing | Distribute load evenly |
| Database bottleneck | Database per service | Independent data models |
| Security scattered | API Gateway auth | Central security layer |

---

## Quick Start Commands

```bash
# Terminal 1: Start Discovery Server (Eureka)
cd discovery-server
mvn spring-boot:run
# Access: http://localhost:8761

# Terminal 2: Start User Service
cd user-service
mvn spring-boot:run
# Runs on: http://localhost:8081

# Terminal 3: Start Profile Service
cd profile-service
mvn spring-boot:run
# Runs on: http://localhost:8082

# Terminal 4: Start Connection Service
cd connection-service
mvn spring-boot:run
# Runs on: http://localhost:8083

# Terminal 5: Start Notification Service
cd notification-service
mvn spring-boot:run
# Runs on: http://localhost:8084

# Terminal 6: Start API Gateway
cd api-gateway
mvn spring-boot:run
# Runs on: http://localhost:8080 (Access here!)

# Terminal 7: Start Frontend
cd alumniconnect-frontend
npm install
npm run dev
# Access: http://localhost:5173
```

---

## Testing the System

```
1. Open Frontend: http://localhost:5173
2. Register new account
3. Login with credentials
4. Create/Update profile
5. Browse other alumni
6. Send connection requests
7. Accept/Reject requests
8. Check notifications
9. Verify data in MySQL
10. Monitor in Eureka: http://localhost:8761
```

---

## Performance Metrics (Expected)

| Metric | Expected | Notes |
|--------|----------|-------|
| Startup Time | ~10-15 sec per service | Normal for Spring Boot |
| API Response | <200ms | With local MySQL |
| JWT Validation | <5ms | Very fast |
| Service Discovery | <100ms | Eureka lookup |
| Database Query | <50ms | Well-indexed |
| Full Round Trip | <500ms | End-to-end response |

---

## Scaling Scenarios

**Scenario 1: 10K Alumni Browse Daily**
- ✅ Scale Profile Service (add instances)
- ✅ Gateway load-balances requests
- ✅ Each instance has its own connection pool
- ✅ MySQL reads can be replicated

**Scenario 2: Peak Connection Requests**
- ✅ Scale Connection Service independently
- ✅ Notification Service handles surge
- ✅ Other services unaffected
- ✅ Total resource cost: minimal

**Scenario 3: Authentication Bottleneck**
- ✅ Scale User Service
- ✅ JWT validation is stateless
- ✅ No session synchronization needed
- ✅ Linear scaling possible

---

## Success Indicators

✅ All services register with Eureka
✅ Frontend loads without CORS errors
✅ Registration/Login works
✅ Can create and update profiles
✅ Connection requests work
✅ Notifications are sent
✅ API Gateway routes all requests
✅ No service-to-service failures
✅ Database data persists
✅ System handles 100+ concurrent users

**🎉 When all of the above work → AlumniConnect is ready for production!**
