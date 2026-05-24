package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.Enum.EMissionType;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardProgressResponse;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardType;
import vn.hcmute.edu.materialsservice.Enum.ELearningStatus;
import vn.hcmute.edu.materialsservice.dtos.response.WordResponse;
import vn.hcmute.edu.materialsservice.models.*;
import vn.hcmute.edu.materialsservice.repository.FlashcardProgressRepository;
import vn.hcmute.edu.materialsservice.repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.repository.WordRepository;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.IStreakService;
import vn.hcmute.edu.materialsservice.services.iFlashcardLearningService;
import vn.hcmute.edu.materialsservice.exceptions.FlashcardNotFoundException;
import vn.hcmute.edu.materialsservice.exceptions.ResourceNotFoundException;
import vn.hcmute.edu.materialsservice.exceptions.WordNotFoundException;
import vn.hcmute.edu.materialsservice.services.iUserService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardLearningServiceImpl implements iFlashcardLearningService {

        private final FlashcardProgressRepository progressRepository;
        private final FlashcardRepository flashcardRepository;
        private final WordRepository wordRepository;
        private final iUserService userService;
        private final IStreakService streakService;
        private final vn.hcmute.edu.materialsservice.repository.UserRepository userRepository;
        private final MissionServiceImpl missionService;
        @Override
        @Transactional
        public FlashcardProgressResponse startOrContinueLearning(String flashcardId, Authentication authentication) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                Flashcard flashcard = flashcardRepository.findById(flashcardId)
                        .orElseThrow(() -> new FlashcardNotFoundException(flashcardId));

                validateFlashcardAccess(flashcard, userId, userDetails);

                // Lấy totalWords thực tế từ DB (luôn đồng bộ)
                int actualTotalWords = (int) wordRepository.countByFlashcardId(flashcardId);

                FlashcardProgress progress = progressRepository
                        .findFirstByUserIdAndFlashcardId(userId, flashcardId)
                        .orElse(null);

                if (progress == null) {
                        progress = FlashcardProgress.builder()
                                .userId(userId)
                                .flashcardId(flashcardId)
                                .viewedWordIds(new HashSet<>())
                                .totalWords(actualTotalWords)
                                .status(ELearningStatus.PROCESSING)
                                .startedAt(LocalDateTime.now())
                                .lastAccessedAt(LocalDateTime.now())
                                .build();
                        log.info("User {} started learning flashcard {}", userId, flashcardId);
                } else {
                        // ✅ Fix: luôn đồng bộ lại totalWords khi tiếp tục học
                        progress.setTotalWords(actualTotalWords);
                        progress.setLastAccessedAt(LocalDateTime.now());
                        log.info("User {} continued learning flashcard {}", userId, flashcardId);
                }

                FlashcardProgress saved = progressRepository.save(progress);
                return toResponse(saved, flashcard);
        }


        @Override
        @Transactional
        public FlashcardProgressResponse markWordAsViewed(String flashcardId, String wordId, Authentication authentication) {

                // 1. Lấy userId từ Authentication cho khớp với Interface
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                // 2. Lấy Flashcard và tổng số từ thực tế trước
                Flashcard flashcard = flashcardRepository.findById(flashcardId)
                        .orElseThrow(() -> new FlashcardNotFoundException(flashcardId));
                int actualTotalWords = (int) wordRepository.countByFlashcardId(flashcardId);

                // 3. Lấy Progress hiện tại (hoặc tạo mới bằng helper method)
                FlashcardProgress progress = progressRepository
                        .findFirstByUserIdAndFlashcardId(userId, flashcardId)
                        .orElseGet(() -> createNewProgress(userId, flashcardId, actualTotalWords));

                // 4. Thêm từ vào danh sách đã học và đồng bộ lại tổng số từ
                boolean isNewWord = progress.getViewedWordIds().add(wordId);
                progress.setTotalWords(actualTotalWords);
                progress.setLastAccessedAt(LocalDateTime.now());

                Integer xpGained = 0;
                Boolean leveledUp = false;

                if (isNewWord && progress.isCompleted() && progress.getCompletedAt() == null) {
                        progress.setCompletedAt(LocalDateTime.now());
                        progress.setStatus(ELearningStatus.DONE); // 🎯 Lúc này là chuyển sang Done nè

                        // ---- TÍNH XP CHO NGƯỜI HỌC ----
                        int baseXP = 2;
                        double multiplier = 1.0 + (0.1 * flashcard.getLevel());
                        xpGained = (int) (progress.getTotalWords() * baseXP * multiplier);

                        // ✅ 1. Gọi service cộng điểm và check Level Up
                        leveledUp = userService.addXpToMember(userId, xpGained);

                        // ✅ 2. Cập nhật Streak VÀ Tăng tổng số Flashcard đã học
                        User user = userService.findById(userId).orElse(null);
                        if (user instanceof Member member) {
                                // Cập nhật chuỗi ngày học
                                streakService.updateStreak(member);

                                // 🚀 THÊM LOGIC TĂNG BỘ ĐẾM FLASHCARD Ở ĐÂY:
                                int currentFlashcardTotal = member.getTotalFlashcardLearned() != null ? member.getTotalFlashcardLearned() : 0;
                                member.setTotalFlashcardLearned(currentFlashcardTotal + 1);

                                // Lưu lại thay đổi vào Database
                                userRepository.save(member);
                                missionService.fireMissionEvent(userId, EMissionType.LEARN_FLASHCARD, 1);
                        }

                        // ---- TÍNH XP THỤ ĐỘNG CHO NGƯỜI TẠO ----
                        if (flashcard.getType() == EFlashcardType.BY_MEMBER
                                && !flashcard.getCreatedBy().equals(userId)) {

                                int creatorPassiveXp = 15;
                                userService.addXpToMember(flashcard.getCreatedBy(), creatorPassiveXp);
                        }
                }
                // 6. Lưu progress
                FlashcardProgress saved = progressRepository.save(progress);

                // 7. Dùng chung hàm toResponse để đảm bảo trả về đủ data cho Frontend
                FlashcardProgressResponse response = toResponse(saved, flashcard);

                // Gắn thêm data Gamification vào DTO trả về
                response.setXpGained(xpGained);
                response.setLeveledUp(leveledUp);

                return response;
        }
        @Override
        public FlashcardProgressResponse getLearningProgress(String flashcardId, Authentication authentication) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                FlashcardProgress progress = progressRepository
                        .findFirstByUserIdAndFlashcardId(userId, flashcardId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Chưa có tiến trình học cho flashcard này"));

                Flashcard flashcard = flashcardRepository.findById(flashcardId)
                        .orElseThrow(() -> new FlashcardNotFoundException(flashcardId));

                return toResponse(progress, flashcard);
        }

        @Override
        public List<FlashcardProgressResponse> getAllLearningProgress(Authentication authentication) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                List<FlashcardProgress> progressList = progressRepository.findByUserId(userId);

                return progressList.stream()
                        .map(progress -> {
                                Flashcard flashcard = flashcardRepository.findById(progress.getFlashcardId())
                                        .orElse(null);
                                return flashcard != null ? toResponse(progress, flashcard) : null;
                        })
                        .filter(response -> response != null)
                        .collect(Collectors.toList());
        }

        @Override
        public List<FlashcardProgressResponse> getLearningProgressByStatus(ELearningStatus status,
                                                                           Authentication authentication) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                List<FlashcardProgress> progressList = progressRepository.findByUserIdAndStatus(userId, status);

                return progressList.stream()
                        .map(progress -> {
                                Flashcard flashcard = flashcardRepository.findById(progress.getFlashcardId())
                                        .orElse(null);
                                return flashcard != null ? toResponse(progress, flashcard) : null;
                        })
                        .filter(response -> response != null)
                        .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public FlashcardProgressResponse resetProgress(String flashcardId, Authentication authentication) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                FlashcardProgress progress = progressRepository
                        .findFirstByUserIdAndFlashcardId(userId, flashcardId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Chưa có tiến trình học cho flashcard này"));

                // Reset progress
                progress.getViewedWordIds().clear();
                progress.setStatus(ELearningStatus.PROCESSING);
                progress.setCompletedAt(null);
                progress.setStartedAt(LocalDateTime.now());
                progress.setLastAccessedAt(LocalDateTime.now());

                FlashcardProgress saved = progressRepository.save(progress);

                Flashcard flashcard = flashcardRepository.findById(flashcardId)
                        .orElseThrow(() -> new FlashcardNotFoundException(flashcardId));

                log.info("User {} reset learning progress for flashcard {}", userId, flashcardId);

                return toResponse(saved, flashcard);
        }

        private void validateFlashcardAccess(Flashcard flashcard, String userId, CustomUserDetails userDetails) {
                // 1. Nếu là Admin hoặc Supporter thì luôn có quyền truy cập
                boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                a.getAuthority().equals("ROLE_SUPPORTER"));
                if (isAdminOrSupporter) {
                        return;
                }

                // 2. Đối với người dùng thông thường (ROLE_MEMBER)
                if (flashcard.getType() == EFlashcardType.BY_MEMBER) {
                        // Nếu bộ thẻ là RIÊNG TƯ (PRIVATE) VÀ người đang đăng nhập KHÔNG PHẢI tác giả
                        if (flashcard.getVisibility() == vn.hcmute.edu.materialsservice.Enum.EFlashcardVisibility.PRIVATE
                                && !userId.equals(flashcard.getCreatedBy())) {
                                throw new AccessDeniedException("Bạn không có quyền truy cập bộ thẻ riêng tư của thành viên khác.");
                        }
                }

        }

        private FlashcardProgressResponse toResponse(FlashcardProgress progress, Flashcard flashcard) {
                List<Word> words = wordRepository.findByFlashcardId(flashcard.getId());

                // ✅ Fix: lấy totalWords thực tế thay vì từ entity (phòng lệch dữ liệu)
                int actualTotalWords = words.size();

                List<WordResponse> wordResponses = words.stream()
                        .map(word -> WordResponse.builder()
                                .id(word.getId())
                                .flashcardId(word.getFlashcardId())
                                .newWord(word.getNewWord())
                                .meaning(word.getMeaning())
                                .wordForm(word.getWordForm())
                                .phoneme(word.getPhoneme())
                                .audioURL(word.getAudioURL())
                                .createdAt(word.getCreatedAt())
                                .updatedAt(word.getUpdatedAt())
                                .build())
                        .collect(Collectors.toList());

                // ✅ Fix: tránh chia cho 0
                double progressPercentage = 0.0;
                if (actualTotalWords > 0) {
                        progressPercentage = Math.round(
                                (progress.getViewedWordCount() * 100.0 / actualTotalWords) * 100.0
                        ) / 100.0;
                }

                return FlashcardProgressResponse.builder()
                        .id(progress.getId())
                        .userId(progress.getUserId())
                        .flashcardId(progress.getFlashcardId())
                        .flashcardTitle(flashcard.getTitle())
                        .viewedWordIds(progress.getViewedWordIds())
                        .viewedWordCount(progress.getViewedWordCount())
                        .totalWords(actualTotalWords)
                        .progressPercentage(progressPercentage)
                        .status(progress.getStatus())
                        .startedAt(progress.getStartedAt())
                        .completedAt(progress.getCompletedAt())
                        .lastAccessedAt(progress.getLastAccessedAt())
                        .words(wordResponses)
                        .build();
        }
        // Hàm helper để tạo Progress mới
        private FlashcardProgress createNewProgress(String userId, String flashcardId, int totalWords) {
                return FlashcardProgress.builder()
                        .userId(userId)
                        .flashcardId(flashcardId)
                        .viewedWordIds(new HashSet<>())
                        .totalWords(totalWords)
                        .status(ELearningStatus.PROCESSING)
                        .startedAt(LocalDateTime.now())
                        .lastAccessedAt(LocalDateTime.now())
                        .build();
        }

}
