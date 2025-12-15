package vn.hcmute.edu.materialsservice.Service;

import org.springframework.security.core.Authentication;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardProgressResponse;
import vn.hcmute.edu.materialsservice.Enum.LearningStatus;

import java.util.List;

public interface FlashcardLearningService {

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
    List<FlashcardProgressResponse> getLearningProgressByStatus(LearningStatus status, Authentication authentication);

    /**
     * Reset tiến trình học (học lại từ đầu)
     */
    FlashcardProgressResponse resetProgress(String flashcardId, Authentication authentication);
}
