# School-Equipment-Lending-Portal
A web-based platform to efficiently manage and track the borrowing, approval, issuing, and return of shared school assets (e.g., sports kits, lab equipment, musical instruments) for students and staff.

Excellent approach 👏 — you’re now focusing on **Task 1 (Oct 16–17)**:

> ✅ **Read assignment → summarize use cases**
> ✅ **Finalize architecture diagram, tech stack, and DB schema (ERD + relationships)**

We’ll go step-by-step.

---

## 🧩 Step 1 — Summary of the Assignment (Functional + Non-functional)

### 🎯 **Objective**

Build a **School Equipment Lending Portal** for managing equipment requests, approvals, and returns for students and staff.

### 👥 **User Roles**

1. **Student**

   * View available equipment
   * Request to borrow items
   * View request status
   * Return borrowed items

2. **Staff**

   * Approve or reject student borrow requests
   * Manage borrowing schedule (avoid overlaps)
   * Mark equipment as returned

3. **Admin**

   * Add/edit/delete equipment
   * Manage user roles (student/staff/admin)
   * Monitor usage and availability

---

### ⚙️ **Core Use Cases**

| Use Case                             | Actor       | Description                                                        |
| ------------------------------------ | ----------- | ------------------------------------------------------------------ |
| **UC1 – User Registration & Login**  | All users   | Sign up or login (JWT auth); roles: Student/Staff/Admin            |
| **UC2 – View Equipment List**        | All         | View all available equipment with filters (category, availability) |
| **UC3 – Request Equipment**          | Student     | Submit a borrow request with start & end dates                     |
| **UC4 – Approve/Reject Request**     | Staff/Admin | Approve or reject requests                                         |
| **UC5 – Issue/Return Equipment**     | Staff/Admin | Mark item as issued/returned                                       |
| **UC6 – Manage Equipment Inventory** | Admin       | CRUD operations for equipment (add/edit/delete)                    |
| **UC7 – Prevent Overlaps**           | System      | Ensure no two active borrow requests for the same item and time    |
| **UC8 – Notifications (optional)**   | System      | Send overdue or approval notifications via RabbitMQ/Kafka          |
| **UC9 – Analytics (optional)**       | Admin       | Show request history or usage dashboard                            |

---

### 🧠 **Non-functional Requirements**

* Secure access (JWT, OAuth2.0)
* Scalable backend (Spring Boot, microservice-ready)
* Responsive UI (React)
* Cloud deployable (AWS)
* Resilient & observable (Circuit Breaker, Monitoring)

---

## 🧱 Step 2 — Tech Stack Proposal

| Layer                       | Technology                                             | Purpose                                    |
| --------------------------- | ------------------------------------------------------ | ------------------------------------------ |
| **Frontend**                | React + TailwindCSS                                    | UI, routing, API integration               |
| **Backend**                 | Spring Boot (Java 21)                                  | REST API, business logic                   |
| **Database**                | PostgreSQL                                             | Relational DB, transactions, AWS RDS ready |
| **Authentication**          | Spring Security + JWT + OAuth2 (Google login optional) | Secure access                              |
| **Messaging**               | RabbitMQ / Kafka                                       | Notifications, async updates               |
| **Resilience**              | Resilience4J + Saga Pattern                            | Fault tolerance                            |
| **Monitoring**              | Spring Boot Actuator + Prometheus + Grafana            | Health & metrics                           |
| **Cloud**                   | AWS Elastic Beanstalk / EC2 + RDS                      | Deployment & hosting                       |
| **AI-Assistance (Phase 2)** | GitHub Copilot / Cursor                                | Refactor, test generation, code assist     |

---

## 🗺️ Step 3 — High-Level Architecture (Concept)

**Actors:** Student / Staff / Admin
**Frontend (React)** → communicates via REST APIs → **Spring Boot Backend**
Backend connects to **PostgreSQL DB** and **Message Broker (RabbitMQ/Kafka)**
Deployed on **AWS**, monitored via **Prometheus + Grafana**

```
[React Frontend] 
    ↓
[Spring Boot Backend]
    ├── Auth Module (JWT + OAuth2)
    ├── Equipment Service
    ├── BorrowRequest Service
    ├── Notification Service (Kafka/RabbitMQ)
    ├── Monitoring (Actuator)
    ↓
[PostgreSQL DB] ←→ [AWS Cloud / RDS]
```

---

## 🗃️ Step 4 — Draft Database Schema (ERD Overview)

**Entities:**

1. **User**

   * user_id (PK)
   * name
   * email
   * password
   * role (`STUDENT`, `STAFF`, `ADMIN`)

2. **Equipment**

   * equipment_id (PK)
   * name
   * category
   * condition
   * quantity
   * available_quantity

3. **BorrowRequest**

   * request_id (PK)
   * user_id (FK → User)
   * equipment_id (FK → Equipment)
   * request_date
   * start_date
   * end_date
   * status (`PENDING`, `APPROVED`, `REJECTED`, `RETURNED`)

4. **Notification** *(optional via Kafka/RabbitMQ)*

   * notification_id (PK)
   * user_id (FK)
   * message
   * type (`OVERDUE`, `APPROVED`, etc.)
   * created_at

**Relationships:**

* User ↔ BorrowRequest → Equipment (Many-to-One)
* Equipment → BorrowRequest (One-to-Many)
* User → Notification (One-to-Many)

---

## 🧭 Step 5 — Next Action (for You)

Let’s proceed collaboratively.
Please complete this step 👇:

👉 **Your Action:**
Propose your version of **architecture** and **ERD** — just describe:

1. Any extra modules you want (e.g., DamageLog, Analytics)
2. Any change in database entities or relationships (e.g., include EquipmentCategory separately)
3. Whether you want to use **Kafka** or **RabbitMQ** for async messaging.

Once you describe your design choice, I’ll refine it, improve schema relationships, and generate:

* ✅ A polished **architecture diagram (UML)**
* ✅ A clear **ERD diagram (visual)**

Would you like me to include **due date tracking** (auto overdue detection) in the design too?
