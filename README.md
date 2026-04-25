# guardrails

#  Guardrails Backend Engineering Assignment

##  Overview

This project is a **Spring Boot microservice** that acts as a central API gateway and guardrail system. It ensures **high-performance, concurrency-safe operations** using Redis and PostgreSQL.

The system is designed to:

* Handle concurrent bot/user interactions
* Enforce strict guardrails using Redis
* Maintain data integrity with PostgreSQL
* Prevent notification spam via batching

---

##  Tech Stack

* **Java 21**
* **Spring Boot 3.x**
* **PostgreSQL** (Primary Database)
* **Redis** (Caching + Guardrails + Atomic Locks)
* **Docker Compose**

---

##  Setup Instructions

### 1. Clone Repository

```bash
git clone https://github.com/SivaReddy-190/guardrails.git
cd guardrails
```

### 2. Start Services (Postgres + Redis)

```bash
docker-compose up -d
```

### 3. Run Spring Boot App

```bash
./mvnw spring-boot:run
```

---

##  Database Schema

### Entities:

* **User**: id, username, is_premium
* **Bot**: id, name, persona_description
* **Post**: id, author_id, content, created_at
* **Comment**: id, post_id, author_id, content, depth_level, created_at

---

##  API Endpoints

### 1. Create Post

```
POST /api/posts
```

### 2. Add Comment

```
POST /api/posts/{postId}/comments
```

### 3. Like Post

```
POST /api/posts/{postId}/like
```

---

## ⚡ Redis Guardrails (Core Logic)

### 🔹 1. Virality Score (Real-Time)

Each interaction updates score instantly:

| Action        | Points |
| ------------- | ------ |
| Bot Reply     | +1     |
| Human Like    | +20    |
| Human Comment | +50    |

**Implementation:**

```
INCRBY post:{id}:virality_score <points>
```

---

### 🔹 2. Horizontal Cap (Bot Limit)

* Max **100 bot replies per post**

```
INCR post:{id}:bot_count
```

If count > 100 →  Reject request (HTTP 429)

---

### 🔹 3. Vertical Cap (Thread Depth)

* Max depth = **20**

```
if (depth_level > 20) reject
```

---

### 🔹 4. Cooldown Cap (Bot-Human Interaction)

* A bot cannot interact with same user within **10 minutes**

```
SET cooldown:bot_{id}:user_{id} 1 EX 600 NX
```

If key exists →  Reject interaction

---

##  Thread Safety (Very Important)

All critical operations use **Redis atomic commands**:

* `INCR` → atomic counter updates
* `SET NX` → ensures uniqueness
* TTL ensures automatic expiration

 This guarantees:

* No race conditions
* Correct handling of 200 concurrent requests
* Horizontal cap strictly enforced at 100

---

##  Notification Engine

### 🔹 Redis Throttler

* Prevents notification spam

If user received notification recently:

```
LPUSH user:{id}:pending_notifs "message"
```

Else:

* Send notification immediately
* Set cooldown (15 mins)

---

### 🔹 CRON Scheduler

Runs every **5 minutes**:

* Fetch pending notifications
* Aggregate messages
* Log summary:

```
"Bot X and N others interacted with your posts"
```

* Clear Redis list

---

##  Testing & Edge Cases

###  1. Race Condition Test

* Simulated **200 concurrent bot requests**
* Redis ensured only **100 comments allowed**

---

###  2. Statelessness

* No in-memory storage (no HashMaps/static variables)
* All state managed via Redis

---

###  3. Data Integrity

* PostgreSQL = source of truth
* Redis = gatekeeper
* DB writes only after Redis validation

---
## Author

**M Siva Prasad Reddy**
