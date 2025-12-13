package vn.hcmute.edu.materialsservice.Service;

import vn.hcmute.edu.materialsservice.Dto.request.PronunciationCheckRequest;
import vn.hcmute.edu.materialsservice.Dto.response.PronunciationCheckResponse;
import vn.hcmute.edu.materialsservice.Dto.response.TopicResponse;

import java.util.List;

public interface PronunciationService {
    PronunciationCheckResponse checkAndSave(PronunciationCheckRequest request);
    PronunciationCheckResponse getById(String id);
    List<PronunciationCheckResponse> getAll();

    String getRandomSentence(String topicName);  // Random câu theo topic
    List<TopicResponse> getTopics();

}
