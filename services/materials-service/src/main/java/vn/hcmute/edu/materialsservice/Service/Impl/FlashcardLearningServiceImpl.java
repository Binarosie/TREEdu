package vn.hcmute.edu.materialsservice.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardProgressResponse;
import vn.hcmute.edu.materialsservice.Enum.FlashcardType;
import vn.hcmute.edu.materialsservice.Enum.LearningStatus;
import vn.hcmute.edu.materialsservice.Model.Flashcard;
import vn.hcmute.edu.materialsservice.Model.FlashcardProgress;
import vn.hcmute.edu.materialsservice.Repository.FlashcardProgressRepository;
import vn.hcmute.edu.materialsservice.Repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.Repository.WordRepository;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.Service.FlashcardLearningService;
import vn.hcmute.edu.materialsservice.exception.FlashcardNotFoundException;
import vn.hcmute.edu.materialsservice.exception.ResourceNotFoundException;
import vn.hcmute.edu.materialsservice.exception.WordNotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardLearningServiceImpl implements FlashcardLearningService {

    private final FlashcardProgressRepository progressRepository;
    private final FlashcardRepository flashcardRepository;
    private final WordRepository wordRepository;

    @Override
    @Transactional
    public FlashcardProgressResponse startOrContinueLearning(String flashcardId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUser().getId().toString();

        // Kiểm tra flashcard có tồn tại không
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new FlashcardNotFoundException(flashcardId));

        // Kiểm tra quyền truy cập flashcard
        validateFlashcardAccess(flashcard, userId, userDetails);

        // Kiểm tra xem đã có progress chưa
        FlashcardProgress progress = progressRepository
                .findByUserIdAndFlashcardId(userId, flashcardId)
                .orElse(null);

        if (progress == null) {
            // Tạo mới progress
            int totalWords = (int) wordRepository.countByFlashcardId(flashcardId);

            progress = FlashcardProgress.builder()
                    .userId(userId)
                    .flashcardId(flashcardId)
                    .viewedWordIds(new HashSet<>())
                    .totalWords(totalWords)
                    .status(LearningStatus.PROCESSING)
                    .startedAt(LocalDateTime.now())
                    .lastAccessedAt(LocalDateTime.now())
                    .build();

            log.info("User {} started learning flashcard {}", userId, flashcardId);
        } else {
            // Cập nhật lastAccessedAt
            progress.setLastAccessedAt(LocalDateTime.now());
            log.info("User {} continued learning flashcard {}", userId, flashcardId);
        }

        FlashcardProgress saved = progressRepository.save(progress);
        return toResponse(saved, flashcard);
    }

    @Override
    @Transactional
    public FlashcardProgressResponse markWordAsViewed(String flashcardId, String wordId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUser().getId().toString();

        // Kiểm tra word có thuộc flashcard không
        if (!wordRepository.existsByIdAndFlashcardId(wordId, flashcardId)) {
            throw new WordNotFoundException(wordId);
        }

        // Lấy progress
        FlashcardProgress progress = progressRepository
                .findByUserIdAndFlashcardId(userId, flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chưa bắt đầu học flashcard này. Vui lòng gọi API start learning trước."));

        // Thêm word vào danh sách đã xem
        progress.getViewedWordIds().add(wordId);
        progress.setLastAccessedAt(LocalDateTime.now());

        // Kiểm tra đã hoàn thành chưa
        if (progress.isCompleted() && progress.getStatus() != LearningStatus.DONE) {
            progress.setStatus(LearningStatus.DONE);
            progress.setCompletedAt(LocalDateTime.now());
            log.info("User {} completed learning flashcard {}", userId, flashcardId);
        }

        FlashcardProgress saved = progressRepository.save(progress);

        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new FlashcardNotFoundException(flashcardId));

        return toResponse(saved, flashcard);
    }

    @Override
    public FlashcardProgressResponse getLearningProgress(String flashcardId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUser().getId().toString();

        FlashcardProgress progress = progressRepository
                .findByUserIdAndFlashcardId(userId, flashcardId)
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
    public List<FlashcardProgressResponse> getLearningProgressByStatus(LearningStatus status,
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
                .findByUserIdAndFlashcardId(userId, flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chưa có tiến trình học cho flashcard này"));

        // Reset progress
        progress.getViewedWordIds().clear();
        progress.setStatus(LearningStatus.PROCESSING);
        progress.setCompletedAt(null);
        progress.setStartedAt(LocalDateTime.now());
        progress.setLastAccessedAt(LocalDateTime.now());

        FlashcardProgress saved = progressRepository.save(progress);

        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new FlashcardNotFoundException(flashcardId));

        log.info("User {} reset learning progress for flashcard {}", userId, flashcardId);

        return toResponse(saved, flashcard);
    }

    // ============= HELPER METHODS =============

    private void validateFlashcardAccess(Flashcard flashcard, String userId, CustomUserDetails userDetails) {
        boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_SUPPORTER"));

        if (!isAdminOrSupporter) {
            if (flashcard.getType() == FlashcardType.BY_MEMBER &&
                    !userId.equals(flashcard.getCreatedBy())) {
                throw new AccessDeniedException(
                        "Bạn không có quyền học flashcard này");
            }
        }
    }

    private FlashcardProgressResponse toResponse(FlashcardProgress progress, Flashcard flashcard) {
        return FlashcardProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUserId())
                .flashcardId(progress.getFlashcardId())
                .flashcardTitle(flashcard.getTitle())
                .viewedWordIds(progress.getViewedWordIds())
                .viewedWordCount(progress.getViewedWordCount())
                .totalWords(progress.getTotalWords())
                .progressPercentage(Math.round(progress.getProgress() * 100.0) / 100.0)
                .status(progress.getStatus())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .lastAccessedAt(progress.getLastAccessedAt())
                .build();
    }
}
