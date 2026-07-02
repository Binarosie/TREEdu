# 🌱 TREEdu Backend – Materials Service

Backend service cho ứng dụng học **Tiếng Việt trực tuyến** TREEdu, cung cấp API để quản lý tài liệu học tập, quiz, flashcard và các tính năng học tập khác.

---

## ✨ Tính năng chính

### 📚 Học tập & Tài liệu

- **Flashcard Management** – Tạo, chỉnh sửa, xóa flashcard với từ vựng
- **Flashcard Learning** – Học từng flashcard với tiến trình theo dõi
- **Quiz System** – Tạo và làm quiz với nhiều loại câu hỏi
- **Dictation** – Bài tập nghe và nhập từ vựng
- **Pronunciation Check** – Kiểm tra phát âm tiếng Việt
- **Topic Management** – Quản lý các chuyên đề học tập

### 👥 Người dùng & Phân quyền

- **Member System** – Đăng ký, quản lý hồ sơ thành viên
- **OAuth2 Google Login** – Đăng nhập bằng tài khoản Google
- **Role-based Authorization** – Phân quyền Member, Supporter, Admin
- **User Profiles** – Quản lý thông tin, cấp độ, XP điểm

### 🔍 Báo cáo & Kiểm soát

- **Flashcard Reporting** – Báo cáo flashcard vi phạm
- **Review Request** – Supporter tạo yêu cầu review
- **Admin Review** – Admin phê duyệt hoặc từ chối review
- **Report Management** – Quản lý tất cả báo cáo

### 📊 Tìm kiếm & Analytics

- **Fuzzy Search** – Tìm kiếm mờ hỗ trợ tiếng Việt có dấu/không dấu
- **Dashboard Stats** – Thống kê số liệu học tập
- **Learning Progress Tracking** – Theo dõi tiến độ học tập

---

## 🛠️ Công nghệ sử dụng

### Backend

- ☕ **Java 17+** – Ngôn ngữ lập trình
- 🍃 **Spring Boot 3.x** – Framework chính
  - 🔒 **Spring Security** – Xác thực, phân quyền
  - 🔑 **Spring OAuth2 Client** – Google OAuth2 login
  - 🗄️ **Spring Data MongoDB** – Tương tác MongoDB
  - 🌐 **Spring Web** – RESTful API
  - ⏰ **Spring Scheduling** – Async tasks
- 🌿 **MapStruct** – DTO Mapping

### Database & Cloud

- 🌿 **MongoDB** – NoSQL Database
- ☁️ **Cloudinary** – Image hosting & processing
- 📦 **Maven** – Dependency management
- 🐳 **Docker & Docker Compose** – Containerization

### Frontend Web

- ⚛️ **React** – UI Library
- 🎨 **Material UI / Ant Design** – UI Components

### Frontend Mobile

- 📱 **React Native / Flutter** – Mobile app

---

## 📋 Yêu cầu hệ thống

- Java JDK 17 trở lên
- Maven 3.8+
- MongoDB
- IDE: IntelliJ IDEA / Eclipse / VS Code

---

## 🚀 Cài đặt & Chạy dự án

### 1️⃣ Clone repository

**Backend Service:**

```bash
git clone https://github.com/Binarosie/TREEdu.git
cd TREEdu
```

**Frontend Web Application:**

```bash
git clone https://github.com/DongQuan2212/FE-TREEdu.git
cd FE-TREEdu
```

**Frontend Mobile Application:**

```bash
git clone https://github.com/DongQuan2212/FE-App-TREEdu.git
cd FE-App-TREEdu
```

### 2️⃣ Cấu hình Database

Tạo database trong MongoDB:

```sql
CREATE DATABASE treedu_db;
```

### 3️⃣ Cấu hình application.properties

Tạo file `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080
spring.application.name=materials-service

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/treedu_db
spring.data.mongodb.database=treedu_db

# OAuth2 Google Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.provider.google.user-name-attribute=sub

# CORS Configuration
cors.allowed-origins=http://localhost:3000,http://localhost:3001
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*

# Cloudinary Configuration (Image Upload)
cloudinary.cloud-name=YOUR_CLOUDINARY_CLOUD_NAME
cloudinary.api-key=YOUR_CLOUDINARY_API_KEY
cloudinary.api-secret=YOUR_CLOUDINARY_API_SECRET

# Async Configuration
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10

# Logging
logging.level.root=INFO
logging.level.vn.hcmute.edu.materialsservice=DEBUG
```

