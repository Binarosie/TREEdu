package vn.hcmute.edu.materialsservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import reactor.netty.http.client.HttpClient;
import vn.hcmute.edu.materialsservice.dtos.request.PronunciationCheckRequest;
import vn.hcmute.edu.materialsservice.dtos.request.SentencesRequest;
import vn.hcmute.edu.materialsservice.dtos.request.TopicRequest;
import vn.hcmute.edu.materialsservice.dtos.response.PronunciationCheckResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicDetailResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicResponse;
import vn.hcmute.edu.materialsservice.Mapper.PronunciationMapper;
import vn.hcmute.edu.materialsservice.Mapper.TopicMapper;
import vn.hcmute.edu.materialsservice.models.PronunciationHistory;
import vn.hcmute.edu.materialsservice.models.Topic;
import vn.hcmute.edu.materialsservice.repository.PronunciationRepository;
import vn.hcmute.edu.materialsservice.repository.TopicRepository;
import vn.hcmute.edu.materialsservice.services.iPronunciationService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PronunciationServiceImpl implements iPronunciationService {

    private final TopicMapper topicMapper;
    private final PronunciationRepository repository;
    private final TopicRepository topicRepository;
    private final PronunciationMapper mapper;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";

    // Không final để Lombok không đưa vào constructor
    private RestClient restClient;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("${")) {
            log.error("GEMINI_API_KEY chưa được cấu hình!");
            throw new IllegalStateException("Gemini API key is missing!");
        }
        log.info("Gemini API key loaded (length={})", apiKey.length());

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(60));

        this.restClient = RestClient.builder()
                .requestFactory(new ReactorClientHttpRequestFactory(httpClient))
                .build();

        log.info("RestClient (timeout=60s) initialized.");
    }

    @Override
    public PronunciationCheckResponse checkAndSave(PronunciationCheckRequest request) {
        PronunciationHistory aiResult = callGeminiForPronunciation(request.getAudio(), request.getExpectedText());
        aiResult.setCreatedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(aiResult));
    }

    @Override
    public PronunciationCheckResponse getById(String id) {
        PronunciationHistory history = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PronunciationHistory not found: " + id));
        return mapper.toResponse(history);
    }

    @Override
    public List<PronunciationCheckResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    public void deleteHistory(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("PronunciationHistory not found: " + id);
        }
        repository.deleteById(id);
        log.info("Deleted PronunciationHistory id={}", id);
    }

    @Override
    public List<TopicResponse> getTopics() {
        return topicRepository.findAll()
                .stream()
                .map(topicMapper::toResponse)
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }

    @Override
    public TopicDetailResponse getTopicById(String id) {
        Topic topic = findTopicOrThrow(id);
        return topicMapper.toDetailResponse(topic);
    }

    @Override
    public TopicDetailResponse createTopic(TopicRequest request) {
        if (topicRepository.findByName(request.getName()) != null) {
            throw new RuntimeException("Topic already exists: " + request.getName());
        }

        Topic topic = Topic.builder()
                .name(request.getName())
                .description(request.getDescription())
                .level(request.getLevel())
                .sentences(request.getSentences() != null
                        ? new ArrayList<>(request.getSentences())
                        : new ArrayList<>())
                .build();

        Topic saved = topicRepository.save(topic);
        log.info("Created topic id={}, name={}", saved.getId(), saved.getName());
        return topicMapper.toDetailResponse(saved);
    }

    @Override
    public TopicDetailResponse updateTopic(String id, TopicRequest request) {
        Topic topic = findTopicOrThrow(id);

        // Kiểm tra trùng tên với topic khác
        Topic existing = topicRepository.findByName(request.getName());
        if (existing != null && !existing.getId().equals(id)) {
            throw new RuntimeException("Topic name already used: " + request.getName());
        }

        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        topic.setLevel(request.getLevel());

        // Nếu request có truyền sentences thì cập nhật luôn, không thì giữ nguyên
        if (request.getSentences() != null) {
            topic.setSentences(new ArrayList<>(request.getSentences()));
        }

        Topic saved = topicRepository.save(topic);
        log.info("Updated topic id={}", saved.getId());
        return topicMapper.toDetailResponse(saved);
    }

    @Override
    public void deleteTopic(String id) {
        if (!topicRepository.existsById(id)) {
            throw new RuntimeException("Topic not found: " + id);
        }
        topicRepository.deleteById(id);
        log.info("Deleted topic id={}", id);
    }

    @Override
    public TopicDetailResponse addSentences(String topicId, SentencesRequest request) {
        Topic topic = findTopicOrThrow(topicId);

        if (topic.getSentences() == null) {
            topic.setSentences(new ArrayList<>());
        }

        List<String> toAdd = request.getSentences();
        if (toAdd == null || toAdd.isEmpty()) {
            throw new RuntimeException("Sentences list must not be empty.");
        }

        topic.getSentences().addAll(toAdd);
        Topic saved = topicRepository.save(topic);
        log.info("Added {} sentence(s) to topic id={}", toAdd.size(), topicId);
        return topicMapper.toDetailResponse(saved);
    }

    @Override
    public TopicDetailResponse removeSentence(String topicId, int sentenceIndex) {
        Topic topic = findTopicOrThrow(topicId);

        List<String> sentences = topic.getSentences();
        if (sentences == null || sentenceIndex < 0 || sentenceIndex >= sentences.size()) {
            throw new RuntimeException(
                    "Invalid sentence index " + sentenceIndex +
                            " for topic id=" + topicId +
                            " (size=" + (sentences == null ? 0 : sentences.size()) + ")");
        }

        String removed = sentences.remove(sentenceIndex);
        Topic saved = topicRepository.save(topic);
        log.info("Removed sentence[{}]='{}' from topic id={}", sentenceIndex, removed, topicId);
        return topicMapper.toDetailResponse(saved);
    }

    @Override
    public String getRandomSentence(String topicName) {
        Topic topic = topicRepository.findByName(topicName);
        if (topic == null || topic.getSentences() == null || topic.getSentences().isEmpty()) {
            throw new RuntimeException("Topic not found or no sentences: " + topicName);
        }
        Random random = new Random();
        return topic.getSentences().get(random.nextInt(topic.getSentences().size()));
    }

    private PronunciationHistory callGeminiForPronunciation(MultipartFile audio, String expectedText) {
        try {
            byte[] audioBytes = audio.getBytes();
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
            String mimeType = audio.getContentType() != null ? audio.getContentType() : "audio/webm";

            String prompt = buildPronunciationPrompt(expectedText);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", mimeType,
                                            "data", base64Audio))))),
                    "generation_config", Map.of(
                            "temperature", 0.0,
                            "max_output_tokens", 8192));

            String responseBody = this.restClient.post()
                    .uri(GEMINI_URL + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String errorBody = res.getBody() != null
                                ? StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8)
                                : "No body";
                        log.error("Gemini 4xx: {} - {}", res.getStatusCode(), errorBody);
                        throw new RuntimeException("Lỗi Gemini (4xx): " + res.getStatusCode() + " - " + errorBody);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Gemini 5xx: {}", res.getStatusCode());
                        throw new RuntimeException("Lỗi Gemini (5xx): " + res.getStatusCode());
                    })
                    .body(String.class);

            log.debug("Gemini raw response: {}", responseBody);
            return parsePronunciationResponse(responseBody, expectedText);

        } catch (IOException e) {
            log.error("Lỗi đọc file audio", e);
            throw new RuntimeException("Không thể đọc file audio: " + e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi gọi Gemini pronunciation", e);
            throw new RuntimeException("Lỗi gọi Gemini: " + e.getMessage());
        }
    }

    private String buildPronunciationPrompt(String expectedText) {
        return """
                Bạn là chuyên gia phát âm tiếng Việt, chuyên nghe audio và so sánh với văn bản chuẩn.
                Văn bản chuẩn (để tham khảo): "%s"

                Nhiệm vụ nghiêm ngặt:
                - NGHE CHÍNH XÁC từ audio, KHÔNG đoán hoặc tự sửa dựa trên văn bản chuẩn.
                - Nếu audio im lặng hoặc không nghe rõ: recognizedText = "Không nghe được âm thanh" và score = 0.
                - Nếu audio không khớp văn bản: ghi lỗi rõ ràng.
                - Trả về CHỈ JSON thuần túy, không thêm text nào khác.

                Cấu trúc JSON:
                {
                  "recognizedText": "text NGHE ĐƯỢC từ audio (KHÔNG đoán)",
                  "pronunciationScore": số nguyên 0-100 (0 nếu không nghe được),
                  "pronunciationErrors": [
                    {
                      "original": "từ/cụm từ chuẩn",
                      "recognized": "từ/cụm từ nghe được",
                      "index": vị trí bắt đầu trong văn bản chuẩn,
                      "type": "pronunciation|intonation|missing_word|extra_word|clarity|no_audio",
                      "explanation": "giải thích lỗi"
                    }
                  ]
                }

                Ví dụ 1: Audio im lặng
                Output: {"recognizedText":"Không nghe được âm thanh","pronunciationScore":0,"pronunciationErrors":[{"original":"","recognized":"","index":0,"type":"no_audio","explanation":"Audio không có âm thanh"}]}

                Bây giờ NGHE CHÍNH XÁC audio đã cho, KHÔNG đoán.
                """
                .formatted(expectedText);
    }

    private PronunciationHistory parsePronunciationResponse(String responseBody, String expectedText) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String finishReason = root.path("candidates").get(0).path("finishReason").asText();
            if ("MAX_TOKENS".equals(finishReason)) {
                log.warn("Gemini output bị cắt do MAX_TOKENS.");
            }

            String jsonText = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            log.info("Raw JSON from Gemini: {}", jsonText);

            if (!jsonText.startsWith("{") || !jsonText.endsWith("}")) {
                log.warn("JSON bị cắt, thử extract...");
                int start = jsonText.indexOf("{");
                int end = jsonText.lastIndexOf("}") + 1;
                if (start >= 0 && end > start) {
                    jsonText = jsonText.substring(start, end);
                }
            }

            PronunciationHistory history = objectMapper.readValue(jsonText, PronunciationHistory.class);
            history.setExpectedText(expectedText);
            if (history.getPronunciationErrors() == null) {
                history.setPronunciationErrors(new ArrayList<>());
            }
            return history;

        } catch (JsonProcessingException e) {
            log.error("Lỗi parse JSON từ Gemini. Raw: {}", responseBody, e);
            throw new RuntimeException("Gemini trả về JSON không hợp lệ.");
        } catch (Exception e) {
            log.error("Lỗi xử lý response Gemini", e);
            throw new RuntimeException("Lỗi xử lý phản hồi AI: " + e.getMessage());
        }
    }

    private Topic findTopicOrThrow(String id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Topic not found: " + id));
    }
}
