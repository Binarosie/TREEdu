package vn.hcmute.edu.materialsservice.Service;

import org.springframework.security.core.Authentication;
import vn.hcmute.edu.materialsservice.Dto.request.WordRequest;
import vn.hcmute.edu.materialsservice.Dto.response.WordResponse;

import java.util.List;

public interface WordService {

    WordResponse addWord(String flashcardId, WordRequest request, Authentication authentication);

    WordResponse updateWord(String id, WordRequest request, Authentication authentication);

    void deleteWord(String id, Authentication authentication);

    WordResponse getWordById(String id);

    List<WordResponse> getWordsByFlashcardId(String flashcardId, Authentication authentication);
}
