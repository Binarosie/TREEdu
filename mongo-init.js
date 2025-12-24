// Switch to materials_db database
db = db.getSiblingDB('materials_db');

// Create collections (optional, MongoDB creates them automatically)
db.createCollection('users');
db.createCollection('materials');
db.createCollection('quizzes');
db.createCollection('flashcards');

// Create indexes for better performance
db.users.createIndex({ "email": 1 }, { unique: true });
db.materials.createIndex({ "title": 1 });
db.quizzes.createIndex({ "materialId": 1 });
db.flashcards.createIndex({ "userId": 1 });

// Insert sample data (optional)
db.materials.insertOne({
    title: "Bài học 1: Chào hỏi",
    description: "Học cách chào hỏi bằng Tiếng Việt",
    level: "beginner",
    createdAt: new Date()
});

print('✅ MongoDB initialized successfully for TREEdu!');
print('📚 Database: materials_db');
print('📦 Collections created: users, materials, quizzes, flashcards');