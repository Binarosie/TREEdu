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
import vn.hcmute.edu.materialsservice.Enum.EFlashcardType;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardVisibility;
import vn.hcmute.edu.materialsservice.Enum.EWordForm;
import vn.hcmute.edu.materialsservice.models.Flashcard;
import vn.hcmute.edu.materialsservice.models.Word;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashcardSeeder {

        private final MongoTemplate mongoTemplate;
        private final ObjectMapper objectMapper;

        private static final String FLASHCARD_COLLECTION = "flashcards";
        private static final String WORD_COLLECTION      = "words";

        public void seedFlashcardsAndWords() {
                log.info("🃏 [FlashcardSeeder] Seeding flashcards and words...");
                seedFlashcards();
                seedWords();
                log.info("✅ [FlashcardSeeder] Done.");
        }

        // -------------------------------------------------------------------------
        // Flashcards
        // -------------------------------------------------------------------------

        private void seedFlashcards() {
                try {
                        List<Map<String, Object>> rawList = loadJson("seeds/materials_db_flashcards.json");
                        int created = 0, skipped = 0;

                        for (Map<String, Object> raw : rawList) {
                                // id là String (hex ObjectId) lấy từ $oid
                                String id = extractOid(raw);
                                if (id == null) continue;

                                // Skip nếu đã tồn tại
                                if (mongoTemplate.exists(Query.query(Criteria.where("_id").is(id)), FLASHCARD_COLLECTION)) {
                                        skipped++;
                                        continue;
                                }

                                Flashcard flashcard = Flashcard.builder()
                                        .id(id)
                                        .title((String) raw.get("title"))
                                        .description((String) raw.get("description"))
                                        .level(toInt(raw.get("level")))
                                        .topic((String) raw.get("topic"))
                                        .type(parseEnum(EFlashcardType.class, (String) raw.get("type")))
                                        .createdBy((String) raw.get("createdBy"))
                                        .visibility(parseEnum(EFlashcardVisibility.class, (String) raw.get("visibility")))
                                        .deleted(Boolean.TRUE.equals(raw.get("deleted")))
                                        .build();

                                mongoTemplate.save(flashcard, FLASHCARD_COLLECTION);
                                created++;
                                log.info("  ✔ Flashcard: {}", flashcard.getTitle());
                        }

                        log.info("  📊 Flashcards — created: {}, skipped: {}", created, skipped);

                } catch (Exception e) {
                        log.error("  ❌ Failed to seed flashcards", e);
                }
        }

        // -------------------------------------------------------------------------
        // Words
        // -------------------------------------------------------------------------

        private void seedWords() {
                try {
                        List<Map<String, Object>> rawList = loadJson("seeds/materials_db_words.json");
                        int created = 0, skipped = 0;

                        for (Map<String, Object> raw : rawList) {
                                String id = extractOid(raw);
                                if (id == null) continue;

                                if (mongoTemplate.exists(Query.query(Criteria.where("_id").is(id)), WORD_COLLECTION)) {
                                        skipped++;
                                        continue;
                                }

                                Word word = Word.builder()
                                        .id(id)
                                        .flashcardId((String) raw.get("flashcardId"))
                                        .newWord((String) raw.get("newWord"))
                                        .meaning((String) raw.get("meaning"))
                                        .wordForm(parseEnum(EWordForm.class, (String) raw.get("wordForm")))
                                        .phoneme((String) raw.get("phoneme"))
                                        .audioURL((String) raw.getOrDefault("audioURL", ""))
                                        .build();

                                mongoTemplate.save(word, WORD_COLLECTION);
                                created++;
                        }

                        log.info("  📊 Words — created: {}, skipped: {}", created, skipped);

                } catch (Exception e) {
                        log.error("  ❌ Failed to seed words", e);
                }
        }

        // -------------------------------------------------------------------------
        // Helpers
        // -------------------------------------------------------------------------

        private List<Map<String, Object>> loadJson(String classpathPath) throws Exception {
                ClassPathResource resource = new ClassPathResource(classpathPath);
                try (InputStream is = resource.getInputStream()) {
                        return objectMapper.readValue(is, new TypeReference<>() {});
                }
        }

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

        /**
         * Parse String → Enum an toàn, trả về null nếu không tìm thấy thay vì throw exception.
         */
        private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
                if (value == null) return null;
                try {
                        return Enum.valueOf(enumClass, value.toUpperCase());
                } catch (IllegalArgumentException e) {
                        log.warn("  ⚠ Unknown enum value '{}' for {}, setting null", value, enumClass.getSimpleName());
                        return null;
                }
        }
}