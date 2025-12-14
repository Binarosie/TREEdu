package vn.hcmute.edu.materialsservice.Service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import vn.hcmute.edu.materialsservice.Dto.request.PronunciationCheckRequest;
import vn.hcmute.edu.materialsservice.Dto.response.PronunciationCheckResponse;
import vn.hcmute.edu.materialsservice.Dto.response.TopicResponse;
import vn.hcmute.edu.materialsservice.Mapper.PronunciationMapper;
import vn.hcmute.edu.materialsservice.Mapper.TopicMapper;
import vn.hcmute.edu.materialsservice.Model.PronunciationHistory;
import vn.hcmute.edu.materialsservice.Model.Topic;
import vn.hcmute.edu.materialsservice.Repository.PronunciationRepository;
import vn.hcmute.edu.materialsservice.Repository.TopicRepository;
import vn.hcmute.edu.materialsservice.Service.PronunciationService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PronunciationServiceImpl implements PronunciationService {
    private final TopicMapper topicMapper;
    private final PronunciationRepository repository;
    private final TopicRepository topicRepository;
    private final PronunciationMapper mapper;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("${")) {
            log.error("GEMINI_API_KEY chưa được cấu hình! Vui lòng kiểm tra application.properties và environment variables.");
            throw new IllegalStateException("Gemini API key is missing!");
        }
        log.info("Gemini API key đã được load thành công (độ dài: {})", apiKey.length());
    }

    @Override
    public PronunciationCheckResponse checkAndSave(PronunciationCheckRequest request) {
        PronunciationHistory aiResult = callGeminiForPronunciation(request.getAudio(), request.getExpectedText());

        aiResult.setCreatedAt(LocalDateTime.now());
        PronunciationHistory saved = repository.save(aiResult);

        return mapper.toResponse(saved);
    }

    @Override
    public PronunciationCheckResponse getById(String id) {
        PronunciationHistory history = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found: " + id));
        return mapper.toResponse(history);
    }

    @Override
    public List<PronunciationCheckResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    public List<TopicResponse> getTopics() {  // ← Đổi kiểu trả về
        return topicRepository.findAll()
                .stream()
                .map(topicMapper::toResponse)
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())) // Sort A-Z
                .toList();
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
                                            "data", base64Audio
                                    ))
                            ))
                    ),
                    "generation_config", Map.of(
                            "temperature", 0.0,
                            "max_output_tokens", 4096
                    )
            );

            RestClient restClient = RestClient.create();

            String responseBody = restClient.post()
                    .uri(GEMINI_URL + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String errorBody = res.getBody() != null
                                ? StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8)
                                : "No body";
                        log.error("Gemini Pronunciation 4xx Error: {} - {}", res.getStatusCode(), errorBody);
                        throw new RuntimeException("Lỗi từ Gemini API (4xx): " + res.getStatusCode() + " - " + errorBody);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Gemini Pronunciation 5xx Error: {}", res.getStatusCode());
                        throw new RuntimeException("Lỗi server từ Gemini API (5xx): " + res.getStatusCode());
                    })
                    .body(String.class);

            log.debug("Full Gemini Pronunciation response: {}", responseBody);

            return parsePronunciationResponse(responseBody, expectedText);

        } catch (IOException e) {
            log.error("Lỗi đọc file audio", e);
            throw new RuntimeException("Không thể đọc file audio: " + e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini cho pronunciation", e);
            throw new RuntimeException("Lỗi gọi Gemini pronunciation: " + e.getMessage());
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
                  "explanation": "giải thích lỗi (ví dụ: 'Không nghe được âm thanh')"
                }
              ]
            }

            Ví dụ 1: Audio im lặng
            Output: {"recognizedText":"Không nghe được âm thanh","pronunciationScore":0,"pronunciationErrors":[{"original":"","recognized":"","index":0,"type":"no_audio","explanation":"Audio không có âm thanh"}]}

            Ví dụ 2: Audio đọc sai "Hôm nay trời đẹp quá, tôi đi chơi" thành "Hôm nay troi dep wa, tui di choi"
            Output: {"recognizedText":"Hôm nay troi dep wa, tui di choi","pronunciationScore":65,"pronunciationErrors":[{"original":"trời","recognized":"troi","index":8,"type":"pronunciation","explanation":"Thiếu dấu ngã"},{"original":"đẹp","recognized":"dep","index":13,"type":"pronunciation","explanation":"Thiếu dấu huyền và 'đ'"}]}

            Bây giờ NGHE CHÍNH XÁC audio đã cho, KHÔNG đoán.
            """.formatted(expectedText);
    }
    private PronunciationHistory parsePronunciationResponse(String responseBody, String expectedText) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String finishReason = root.path("candidates").get(0).path("finishReason").asText();
            if ("MAX_TOKENS".equals(finishReason)) {
                log.warn("Gemini pronunciation output bị cắt do hết token.");
            }

            String jsonText = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            log.info("Raw JSON from Gemini Pronunciation: {}", jsonText);

            // Fallback nếu JSON bị cắt
            if (!jsonText.startsWith("{") || !jsonText.endsWith("}")) {
                log.warn("JSON pronunciation bị cắt, thử extract...");
                int start = jsonText.indexOf("{");
                int end = jsonText.lastIndexOf("}") + 1;
                if (start >= 0 && end > start) {
                    jsonText = jsonText.substring(start, end);
                    log.info("Extracted JSON: {}", jsonText);
                }
            }

            PronunciationHistory history = objectMapper.readValue(jsonText, PronunciationHistory.class);
            history.setExpectedText(expectedText);

            if (history.getPronunciationErrors() == null) {
                history.setPronunciationErrors(new ArrayList<>());
            }

            return history;

        } catch (JsonProcessingException e) {
            log.error("Lỗi parse JSON pronunciation từ Gemini. Raw response: {}", responseBody, e);
            throw new RuntimeException("Gemini trả về JSON không hợp lệ cho pronunciation.");
        } catch (Exception e) {
            log.error("Lỗi xử lý response pronunciation từ Gemini", e);
            throw new RuntimeException("Lỗi xử lý phản hồi pronunciation từ AI: " + e.getMessage());
        }
    }
}
