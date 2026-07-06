package vn.hcmute.edu.materialsservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TTSService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final CloudinaryService cloudinaryService;

    @Value("${fpt.api-key}")
    private String fptApiKey;

    private static final String FPT_TTS_URL = "https://api.fpt.ai/hmi/tts/v5";
    private static final String VOICE = "banmai";

    /**
     * Gọi FPT AI để generate audio từ text
     *
     * @param word - từ cần đọc
     * @return URL của file audio
     */
    public String generateAudioUrl(String word) {
        if (word == null || word.trim().isEmpty()) {
            log.warn("Word is empty, skipping TTS generation");
            return null;
        }

        try {
            String response = webClient.post()
                    .uri(FPT_TTS_URL)
                    .header("api-key", fptApiKey)
                    .header("voice", VOICE)
                    .bodyValue(word)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);

                // Kiểm tra error code
                int errorCode = jsonNode.get("error").asInt();
                if (errorCode == 0) {
                    String audioUrl = jsonNode.get("async").asText();
                    log.info("TTS generated successfully for word: {}", word);
                    return audioUrl;
                } else {
                    String message = jsonNode.get("message").asText();
                    log.error("FPT TTS error: {}", message);
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("Error calling FPT TTS API for word: {}", word, e);
        }

        return null;
    }

    public String generateAudioAndStore(String word, String wordId) {
        if (word == null || word.trim().isEmpty()) {
            log.warn("Word is empty, skipping TTS generation");
            return null;
        }

        try {
            // 1. Generate audio từ FPT TTS API (nhận temporary URL)
            String tempAudioUrl = generateAudioUrl(word);

            if (tempAudioUrl == null) {
                log.warn("Failed to generate audio from FPT TTS for word: {}", word);
                return null;
            }

            log.debug("🎵 Generated temporary audio URL for word {}: {}", word, tempAudioUrl);

            // 2. Download từ temporary URL và upload lên Cloudinary
            String permanentUrl = cloudinaryService.downloadAndStoreAudio(tempAudioUrl, wordId);

            if (permanentUrl != null) {
                log.info(" Audio stored permanently for word {}: {}", word, permanentUrl);
            } else {
                log.warn("Failed to store audio to Cloudinary, using temporary URL as fallback");
                return tempAudioUrl; // Fallback: trả về URL tạm nếu lưu permanent thất bại
            }

            return permanentUrl;

        } catch (Exception e) {
            log.error("Error in generateAudioAndStore for word {}: {}", word, e.getMessage(), e);
            return null;
        }
    }
}