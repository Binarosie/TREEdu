package vn.hcmute.edu.materialsservice.Service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatusCode;
import vn.hcmute.edu.materialsservice.Dto.request.GenerateQuizFromFileRequest;
import vn.hcmute.edu.materialsservice.Dto.request.QuizRequest;
import vn.hcmute.edu.materialsservice.Dto.response.QuizEditResponse;
import vn.hcmute.edu.materialsservice.Dto.response.QuizResponse;
import vn.hcmute.edu.materialsservice.Mapper.QuizMapper;
import vn.hcmute.edu.materialsservice.Model.Quiz;
import vn.hcmute.edu.materialsservice.Repository.QuizRepository;
import vn.hcmute.edu.materialsservice.Service.QuizService;
import vn.hcmute.edu.materialsservice.exception.DuplicateResourceException;
import vn.hcmute.edu.materialsservice.exception.InvalidDataException;
import vn.hcmute.edu.materialsservice.exception.ResourceNotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final ObjectMapper objectMapper;
    private final vn.hcmute.edu.materialsservice.Repository.QuizAttemptRepository quizAttemptRepository;

    @Value("${gemini.api.key}")
    private String apiKey;
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";

    @Override
    public QuizResponse createQuiz(QuizRequest requestDTO) {

        // Check if quiz with same title already exists
        if (quizRepository.existsByTitle(requestDTO.getTitle())) {
            throw new DuplicateResourceException("Quiz with title '" + requestDTO.getTitle() + "' already exists");
        }

        // Validate questions
        validateQuestions(requestDTO);

        // Map DTO to entity
        Quiz quiz = quizMapper.toEntity(requestDTO);
        quiz.setDeleted(false); // Mặc định không bị xóa

        // Save quiz
        Quiz savedQuiz = quizRepository.save(quiz);

        log.info("Quiz created successfully with ID: {}", savedQuiz.getId());
        return quizMapper.toResponse(savedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResponse getQuizById(String id) {
        log.info("Fetching quiz with ID: {}", id);

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));

        return quizMapper.toResponse(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> getAllQuizzes(Pageable pageable) {
        log.info("Fetching all quizzes with pagination: page {}, size {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Quiz> quizPage = quizRepository.findAll(pageable);
        return quizPage.map(quizMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByTopic(String topic) {
        log.info("Fetching quizzes by topic: {}", topic);

        List<Quiz> quizzes = quizRepository.findByTopic(topic);
        return quizzes.stream()
                .map(quizMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByLevel(Integer level) {
        log.info("Fetching quizzes by level: {}", level);

        List<Quiz> quizzes = quizRepository.findByLevel(level);
        return quizzes.stream()
                .map(quizMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QuizResponse updateQuiz(String id, QuizRequest requestDTO) {
        log.info("Updating quiz with ID: {}", id);

        Quiz existingQuiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));

        // ============ KIỂM TRA QUIZ ĐÃ CÓ NGƯỜI LÀM CHƯA ============
        long attemptCount = quizAttemptRepository.countByQuizId(id);
        if (attemptCount > 0) {
            throw new IllegalStateException(
                    "Không thể cập nhật quiz này vì đã có " + attemptCount + " lượt làm bài. "
                            + "Chỉ được cập nhật quiz chưa có ai làm.");
        }
        // ==========================================================

        // Check if new title conflicts with another quiz
        if (!existingQuiz.getTitle().equals(requestDTO.getTitle())
                && quizRepository.existsByTitle(requestDTO.getTitle())) {
            throw new DuplicateResourceException("Quiz with title '" + requestDTO.getTitle() + "' already exists");
        }

        // Validate questions
        validateQuestions(requestDTO);

        // Update entity
        quizMapper.updateEntityFromDTO(requestDTO, existingQuiz);

        // Save updated quiz
        Quiz updatedQuiz = quizRepository.save(existingQuiz);

        log.info("Quiz updated successfully with ID: {}", updatedQuiz.getId());
        return quizMapper.toResponse(updatedQuiz);
    }

    @Override
    public void deleteQuiz(String id) {
        log.info("Soft deleting quiz with ID: {}", id);

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));

        // ============ KIỂM TRA QUIZ ĐÃ CÓ NGƯỜI LÀM CHƯA ============
        long attemptCount = quizAttemptRepository.countByQuizId(id);
        if (attemptCount > 0) {
            throw new IllegalStateException(
                    "Không thể xóa quiz này vì đã có " + attemptCount + " lượt làm bài. "
                            + "Chỉ được xóa quiz chưa có ai làm.");
        }
        // ==========================================================

        // Soft delete - chỉ đánh dấu deleted = true
        quiz.setDeleted(true);
        quiz.setDeletedAt(java.time.LocalDateTime.now());
        quizRepository.save(quiz);

        log.info("Quiz soft deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponse> searchQuizzesByTopic(String topic) {
        log.info("Searching quizzes by topic keyword: {}", topic);

        List<Quiz> quizzes = quizRepository.findByTopicIgnoreCase(topic);
        return quizzes.stream()
                .map(quizMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QuizEditResponse getQuizForEdit(String id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + id));

        return quizMapper.toEditResponse(quiz);
    }

    private void validateQuestions(QuizRequest requestDTO) {
        requestDTO.getQuestions().forEach(questionDTO -> {
            // Check if exactly one correct answer exists
            long correctAnswersCount = questionDTO.getOptions().stream()
                    .filter(answer -> answer.getIsCorrect() != null && answer.getIsCorrect())
                    .count();

            if (correctAnswersCount != 1) {
                throw new InvalidDataException(
                        "Each question must have exactly one correct answer. Question: '"
                                + questionDTO.getContent() + "' has " + correctAnswersCount + " correct answers");
            }
        });
    }

    @Override
    public QuizResponse generateQuizFromFile(GenerateQuizFromFileRequest request) throws IOException {
        MultipartFile file = request.getFile();
        if (file.isEmpty())
            throw new IllegalArgumentException("File không được để trống");

        if (file.getSize() > 10 * 1024 * 1024) { // Giới hạn 10MB
            throw new IllegalArgumentException("File quá lớn, tối đa 10MB");
        }

        byte[] fileBytes = file.getBytes();
        String base64File = Base64.getEncoder().encodeToString(fileBytes);
        String mimeType = getMimeType(file.getOriginalFilename());

        String prompt = buildHighQualityQuizPrompt(
                request.getTopic(),
                request.getLevel(),
                request.getQuestionCount(),
                file.getOriginalFilename());

        String geminiResponse = callGeminiWithFile(prompt, base64File, mimeType);
        String cleanJson = extractAndFixJsonFromResponse(geminiResponse);

        QuizRequest quizRequest;
        try {
            quizRequest = objectMapper.readValue(cleanJson, QuizRequest.class);
        } catch (Exception e) {
            log.error("Parse QuizRequest thất bại sau khi fix JSON. Clean JSON: {}", cleanJson, e);
            throw new InvalidDataException("AI trả về dữ liệu không hợp lệ, không thể tạo quiz.");
        }

        // Tái sử dụng createQuiz để validate + lưu
        return createQuiz(quizRequest);
    }

    private String buildHighQualityQuizPrompt(String topic, Integer level, Integer questionCount, String filename) {
        String safeTopic = topic != null && !topic.isBlank() ? topic : "tài liệu đính kèm";
        int safeLevel = (level != null && level >= 1 && level <= 6) ? level : 3;
        int safeCount = (questionCount != null && questionCount > 0 && questionCount <= 50) ? questionCount : 10;

        return """
                Bạn là chuyên gia tạo bài quiz trắc nghiệm chất lượng cao từ tài liệu học tập.

                File đính kèm: %s
                Yêu cầu:
                - Chủ đề chính: %s
                - Độ khó (level): %d (1=dễ nhất, 6=khó nhất)
                - Tạo đúng %d câu hỏi
                - Mỗi câu: đúng 4 đáp án A/B/C/D, chỉ 1 đáp án đúng
                - Có giải thích chi tiết, rõ ràng
                - Timer: %d phút (tự ước lượng hợp lý)

                QUAN TRỌNG: Trả về CHỈ JSON thuần túy, không thêm bất kỳ text nào khác.
                Không dùng ```json, không giải thích, không comment.

                Cấu trúc JSON chính xác (tuân thủ 100%%):
                {
                  "title": "Tiêu đề ngắn gọn, hấp dẫn về chủ đề",
                  "level": %d,
                  "topic": "%s",
                  "timer": "%d",
                  "questions": [
                    {
                      "content": "Nội dung câu hỏi đầy đủ?",
                      "options": [
                        {"content": "Đáp án A", "isCorrect": false},
                        {"content": "Đáp án B", "isCorrect": true},
                        {"content": "Đáp án C", "isCorrect": false},
                        {"content": "Đáp án D", "isCorrect": false}
                      ],
                      "explanation": "Giải thích chi tiết tại sao đáp án đúng và các đáp án sai vì sao sai."
                    }
                  ]
                }

                Bắt đầu tạo ngay sau khi đọc tài liệu.
                """.formatted(
                filename,
                safeTopic,
                safeLevel,
                safeCount,
                safeCount * 2, // timer = số phút = số câu * 2
                safeLevel,
                safeTopic,
                safeCount * 2); // timer truyền vào = số câu * 2
    }

    private String callGeminiWithFile(String prompt, String base64File, String mimeType) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", base64File))))),
                "generationConfig", Map.of( // Dùng generationConfig (chữ C hoa)
                        "temperature", 0.3,
                        "maxOutputTokens", 8192,
                        "topP", 0.95));

        return RestClient.create()
                .post()
                .uri(GEMINI_URL + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    String error = res.getBody() != null
                            ? StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8)
                            : "No body";
                    log.error("Gemini 4xx Error: {}", error);
                    throw new RuntimeException("Lỗi từ Gemini: " + error);
                })
                .body(String.class);
    }

    private String extractAndFixJsonFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText()
                    .trim();

            log.info("Raw output từ Gemini: {}", text);

            // Loại bỏ markdown
            if (text.startsWith("```json"))
                text = text.substring(7);
            if (text.startsWith("```"))
                text = text.substring(3);
            if (text.endsWith("```"))
                text = text.substring(0, text.length() - 3);
            text = text.trim();

            // Fallback: tìm block JSON lớn nhất
            if (!text.startsWith("{") || !text.endsWith("}")) {
                int start = text.indexOf("{");
                int end = text.lastIndexOf("}") + 1;
                if (start >= 0 && end > start) {
                    text = text.substring(start, end);
                    log.info("Đã extract JSON block: {}", text);
                }
            }

            return text;

        } catch (Exception e) {
            log.error("Lỗi extract JSON từ Gemini response: {}", responseBody, e);
            throw new RuntimeException("Không thể đọc phản hồi từ AI");
        }
    }

    private String getMimeType(String filename) {
        if (filename == null)
            return "application/octet-stream";
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    private String sanitizeJsonString(String json) {
        // Thay thế dấu nháy đơn bằng ngoặc kép
        json = json.replaceAll("(?<!\\\\)'", "\"");

        // Escape ngoặc kép bên trong chuỗi nếu chưa escape
        json = json.replaceAll("(?<!\\\\)\"", "\\\\\"");

        // Loại bỏ comment hoặc text thừa (nếu có)
        json = json.replaceAll("//.*|(?s)/\\*.*?\\*/", "");

        return json.trim();
    }
}
