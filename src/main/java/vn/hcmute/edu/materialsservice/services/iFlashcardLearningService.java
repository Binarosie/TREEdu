package vn.hcmute.edu.materialsservice.services;

import org.springframework.security.core.Authentication;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardProgressResponse;
import vn.hcmute.edu.materialsservice.dtos.response.WordCheckResponse;
import vn.hcmute.edu.materialsservice.Enum.ELearningStatus;

import java.util.List;

public interface iFlashcardLearningService {

    FlashcardProgressResponse startOrContinueLearning(String flashcardId, Authentication authentication);

    FlashcardProgressResponse markWordAsViewed(String flashcardId, String wordId, Authentication authentication);

    WordCheckResponse submitWordAnswer(String flashcardId, String wordId, String userAnswer,
            Authentication authentication);

    FlashcardProgressResponse getLearningProgress(String flashcardId, Authentication authentication);

    List<FlashcardProgressResponse> getAllLearningProgress(Authentication authentication);

    List<FlashcardProgressResponse> getLearningProgressByStatus(ELearningStatus status, Authentication authentication);

    FlashcardProgressResponse resetProgress(String flashcardId, Authentication authentication);
}
