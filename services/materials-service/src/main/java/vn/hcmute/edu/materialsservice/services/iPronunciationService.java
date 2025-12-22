package vn.hcmute.edu.materialsservice.services;

import vn.hcmute.edu.materialsservice.dtos.request.PronunciationCheckRequest;
import vn.hcmute.edu.materialsservice.dtos.response.PronunciationCheckResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicResponse;

import java.util.List;

public interface iPronunciationService {
    PronunciationCheckResponse checkAndSave(PronunciationCheckRequest request);
    PronunciationCheckResponse getById(String id);
    List<PronunciationCheckResponse> getAll();

    String getRandomSentence(String topicName);  // Random câu theo topic
    List<TopicResponse> getTopics();

}
