package vn.hcmute.edu.materialsservice.Dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateQuizFromFileRequest {
    private MultipartFile file;           // PDF, DOCX, TXT
    private String topic;                 // Ví dụ: "Tiếng Anh", "Toán học"
    private Integer level;                // 1-6 (tùy chọn)
    private Integer questionCount ;   // Mặc định 10 câu
}
