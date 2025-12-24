package vn.hcmute.edu.materialsservice.dtos.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PronunciationCheckRequest {
    private MultipartFile audio;
    private String expectedText;
}
