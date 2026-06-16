package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.dtos.request.WordRequest;
import vn.hcmute.edu.materialsservice.dtos.response.WordResponse;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardType;
import vn.hcmute.edu.materialsservice.Mapper.WordMapper;
import vn.hcmute.edu.materialsservice.models.Flashcard;
import vn.hcmute.edu.materialsservice.models.Word;
import vn.hcmute.edu.materialsservice.repository.FlashcardProgressRepository;
import vn.hcmute.edu.materialsservice.repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.repository.WordRepository;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.iWordService;
import vn.hcmute.edu.materialsservice.exceptions.FlashcardNotFoundException;
import vn.hcmute.edu.materialsservice.exceptions.WordNotFoundException;
import vn.hcmute.edu.materialsservice.services.TTSService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordServiceImpl implements iWordService {

        private final WordRepository wordRepository;
        private final FlashcardRepository flashcardRepository;
        private final WordMapper wordMapper;
        private final FlashcardProgressRepository progressRepository;
        private final TTSService ttsService;

        @Override
        @Transactional
        public WordResponse addWord(String flashcardId, WordRequest request, Authentication authentication) {
                // Kiểm tra flashcard có tồn tại không
                Flashcard flashcard = flashcardRepository.findById(flashcardId)
                                .orElseThrow(() -> new FlashcardNotFoundException(flashcardId));

                long progressCount = progressRepository.findByFlashcardId(flashcardId).size();
                if (progressCount > 0) {
                        throw new IllegalStateException(
                                        "Không thể thêm từ vào flashcard này vì đã có " + progressCount + " người học. "
                                                        + "Chỉ được thêm từ vào flashcard chưa có ai học.");
                }

                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUser().getId().toString();
                boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                                a.getAuthority().equals("ROLE_SUPPORTER"));

                if (!isAdminOrSupporter) {
                        if (flashcard.getType() == EFlashcardType.SYSTEM) {
                                throw new AccessDeniedException(
                                                "Bạn không có quyền thêm từ vào flashcard hệ thống");
                        }
                        if (flashcard.getType() == EFlashcardType.BY_MEMBER &&
                                        !userId.equals(flashcard.getCreatedBy())) {
                                throw new AccessDeniedException(
                                                "Bạn chỉ có thể thêm từ vào flashcard do chính bạn tạo");
                        }
                }

                Word word = wordMapper.toEntity(request);
                word.setFlashcardId(flashcardId);
                word.setCreatedAt(LocalDateTime.now());
                word.setUpdatedAt(LocalDateTime.now());

                // 1. Lưu word trước để có ID
                Word savedWord = wordRepository.save(word);

                // 2. Generate audio và lưu trữ vĩnh viễn (không phải URL tạm)
                String audioUrl = ttsService.generateAudioAndStore(savedWord.getNewWord(), savedWord.getId());
                if (audioUrl != null) {
                        savedWord.setAudioURL(audioUrl);
                        savedWord = wordRepository.save(savedWord);
                }

                log.info("Word added to flashcard {}: {} (audio: {})", flashcardId, savedWord.getNewWord(), audioUrl);

                return wordMapper.toResponse(savedWord);
        }

        @Override
        @Transactional
        public WordResponse updateWord(String id, WordRequest request, Authentication authentication) {
                Word existingWord = wordRepository.findById(id)
                                .orElseThrow(() -> new WordNotFoundException(id));

                // Lấy flashcard để check quyền
                Flashcard flashcard = flashcardRepository.findById(existingWord.getFlashcardId())
                                .orElseThrow(() -> new FlashcardNotFoundException(existingWord.getFlashcardId()));

                long progressCount = progressRepository.findByFlashcardId(flashcard.getId()).size();
                if (progressCount > 0) {
                        throw new IllegalStateException(
                                        "Không thể cập nhật từ này vì flashcard đã có " + progressCount + " người học. "
                                                        + "Chỉ được cập nhật từ trong flashcard chưa có ai học.");
                }

                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUser().getId().toString();
                boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                                a.getAuthority().equals("ROLE_SUPPORTER"));

                if (!isAdminOrSupporter) {
                        if (flashcard.getType() == EFlashcardType.SYSTEM) {
                                throw new AccessDeniedException(
                                                "Bạn không có quyền cập nhật từ trong flashcard hệ thống");
                        }
                        if (flashcard.getType() == EFlashcardType.BY_MEMBER &&
                                        !userId.equals(flashcard.getCreatedBy())) {
                                throw new AccessDeniedException(
                                                "Bạn chỉ có thể cập nhật từ trong flashcard do chính bạn tạo");
                        }
                }

                existingWord.setNewWord(request.getNewWord());
                existingWord.setMeaning(request.getMeaning());
                existingWord.setWordForm(request.getWordForm());
                existingWord.setPhoneme(request.getPhoneme());

                // Generate audio và lưu trữ vĩnh viễn (không phải URL tạm)
                String audioUrl = ttsService.generateAudioAndStore(request.getNewWord(), existingWord.getId());
                if (audioUrl != null) {
                        existingWord.setAudioURL(audioUrl);
                }

                existingWord.setUpdatedAt(LocalDateTime.now());

                Word updatedWord = wordRepository.save(existingWord);

                log.info("Word updated: {} (audio: {})", id, audioUrl);

                return wordMapper.toResponse(updatedWord);
        }

        @Override
        public void deleteWord(String id, Authentication authentication) {
                Word existingWord = wordRepository.findById(id)
                                .orElseThrow(() -> new WordNotFoundException(id));

                // Lấy flashcard để check quyền
                Flashcard flashcard = flashcardRepository.findById(existingWord.getFlashcardId())
                                .orElseThrow(() -> new FlashcardNotFoundException(existingWord.getFlashcardId()));

                long progressCount = progressRepository.findByFlashcardId(flashcard.getId()).size();
                if (progressCount > 0) {
                        throw new IllegalStateException(
                                        "Không thể xóa từ này vì flashcard đã có " + progressCount + " người học. "
                                                        + "Chỉ được xóa từ trong flashcard chưa có ai học.");
                }

                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUser().getId().toString();
                boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                                a.getAuthority().equals("ROLE_SUPPORTER"));

                if (!isAdminOrSupporter) {
                        if (flashcard.getType() == EFlashcardType.SYSTEM) {
                                throw new AccessDeniedException(
                                                "Bạn không có quyền xóa từ trong flashcard hệ thống");
                        }
                        if (flashcard.getType() == EFlashcardType.BY_MEMBER &&
                                        !userId.equals(flashcard.getCreatedBy())) {
                                throw new AccessDeniedException(
                                                "Bạn chỉ có thể xóa từ trong flashcard do chính bạn tạo");
                        }
                }

                wordRepository.deleteById(id);

                log.info("Word deleted: {}", id);
        }

        @Override
        public WordResponse getWordById(String id) {
                Word word = wordRepository.findById(id)
                                .orElseThrow(() -> new WordNotFoundException(id));

                return wordMapper.toResponse(word);
        }

        @Override
        public List<WordResponse> getWordsByFlashcardId(String flashcardId, Authentication authentication) {
                // Kiểm tra flashcard có tồn tại không
                Flashcard flashcard = flashcardRepository.findById(flashcardId)
                                .orElseThrow(() -> new FlashcardNotFoundException(flashcardId));

                System.out.println("=== DEBUG GET WORDS BY FLASHCARD ===");
                System.out.println("Flashcard ID: " + flashcardId);
                System.out.println("Flashcard Type: " + flashcard.getType());
                System.out.println("Flashcard CreatedBy: " + flashcard.getCreatedBy());
                System.out.println("Authentication: " + authentication);
                if (authentication != null) {
                        System.out.println("isAuthenticated: " + authentication.isAuthenticated());
                        System.out.println("Principal class: " + authentication.getPrincipal().getClass().getName());
                }

                // ================= FILTER THEO ROLE =================
                // Check if user is truly authenticated (not anonymous)
                boolean isRealUser = authentication != null
                                && authentication.isAuthenticated()
                                && !(authentication.getPrincipal() instanceof String); // Anonymous user has
                                                                                       // "anonymousUser" string

                if (isRealUser) {
                        try {
                                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

                                boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                                                a.getAuthority().equals("ROLE_SUPPORTER"));

                                // MEMBER chỉ xem words của SYSTEM hoặc flashcard của mình
                                if (!isAdminOrSupporter) {
                                        String userId = userDetails.getUser().getId().toString();
                                        if (flashcard.getType() == EFlashcardType.BY_MEMBER &&
                                                        !userId.equals(flashcard.getCreatedBy())) {
                                                throw new AccessDeniedException(
                                                                "Bạn không có quyền xem words của flashcard này");
                                        }
                                }
                        } catch (ClassCastException e) {
                                // If cast fails, treat as guest
                                if (flashcard.getType() != EFlashcardType.SYSTEM) {
                                        throw new AccessDeniedException(
                                                        "Bạn cần đăng nhập để xem words của flashcard này");
                                }
                        }
                } else {
                        // GUEST chỉ xem words của SYSTEM flashcard
                        if (flashcard.getType() != EFlashcardType.SYSTEM) {
                                throw new AccessDeniedException(
                                                "Bạn cần đăng nhập để xem words của flashcard này");
                        }
                }

                List<Word> words = wordRepository.findByFlashcardId(flashcardId);
                return wordMapper.toResponseList(words);
        }

        @Override
        @Transactional
        public int regenerateBrokenAudio(String flashcardId) {
                // Lấy tất cả words trong flashcard
                List<Word> words = wordRepository.findByFlashcardId(flashcardId);

                int regeneratedCount = 0;

                log.info("🔄 Starting to regenerate audio for {} words in flashcard {}", words.size(), flashcardId);

                for (Word word : words) {
                        try {
                                // Re-generate audio từ TTS API và lưu lên Cloudinary
                                String newAudioUrl = ttsService.generateAudioAndStore(word.getNewWord(), word.getId());

                                if (newAudioUrl != null) {
                                        word.setAudioURL(newAudioUrl);
                                        word.setUpdatedAt(LocalDateTime.now());
                                        wordRepository.save(word);
                                        regeneratedCount++;
                                        log.info("✅ Regenerated audio for word: {} -> {}", word.getNewWord(),
                                                        newAudioUrl);
                                } else {
                                        log.warn("⚠️ Failed to regenerate audio for word: {}", word.getNewWord());
                                }
                        } catch (Exception e) {
                                log.error("❌ Error regenerating audio for word {}: {}", word.getNewWord(),
                                                e.getMessage(), e);
                        }
                }

                log.info("✅ Audio regeneration complete: {} / {} words updated", regeneratedCount, words.size());

                return regeneratedCount;
        }
}
