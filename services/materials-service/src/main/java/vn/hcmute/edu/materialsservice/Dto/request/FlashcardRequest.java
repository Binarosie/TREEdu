package vn.hcmute.edu.materialsservice.Dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 3, max = 200, message = "Tiêu đề phải từ 3-200 ký tự")
    private String title;

    @Size(max = 1000, message = "Mô tả không được quá 1000 ký tự")
    private String description;

    @NotNull(message = "Level không được để trống")
    @Min(value = 1, message = "Level phải từ 1-6")
    @Max(value = 6, message = "Level phải từ 1-6")
    private Integer level;

    @NotBlank(message = "Topic không được để trống")
    private String topic;
}
