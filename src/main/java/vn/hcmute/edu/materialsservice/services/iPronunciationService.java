package vn.hcmute.edu.materialsservice.services;

import vn.hcmute.edu.materialsservice.dtos.request.PronunciationCheckRequest;
import vn.hcmute.edu.materialsservice.dtos.request.SentencesRequest;
import vn.hcmute.edu.materialsservice.dtos.request.TopicRequest;
import vn.hcmute.edu.materialsservice.dtos.response.PronunciationCheckResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicDetailResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicResponse;

import java.util.List;

public interface iPronunciationService {

    PronunciationCheckResponse checkAndSave(PronunciationCheckRequest request);

    PronunciationCheckResponse getById(String id);

    List<PronunciationCheckResponse> getAll();

    void deleteHistory(String id);

    List<TopicResponse> getTopics();

    TopicDetailResponse getTopicById(String id);

    TopicDetailResponse createTopic(TopicRequest request);

    TopicDetailResponse updateTopic(String id, TopicRequest request);

    void deleteTopic(String id);

    TopicDetailResponse addSentences(String topicId, SentencesRequest request);

    TopicDetailResponse removeSentence(String topicId, int sentenceIndex);

    String getRandomSentence(String topicName);
}
