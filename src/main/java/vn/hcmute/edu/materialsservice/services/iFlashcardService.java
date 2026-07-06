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

    FlashcardWithWordsResponse getFlashcardWithWords(String id, Authentication authentication);

    List<FlashcardResponse> getAllFlashcard(Authentication authentication);

    List<FlashcardResponse> getFlashcardsByTitle(String title, Authentication authentication);

    List<FlashcardResponse> getFlashcardsByLevel(Integer level, Authentication authentication);

    FlashcardResponse changeVisibility(String id, EFlashcardVisibility visibility, Authentication authentication);

    List<FlashcardResponse> getPublicFlashcards(Authentication authentication);

    List<FlashcardResponse> getAllFlashcardWithPublic(Authentication authentication);

    List<FlashcardResponse> getMyFlashcards(Authentication authentication);
}
