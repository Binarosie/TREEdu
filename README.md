# 🌱 TREEdu Backend – Materials Service

Backend service cho ứng dụng học **Tiếng Việt trực tuyến** TREEdu, cung cấp API để quản lý tài liệu học tập, quiz, flashcard và các tính năng học tập khác.

---

## ✨ Tính năng chính

- 📚 **Quản lý tài liệu học tập** – API cho bài học, quiz, flashcard
- 🔐 **Xác thực & Phân quyền** – OAuth2 Google login integration
- 🎯 **RESTful API** – Chuẩn REST API cho frontend
- 📊 **Database Management** – Quản lý dữ liệu học tập hiệu quả

---

## 🛠️ Công nghệ sử dụng

- ☕ **Java 17+** – Ngôn ngữ lập trình
- 🍃 **Spring Boot** – Framework chính
    - 🔒 **Spring Security** – Bảo mật và xác thực
    - 🔑 **OAuth2 Client** – Google OAuth2 login
    - 🗄️ **Spring Data JPA** – Tương tác database
    - 🌐 **Spring Web** – RESTful API
- 🐘 **MongoDBL** – Cơ sở dữ liệu quan hệ
- 📦 **Maven** – Dependency management
- 🐳 **Docker** (optional) – Containerization

---

## 📋 Yêu cầu hệ thống

- Java JDK 17 trở lên
- Maven 3.8+
- MongoDB
- IDE: IntelliJ IDEA / Eclipse / VS Code

---

## 🚀 Cài đặt & Chạy dự án

### 1️⃣ Clone repository

```bash
git clone https://github.com/Binarosie/TREEdu.git
```

### 2️⃣ Cấu hình Database

Tạo database trong MongoDB:

```sql
CREATE DATABASE treedu_db;
```

### 3️⃣ Cấu hình application.properties

Tạo file `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=mongodb://localhost:27017/treedu_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# Server Port
server.port=8080

# OAuth2 Google Configuration
spring.security.oauth2.client.registration.google.client-id=your-client-id
spring.security.oauth2.client.registration.google.client-secret=your-client-secret
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/google

# CORS Configuration
cors.allowed-origins=http://localhost:3000
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
│   ├── java/
│   │   └── vn/hcmute/edu/materialsservice/
│   │       ├── TreeduApplication.java    # Main application class
│   │       ├── config/                            # Cấu hình (Security, CORS, ...)
│   │       ├── controller/                        # REST API Controllers
│   │       ├── service/                           # Business Logic
│   │       ├── repository/                        # Data Access Layer
│   │       ├── model/                             # Model classes
│   │       ├── dto/                               # Data Transfer Objects
│   │       └── exception/                         # Exception handling
│   └── resources/
│       ├── application.properties                 # Cấu hình chính
└── test/                                          # Unit tests
```

---

## 🔌 API Endpoints (Ví dụ)

### Authentication

```
POST   /api/auth/login          # Login with credentials
GET    /oauth2/authorization/google  # Google OAuth2 login
GET    /api/auth/user           # Get current user info
```

### Materials Management

```
GET    /api/materials           # Lấy danh sách tài liệu
GET    /api/materials/{id}      # Lấy chi tiết tài liệu
POST   /api/materials           # Tạo tài liệu mới
PUT    /api/materials/{id}      # Cập nhật tài liệu
DELETE /api/materials/{id}      # Xóa tài liệu
```

### Quiz & Flashcard

```
GET    /api/quiz                # Lấy danh sách quiz
POST   /api/quiz                # Tạo quiz mới
GET    /api/flashcard           # Lấy danh sách flashcard
POST   /api/flashcard           # Tạo flashcard mới
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

## 🔒 Bảo mật

- ✅ Google OAuth2 authentication
- ✅ JWT token-based authorization (nếu có)
- ✅ CORS configuration
- ✅ SQL injection prevention (JPA/Hibernate)
- ✅ Password encryption (BCrypt)

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

## 👥 Team

- Trần Như Quỳnh - 22110218
- Bùi Lê Đông Quân - 22110213

---

## 📧 Liên hệ

- Email: 22110218@student.hcmute.edu.vn
- GitHub: Binarosie(https://github.com/Binarosie)

---