### 4️⃣ Build project

```bash
mvn clean install
```

### 5️⃣ Chạy ứng dụng

```bash
mvn spring-boot:run
```

Hoặc chạy file JAR:

```bash
java -jar target/treedu-0.0.1-SNAPSHOT.jar
```

Server sẽ chạy tại: `http://localhost:8080`

---

## 📂 Cấu trúc thư mục

```
src/
├── main/
│   ├── java/vn/hcmute/edu/materialsservice/
│   │   ├── MaterialsServiceApplication.java       # Main application class
│   │   ├── configs/                               # Cấu hình
│   │   │   ├── AsyncConfig.java
│   │   │   ├── CloudinaryConfig.java              # Cloudinary (Image upload)
│   │   │   ├── CorsConfig.java                    # CORS configuration
│   │   │   ├── OAuth2LoginConfig.java             # OAuth2 configuration
│   │   │   └── WebConfig.java
│   │   ├── controllers/                           # REST API Controllers
│   │   │   ├── AuthController.java
│   │   │   ├── FlashcardController.java
│   │   │   ├── FlashcardLearningController.java
│   │   │   ├── DictationController.java
│   │   │   ├── QuizController.java
│   │   │   ├── PronunciationController.java
│   │   │   └── ...
│   │   ├── services/                              # Business Logic
│   │   │   ├── iFlashcardService.java
│   │   │   ├── iFlashcardLearningService.java
│   │   │   ├── iFlashcardReportService.java
│   │   │   ├── iFlashcardReviewService.java
│   │   │   ├── iPronunciationService.java
│   │   │   └── ...
│   │   ├── repository/                            # Data Access Layer (MongoDB)
│   │   │   ├── FlashcardRepository.java
│   │   │   ├── QuizRepository.java
│   │   │   ├── FlashcardReportRepository.java
│   │   │   └── ...
│   │   ├── models/                                # Entity Models
│   │   ├── dtos/                                  # Data Transfer Objects
│   │   │   ├── request/                           # Request DTOs
│   │   │   └── response/                          # Response DTOs
│   │   ├── Enum/                                  # Enumerations
│   │   ├── exceptions/                            # Custom Exceptions
│   │   ├── Mapper/                                # MapStruct Mappers
│   │   ├── security/                              # Security components
│   │   └── utils/                                 # Utility classes
│   │       └── FuzzySearchUtil.java               # Fuzzy search implementation
│   └── resources/
│       ├── application.properties                 # Main configuration
│       └── seeds/                                 # Database seed data
└── test/
    └── java/                                      # Unit & Integration tests
```

---

## 🔌 API Endpoints

### 🔐 Authentication & User

```
POST   /api/auth/login                  # Đăng nhập với email/password
GET    /oauth2/authorization/google     # Google OAuth2 login
GET    /api/auth/user                   # Lấy thông tin user hiện tại
POST   /api/auth/register               # Đăng ký thành viên mới
GET    /api/users/{id}                  # Lấy thông tin user
PUT    /api/users/{id}                  # Cập nhật hồ sơ user
```

### 📚 Flashcard Management

```
GET    /api/flashcards                  # Lấy danh sách flashcard
GET    /api/flashcards/{id}             # Chi tiết flashcard
POST   /api/flashcards                  # Tạo flashcard mới
PUT    /api/flashcards/{id}             # Cập nhật flashcard
DELETE /api/flashcards/{id}             # Xóa flashcard
GET    /api/flashcards/search           # Tìm kiếm flashcard (fuzzy)
```

### 📖 Flashcard Learning

```
GET    /api/flashcard-learning/progress/{id}     # Lấy tiến độ học
POST   /api/flashcard-learning/start/{id}        # Bắt đầu học flashcard
POST   /api/flashcard-learning/mark-viewed       # Đánh dấu word đã xem
POST   /api/flashcard-learning/submit-answer     # Submit câu trả lời
POST   /api/flashcard-learning/reset/{id}        # Reset tiến độ
GET    /api/flashcard-learning/all               # Lấy tất cả tiến độ
```

### 🎯 Quiz

```
GET    /api/quizzes                     # Lấy danh sách quiz
GET    /api/quizzes/{id}                # Chi tiết quiz
POST   /api/quizzes                     # Tạo quiz mới
PUT    /api/quizzes/{id}                # Cập nhật quiz
DELETE /api/quizzes/{id}                # Xóa quiz
POST   /api/quiz-attempts               # Bắt đầu làm quiz
POST   /api/quiz-attempts/{id}/submit   # Nộp bài quiz
```

