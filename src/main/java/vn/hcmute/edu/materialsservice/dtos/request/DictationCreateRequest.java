package vn.hcmute.edu.materialsservice.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import vn.hcmute.edu.materialsservice.models.AudioSegment;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictationCreateRequest {
    @NotBlank(message = "Tiêu đề bài học không được để trống")
    private String title;

    @NotBlank(message = "Đường dẫn file Audio không được để trống")
    private String audioUrl;

    @NotBlank(message = "Trình độ không được để trống")
    private String level;

    @NotEmpty(message = "Danh sách phân đoạn audio không được để trống")
    private List<AudioSegment> segments;
}
