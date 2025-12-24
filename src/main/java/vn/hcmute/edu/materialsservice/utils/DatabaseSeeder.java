package vn.hcmute.edu.materialsservice.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {

    private final UserSeeder userSeeder;
    private final TopicSeeder topicSeeder;
    private final FlashcardSeeder flashcardSeeder;
    private final QuizSeeder quizSeeder;

    @PostConstruct
    public void seedAll() {
        log.info("🌱 Starting database seeding...");

        userSeeder.seedUsers();
        topicSeeder.seedTopics();
        flashcardSeeder.seedFlashcardsAndWords();
        quizSeeder.seedQuizzes();

        log.info("✅ Database seeded successfully!");
    }
}