### 🎤 Dictation & Pronunciation

```
GET    /api/dictations                  # Danh sách bài dictation
POST   /api/dictations/check            # Kiểm tra bài dictation
GET    /api/pronunciation/topics        # Danh sách chuyên đề phát âm
GET    /api/pronunciation/{id}          # Chi tiết chuyên đề
POST   /api/pronunciation/check         # Kiểm tra phát âm
```

### 📝 Flashcard Reporting

```
POST   /api/flashcard-reports           # Báo cáo flashcard vi phạm
GET    /api/flashcard-reports/pending   # Lấy báo cáo chờ xử lý (Supporter)
GET    /api/flashcard-reports/{id}      # Chi tiết báo cáo
PUT    /api/flashcard-reports/{id}      # Cập nhật trạng thái báo cáo
```

### ✅ Flashcard Review

```
POST   /api/review-requests             # Tạo yêu cầu review (Supporter)
GET    /api/review-requests/pending     # Lấy requests chờ xử lý (Admin)
PUT    /api/review-requests/{id}        # Phê duyệt/từ chối review (Admin)
```

---

## 🐳 Docker (Optional)

### Build Docker image

```bash
docker build -t treedu-backend .
```

### Run với Docker Compose

```bash
docker-compose up -d
```

---

## 🧪 Testing

Chạy unit tests:

```bash
mvn test
```

Chạy integration tests:

```bash
mvn verify
```

---

## � Project Documentation

- **Backend API** – TREEdu Backend Service (Materials Management)
- **Frontend Web** – TREEdu Web Application (React)
- **Frontend Mobile** – TREEdu Mobile App (React Native / Flutter)

---

## 🔒 Bảo mật

- ✅ Google OAuth2 authentication
- ✅ JWT token-based authorization
- ✅ CORS configuration
- ✅ Input validation & sanitization
- ✅ Password encryption (BCrypt)
- ✅ Role-based access control (RBAC)

---

## 📖 Feature Details

### Flashcard Learning Flow

1. **Tạo Flashcard** – Supporter/Admin tạo flashcard mới
2. **Đăng tải** – Phê duyệt và công khai flashcard
3. **Học** – Member học flashcard, lần lượt xem từng từ
4. **Kiểm tra** – Gõ từ và check đúng/sai
5. **Theo dõi** – Hệ thống lưu tiến độ học tập

### Quiz System

1. **Tạo Quiz** – Supporter/Admin tạo quiz
2. **Câu hỏi** – Hỗ trợ nhiều loại: multiple choice, short answer, etc.
3. **Làm Quiz** – Member làm quiz trong thời gian giới hạn
4. **Kết quả** – Hiển thị điểm, phân tích kết quả

### Fuzzy Search Implementation

- Tìm kiếm mờ với tiếng Việt có dấu/không dấu
- Sử dụng Levenshtein Distance algorithm
- Ưu tiên: exact match > starts with > contains > levenshtein
- Threshold: 40% similarity mặc định

### Pronunciation System

- Kiểm tra phát âm qua microphone
- Hỗ trợ tiếng Việt chuẩn
- Feedback chi tiết về lỗi phát âm

---

## 🤝 Contributing

1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

## � Quick Start

**Startup Backend:**

```bash
cd TREEdu
mvn clean install
mvn spring-boot:run
```

**Startup Frontend Web:**

```bash
cd FE-TREEdu
npm install
npm start
```

**Startup Frontend Mobile:**

```bash
cd FE-App-TREEdu
npm install
npm start
# or for development
expo start
```

---

## 🛠️ Environment Variables

Cấu hình các biến môi trường cần thiết:

```bash
# Backend
MONGODB_URI=mongodb://localhost:27017/treedu_db
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud
CLOUDINARY_API_KEY=your_cloudinary_key
CLOUDINARY_API_SECRET=your_cloudinary_secret

# Frontend
REACT_APP_API_URL=http://localhost:8080
REACT_APP_GOOGLE_CLIENT_ID=your_google_client_id
```

---

- Trần Như Quỳnh - 22110218
- Bùi Lê Đông Quân - 22110213

---

## 📧 Liên hệ

- Email: 22110218@student.hcmute.edu.vn
- GitHub: Binarosie(https://github.com/Binarosie)

---
