package vn.hcmute.edu.materialsservice.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.materialsservice.Enum.WordForm;
import vn.hcmute.edu.materialsservice.Model.Flashcard;
import vn.hcmute.edu.materialsservice.Model.Word;
import vn.hcmute.edu.materialsservice.Repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.Repository.WordRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashcardSeeder {

        private final FlashcardRepository flashcardRepository;
        private final WordRepository wordRepository;

        public void seedFlashcardsAndWords() {
                if (flashcardRepository.count() > 0) {
                        log.info("⏭️ Flashcards already exist, skipping flashcard seeding");
                        return;
                }

                log.info("🌱 Seeding flashcards and words...");

                // Flashcard 1: Chào hỏi cơ bản
                Flashcard flashcard1 = Flashcard.builder()
                                .title("Chào hỏi cơ bản")
                                .description("Các từ và cụm từ chào hỏi thông dụng")
                                .level(1)
                                .topic("Chào hỏi")
                                .type(vn.hcmute.edu.materialsservice.Enum.FlashcardType.SYSTEM)
                                .createdBy(null)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                flashcard1 = flashcardRepository.save(flashcard1);

                List<Word> words1 = Arrays.asList(
                                Word.builder()
                                                .flashcardId(flashcard1.getId())
                                                .newWord("Xin chào")
                                                .meaning("Lời chào hỏi lịch sự, thân thiện")
                                                .wordForm(WordForm.PHRASE)
                                                .phoneme("/sin caːw/")
                                                .imageURL("https://example.com/images/xinchao.jpg")
                                                .audioURL("https://example.com/audio/xinchao.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build(),
                                Word.builder()
                                                .flashcardId(flashcard1.getId())
                                                .newWord("Cảm ơn")
                                                .meaning("Lời cảm ơn, bày tỏ lòng biết ơn")
                                                .wordForm(WordForm.PHRASE)
                                                .phoneme("/kaːm ɔn/")
                                                .imageURL("https://example.com/images/camon.jpg")
                                                .audioURL("https://example.com/audio/camon.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build(),
                                Word.builder()
                                                .flashcardId(flashcard1.getId())
                                                .newWord("Tạm biệt")
                                                .meaning("Lời chào khi chia tay")
                                                .wordForm(WordForm.PHRASE)
                                                .phoneme("/taːm biət/")
                                                .imageURL("https://example.com/images/tambiet.jpg")
                                                .audioURL("https://example.com/audio/tambiet.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build());
                wordRepository.saveAll(words1);

                // Flashcard 2: Động từ thường dùng
                Flashcard flashcard2 = Flashcard.builder()
                                .title("Động từ thường dùng")
                                .description("Các động từ hay gặp trong cuộc sống hàng ngày")
                                .level(1)
                                .topic("Hoạt động hàng ngày")
                                .type(vn.hcmute.edu.materialsservice.Enum.FlashcardType.SYSTEM)
                                .createdBy(null)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                flashcard2 = flashcardRepository.save(flashcard2);

                List<Word> words2 = Arrays.asList(
                                Word.builder()
                                                .flashcardId(flashcard2.getId())
                                                .newWord("Ăn")
                                                .meaning("Nhai và nuốt thức ăn")
                                                .wordForm(WordForm.VERB)
                                                .phoneme("/aːn/")
                                                .imageURL("https://example.com/images/an.jpg")
                                                .audioURL("https://example.com/audio/an.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build(),
                                Word.builder()
                                                .flashcardId(flashcard2.getId())
                                                .newWord("Ngủ")
                                                .meaning("Nghỉ ngơi bằng cách nhắm mắt và để não hoạt động chậm")
                                                .wordForm(WordForm.VERB)
                                                .phoneme("/ŋuː/")
                                                .imageURL("https://example.com/images/ngu.jpg")
                                                .audioURL("https://example.com/audio/ngu.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build(),
                                Word.builder()
                                                .flashcardId(flashcard2.getId())
                                                .newWord("Học")
                                                .meaning("Tiếp thu kiến thức hoặc kỹ năng")
                                                .wordForm(WordForm.VERB)
                                                .phoneme("/hɔk/")
                                                .imageURL("https://example.com/images/hoc.jpg")
                                                .audioURL("https://example.com/audio/hoc.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build(),
                                Word.builder()
                                                .flashcardId(flashcard2.getId())
                                                .newWord("Làm")
                                                .meaning("Thực hiện một công việc, hành động")
                                                .wordForm(WordForm.VERB)
                                                .phoneme("/laːm/")
                                                .imageURL("https://example.com/images/lam.jpg")
                                                .audioURL("https://example.com/audio/lam.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build());
                wordRepository.saveAll(words2);

                // Flashcard 3: Tính từ miêu tả
                Flashcard flashcard3 = Flashcard.builder()
                                .title("Tính từ miêu tả")
                                .description("Các tính từ thường dùng để miêu tả sự vật")
                                .level(1)
                                .topic("Chào hỏi")
                                .type(vn.hcmute.edu.materialsservice.Enum.FlashcardType.SYSTEM)
                                .createdBy(null)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                flashcard3 = flashcardRepository.save(flashcard3);

                List<Word> words3 = Arrays.asList(
                                Word.builder()
                                                .flashcardId(flashcard3.getId())
                                                .newWord("Đẹp")
                                                .meaning("Có hình dáng, màu sắc đẹp mắt, dễ nhìn")
                                                .wordForm(WordForm.ADJECTIVE)
                                                .phoneme("/ɗɛp/")
                                                .imageURL("https://example.com/images/dep.jpg")
                                                .audioURL("https://example.com/audio/dep.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build(),
                                Word.builder()
                                                .flashcardId(flashcard3.getId())
                                                .newWord("Tốt")
                                                .meaning("Có phẩm chất cao, tốt đẹp")
                                                .wordForm(WordForm.ADJECTIVE)
                                                .phoneme("/tot/")
                                                .imageURL("https://example.com/images/tot.jpg")
                                                .audioURL("https://example.com/audio/tot.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build(),
                                Word.builder()
                                                .flashcardId(flashcard3.getId())
                                                .newWord("Lớn")
                                                .meaning("Có kích thước, quy mô cao")
                                                .wordForm(WordForm.ADJECTIVE)
                                                .phoneme("/lɔːn/")
                                                .imageURL("https://example.com/images/lon.jpg")
                                                .audioURL("https://example.com/audio/lon.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build(),
                                Word.builder()
                                                .flashcardId(flashcard3.getId())
                                                .newWord("Nhanh")
                                                .meaning("Có tốc độ cao, di chuyển mau lẹ")
                                                .wordForm(WordForm.ADJECTIVE)
                                                .phoneme("/ɲaɪŋ/")
                                                .imageURL("https://example.com/images/nhanh.jpg")
                                                .audioURL("https://example.com/audio/nhanh.mp3")
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build());
                wordRepository.saveAll(words3);

                log.info("✅ Seeded {} flashcards with {} words", 3, words1.size() + words2.size() + words3.size());
        }
}
