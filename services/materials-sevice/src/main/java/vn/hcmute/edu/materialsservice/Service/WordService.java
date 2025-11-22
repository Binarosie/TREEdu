package vn.hcmute.edu.materialsservice.Service;

import vn.hcmute.edu.materialsservice.Dto.request.WordRequest;
import vn.hcmute.edu.materialsservice.Dto.response.WordResponse;

import java.util.List;

public interface WordService {

    WordResponse addWord(String flashcardId, WordRequest request);

    WordResponse updateWord(String id, WordRequest request);

    void deleteWord(String id);

    WordResponse getWordById(String id);

    List<WordResponse> getWordsByFlashcardId(String flashcardId);
}
