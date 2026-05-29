package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import vn.hcmute.edu.materialsservice.Enum.ELessonStatus;
import vn.hcmute.edu.materialsservice.dtos.request.DictationCheckRequest;
import vn.hcmute.edu.materialsservice.dtos.request.DictationCreateRequest;
import vn.hcmute.edu.materialsservice.dtos.request.DictationUpdateRequest;
import vn.hcmute.edu.materialsservice.dtos.response.DictationCheckResponse;
import vn.hcmute.edu.materialsservice.models.AudioSegment;
import vn.hcmute.edu.materialsservice.models.DictationLesson;
import vn.hcmute.edu.materialsservice.repository.DictationLessonRepository;
import vn.hcmute.edu.materialsservice.services.iDictationService;

import org.springframework.http.HttpHeaders;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j

public class DictationServiceImpl implements iDictationService {

    private final DictationLessonRepository lessonRepository;

    private final RestTemplate restTemplate;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public DictationServiceImpl(DictationLessonRepository lessonRepository, RestTemplate restTemplate) {
        this.lessonRepository = lessonRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public DictationLesson createLesson(DictationCreateRequest request) {
        DictationLesson lesson = DictationLesson.builder()
                .title(request.getTitle())
                .audioUrl(request.getAudioUrl())
                .level(request.getLevel())
                .segments(request.getSegments())
                .build();
        return lessonRepository.save(lesson);
    }

    @Override
    public List<DictationLesson> getAllLessonsForMember() {
        return lessonRepository.findAllByStatus(ELessonStatus.PUBLISHED);
    }

    @Override
    public List<DictationLesson> getAllLessonsForAdmin() {
        return lessonRepository.findAll();
    }

    @Override
    public DictationLesson getLessonById(String id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài nghe chính tả ID: " + id));
    }

    @Override
    public DictationCheckResponse checkAnswer(String dictationId, DictationCheckRequest request) {
        DictationLesson lesson = lessonRepository.findById(dictationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài nghe chính tả ID: " + dictationId));

        AudioSegment targetSegment = lesson.getSegments().stream()
                .filter(seg -> seg.getId().equals(request.getSegmentId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phân đoạn số: " + request.getSegmentId()));

        String original = targetSegment.getTranscript();
        String userText = request.getUserText();

        Double accuracy = calculateLevenshteinAccuracy(original, userText);
        Boolean passed = accuracy >= 80.0;
        List<DictationCheckResponse.WordDiff> wordDetails = compareWordByWord(original, userText);

        return DictationCheckResponse.builder()
                .accuracy(accuracy)
                .passed(passed)
                .correctAnswer(original)
                .wordDetails(wordDetails)
                .build();
    }

    @Override
    public void updateLessonStatus(String id, ELessonStatus status) {
        DictationLesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài nghe ID: " + id));
        lesson.setStatus(status);
        lessonRepository.save(lesson);
    }

    @Override
    public DictationLesson generateLessonWithAI(MultipartFile file, String title, String level, String audioUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : "audio/mpeg"
            ));
            HttpEntity<ByteArrayResource> fileEntity = new HttpEntity<>(fileResource, fileHeaders);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileEntity);
            body.add("title", title);
            body.add("level", level);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Gửi yêu cầu sang AI Service: {}", aiServiceUrl);
            ResponseEntity<DictationCreateRequest> response = restTemplate.postForEntity(
                    aiServiceUrl, requestEntity, DictationCreateRequest.class);

            DictationCreateRequest aiResult = response.getBody();
            if (aiResult == null || aiResult.getSegments() == null || aiResult.getSegments().isEmpty()) {
                throw new RuntimeException("AI Service không trả về dữ liệu hợp lệ!");

            }

            // Ghi đè URL từ Python bằng URL thực từ BE Java
            aiResult.setAudioUrl(audioUrl);

            log.info("Lưu bài nghe vào MongoDB với {} segments", aiResult.getSegments().size());
            return this.createLesson(aiResult);

        } catch (Exception e) {
            log.error("Lỗi AI xử lý âm thanh: ", e);
            throw new RuntimeException("Lỗi AI xử lý âm thanh: " + e.getMessage());
        }
    }

    @Override
    public DictationLesson updateLesson(String id, DictationUpdateRequest request) {
        DictationLesson existingLesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài nghe ID để cập nhật: " + id));

        if (request.getTitle() != null) {
            existingLesson.setTitle(request.getTitle());
        }
        if (request.getLevel() != null) {
            existingLesson.setLevel(request.getLevel());
        }
        if (request.getSegments() != null) {
            existingLesson.setSegments(request.getSegments());
        }

        return lessonRepository.save(existingLesson);
    }

    // --- Các hàm Utils toán học/chuẩn hóa giữ nguyên tính chặt chẽ ---
    private Double calculateLevenshteinAccuracy(String original, String userText) {
        String cleanOrigin = normalize(original);
        String cleanUser = normalize(userText);

        if (cleanOrigin.isEmpty() && cleanUser.isEmpty()) return 100.0;
        if (cleanOrigin.isEmpty() || cleanUser.isEmpty()) return 0.0;

        LevenshteinDistance distanceCalc = new LevenshteinDistance();
        int distance = distanceCalc.apply(cleanOrigin, cleanUser);

        int maxLength = Math.max(cleanOrigin.length(), cleanUser.length());
        double accuracy = (1.0 - ((double) distance / maxLength)) * 100;
        return Math.round(accuracy * 10.0) / 10.0;
    }

    private List<DictationCheckResponse.WordDiff> compareWordByWord(String original, String userText) {
        List<DictationCheckResponse.WordDiff> details = new ArrayList<>();
        String[] originWords = original.replaceAll("[.,\\/#!$%\\^&\\*;:{}=\\-_`~()?]", "").split("\\s+");
        String[] userWords = userText != null ? userText.replaceAll("[.,\\/#!$%\\^&\\*;:{}=\\-_`~()?]", "").split("\\s+") : new String[0];

        for (int i = 0; i < originWords.length; i++) {
            String origWord = originWords[i];
            if (i < userWords.length) {
                String userWord = userWords[i];
                if (normalize(origWord).equals(normalize(userWord))) {
                    details.add(new DictationCheckResponse.WordDiff(origWord, "CORRECT"));
                } else {
                    details.add(new DictationCheckResponse.WordDiff(userWord + " (" + origWord + ")", "INCORRECT"));
                }
            } else {
                details.add(new DictationCheckResponse.WordDiff("Missing: " + origWord, "INCORRECT"));
            }
        }
        return details;
    }

    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase().replaceAll("[.,\\/#!$%\\^&\\*;:{}=\\-_`~()?]", "").trim();
    }
}
