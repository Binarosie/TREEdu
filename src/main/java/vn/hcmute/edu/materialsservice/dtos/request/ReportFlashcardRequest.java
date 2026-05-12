package vn.hcmute.edu.materialsservice.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportFlashcardRequest {

    @NotBlank(message = "Lý do báo cáo không được để trống")
    private String reason;
}
