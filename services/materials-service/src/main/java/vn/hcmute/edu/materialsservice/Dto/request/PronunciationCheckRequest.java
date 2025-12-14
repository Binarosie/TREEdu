package vn.hcmute.edu.materialsservice.Dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PronunciationCheckRequest {
    private MultipartFile audio;  // File audio ghi âm (webm/mp3)
    private String expectedText;  // Câu mẫu chuẩn để so sánh
}
