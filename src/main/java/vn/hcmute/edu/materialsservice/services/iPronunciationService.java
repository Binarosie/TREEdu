package vn.hcmute.edu.materialsservice.services;

import vn.hcmute.edu.materialsservice.dtos.request.PronunciationCheckRequest;
import vn.hcmute.edu.materialsservice.dtos.request.SentencesRequest;
import vn.hcmute.edu.materialsservice.dtos.request.TopicRequest;
import vn.hcmute.edu.materialsservice.dtos.response.PronunciationCheckResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicDetailResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicResponse;

import java.util.List;

public interface iPronunciationService {

    // ── Pronunciation Check ───────────────────────────────────────────────────
    PronunciationCheckResponse checkAndSave(PronunciationCheckRequest request);

    // Thêm method chỉ check, không lưu DB
    PronunciationCheckResponse checkOnly(PronunciationCheckRequest request);

    PronunciationCheckResponse getById(String id);

    List<PronunciationCheckResponse> getAll();

    void deleteHistory(String id);

    // ── Topic ─────────────────────────────────────────────────────────────────
    /** Trả về tất cả topics (summary, không có sentences). */
    List<TopicResponse> getTopics();

    /** Trả về chi tiết 1 topic kèm full danh sách sentences. */
    TopicDetailResponse getTopicById(String id);

    /** Tạo topic mới (có thể kèm sentences ban đầu). */
    TopicDetailResponse createTopic(TopicRequest request);

    /** Cập nhật tên/mô tả/level của topic (KHÔNG thay đổi sentences). */
    TopicDetailResponse updateTopic(String id, TopicRequest request);

    /** Xóa toàn bộ topic. */
    void deleteTopic(String id);

    // ── Sentences trong Topic ─────────────────────────────────────────────────
    /** Thêm 1 hoặc nhiều câu vào topic. */
    TopicDetailResponse addSentences(String topicId, SentencesRequest request);

    /** Xóa 1 câu khỏi topic theo index trong list. */
    TopicDetailResponse removeSentence(String topicId, int sentenceIndex);

    /** Lấy câu ngẫu nhiên từ topic theo tên. */
    String getRandomSentence(String topicName);
}
