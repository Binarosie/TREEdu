package vn.hcmute.edu.materialsservice.Dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkWordViewedRequest {

    @NotBlank(message = "Word ID không được để trống")
    private String wordId;
}
