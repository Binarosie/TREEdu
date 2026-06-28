package vn.hcmute.edu.materialsservice.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    @NotBlank(message = "Lý do yêu cầu review không được để trống")
    private String reason;
}
