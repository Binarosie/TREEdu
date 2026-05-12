package vn.hcmute.edu.materialsservice.services;

import org.springframework.security.core.Authentication;
import vn.hcmute.edu.materialsservice.dtos.request.FlashcardRequest;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardResponse;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardWithWordsResponse;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardVisibility;

import java.util.List;

public interface iFlashcardService {

    FlashcardResponse createFlashcard(FlashcardRequest request, Authentication authentication);

    FlashcardResponse updateFlashcard(String id, FlashcardRequest request, Authentication authentication);

    void deleteFlashcard(String id, Authentication authentication);

    FlashcardResponse getFlashcardById(String id, Authentication authentication);

    // THÊM METHOD MỚI
    FlashcardWithWordsResponse getFlashcardWithWords(String id, Authentication authentication);

    List<FlashcardResponse> getAllFlashcard(Authentication authentication);

    List<FlashcardResponse> getFlashcardsByTopic(String topic, Authentication authentication);

    List<FlashcardResponse> getFlashcardsByLevel(Integer level, Authentication authentication);

    // PHƯƠNG THỨC MỚI CHO VISIBILITY
    FlashcardResponse changeVisibility(String id, EFlashcardVisibility visibility, Authentication authentication);

    List<FlashcardResponse> getPublicFlashcards(Authentication authentication);

    /**
     * Lấy tất cả flashcard của user + tất cả PUBLIC flashcard từ hệ thống (của user
     * khác)
     */
    List<FlashcardResponse> getAllFlashcardWithPublic(Authentication authentication);

    List<FlashcardResponse> getMyFlashcards(Authentication authentication);
}
