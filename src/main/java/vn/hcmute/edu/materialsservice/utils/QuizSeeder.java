package vn.hcmute.edu.materialsservice.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.materialsservice.models.Answer;
import vn.hcmute.edu.materialsservice.models.Question;
import vn.hcmute.edu.materialsservice.models.Quiz;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizSeeder {

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    private static final String QUIZ_COLLECTION = "quiz"; // khớp @Document(collection = "quiz")

    public void seedQuizzes() {
        log.info("📝 [QuizSeeder] Seeding quizzes...");

        try {
            List<Map<String, Object>> rawList = loadJson("seeds/materials_db_quiz.json");
            int created = 0, skipped = 0;

            for (Map<String, Object> raw : rawList) {
                String id = extractOid(raw); // String, không phải ObjectId
                if (id == null) continue;

                if (mongoTemplate.exists(Query.query(Criteria.where("_id").is(id)), QUIZ_COLLECTION)) {
                    skipped++;
                    continue;
                }

                Quiz quiz = Quiz.builder()
                        .id(id)
                        .title((String) raw.get("title"))
                        .level(toInt(raw.get("level")))
                        .topic((String) raw.get("topic"))
                        .timer(toInt(raw.get("timer")))
                        .questionCount(toInt(raw.get("questionCount")))
                        .questions(parseQuestions(raw))
                        .deleted(false)
                        .build();

                mongoTemplate.save(quiz, QUIZ_COLLECTION);
                created++;
                log.info("  ✔ Quiz: {} (level {}, {} questions)",
                        quiz.getTitle(), quiz.getLevel(), quiz.getQuestions().size());
            }

            log.info("  📊 Quizzes — created: {}, skipped: {}", created, skipped);

        } catch (Exception e) {
            log.error("  ❌ Failed to seed quizzes", e);
        }

        log.info("✅ [QuizSeeder] Done.");
    }

    // -------------------------------------------------------------------------
    // Parse questions  →  List<Question>
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<Question> parseQuestions(Map<String, Object> raw) {
        List<Map<String, Object>> questionRaws =
                (List<Map<String, Object>>) raw.getOrDefault("questions", List.of());

        return questionRaws.stream().map(q ->
                Question.builder()
                        // questionId để UUID tự sinh (@Builder.Default)
                        .content((String) q.get("content"))
                        .explanation((String) q.getOrDefault("explanation", ""))
                        .options(parseAnswers(q))
                        .build()
        ).collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Parse options  →  List<Answer>
    // JSON field: "options": [ { "content": "...", "isCorrect": true } ]
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<Answer> parseAnswers(Map<String, Object> questionRaw) {
        List<Map<String, Object>> optionRaws =
                (List<Map<String, Object>>) questionRaw.getOrDefault("options", List.of());

        return optionRaws.stream().map(o ->
                Answer.builder()
                        // answerId để UUID tự sinh (@Builder.Default)
                        .content((String) o.get("content"))
                        .isCorrect(Boolean.TRUE.equals(o.get("isCorrect")))
                        .build()
        ).collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private String extractOid(Map<String, Object> raw) {
        Object idField = raw.get("_id");
        if (idField instanceof Map) {
            return (String) ((Map<String, Object>) idField).get("$oid");
        }
        return idField != null ? idField.toString() : null;
    }

    private int toInt(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private List<Map<String, Object>> loadJson(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<>() {});
        }
    }
}