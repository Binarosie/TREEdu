package vn.hcmute.edu.materialsservice.services;

import org.springframework.security.core.Authentication;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardProgressResponse;
import vn.hcmute.edu.materialsservice.Enum.ELearningStatus;

import java.util.List;

public interface iFlashcardLearningService {

    /**
     * Bắt đầu học hoặc tiếp tục học flashcard
     */
    FlashcardProgressResponse startOrContinueLearning(String flashcardId, Authentication authentication);

    /**
     * Đánh dấu một word đã được xem
     */
    FlashcardProgressResponse markWordAsViewed(String flashcardId, String wordId, Authentication authentication);

    /**
     * Lấy tiến trình học của user với 1 flashcard cụ thể
     */
    FlashcardProgressResponse getLearningProgress(String flashcardId, Authentication authentication);

    /**
     * Lấy tất cả flashcard đang học của user
     */
    List<FlashcardProgressResponse> getAllLearningProgress(Authentication authentication);

    /**
     * Lấy flashcard đang học theo trạng thái
     */
    List<FlashcardProgressResponse> getLearningProgressByStatus(ELearningStatus status, Authentication authentication);

    /**
     * Reset tiến trình học (học lại từ đầu)
     */
    FlashcardProgressResponse resetProgress(String flashcardId, Authentication authentication);
}
