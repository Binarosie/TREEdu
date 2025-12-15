package vn.hcmute.edu.materialsservice.Service;

import org.springframework.security.core.Authentication;
import vn.hcmute.edu.materialsservice.Dto.request.FlashcardRequest;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardResponse;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardWithWordsResponse;

import java.util.List;

public interface FlashcardService {

    FlashcardResponse createFlashcard(FlashcardRequest request, Authentication authentication);

    FlashcardResponse updateFlashcard(String id, FlashcardRequest request, Authentication authentication);

    void deleteFlashcard(String id, Authentication authentication);

    FlashcardResponse getFlashcardById(String id, Authentication authentication);

    // THÊM METHOD MỚI
    FlashcardWithWordsResponse getFlashcardWithWords(String id, Authentication authentication);

    List<FlashcardResponse> getAllFlashcard(Authentication authentication);

    List<FlashcardResponse> getFlashcardsByTopic(String topic, Authentication authentication);

    List<FlashcardResponse> getFlashcardsByLevel(Integer level, Authentication authentication);
}
