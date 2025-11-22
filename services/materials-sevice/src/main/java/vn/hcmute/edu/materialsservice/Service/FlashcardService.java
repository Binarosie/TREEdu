package vn.hcmute.edu.materialsservice.Service;

import vn.hcmute.edu.materialsservice.Dto.request.FlashcardRequest;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardResponse;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardWithWordsResponse;

import java.util.List;

public interface FlashcardService {

    FlashcardResponse createFlashcard(FlashcardRequest request);

    FlashcardResponse updateFlashcard(String id, FlashcardRequest request);

    void deleteFlashcard(String id);

    FlashcardResponse getFlashcardById(String id);

    // THÊM METHOD MỚI
    FlashcardWithWordsResponse getFlashcardWithWords(String id);

    List<FlashcardResponse> getAllFlashcard();

    List<FlashcardResponse> getFlashcardsByTopic(String topic);

    List<FlashcardResponse> getFlashcardsByLevel(Integer level);
}
