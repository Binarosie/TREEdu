package vn.hcmute.edu.materialsservice.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictationCheckRequest {
    @NotNull(message = "Mã phân đoạn (segmentId) không được để trống")
    private Integer segmentId; // Chỉ cần truyền segmentId

    private String userText;   // Nội dung học viên gõ
}
