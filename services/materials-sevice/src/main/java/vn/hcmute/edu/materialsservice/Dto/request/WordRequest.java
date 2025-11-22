package vn.hcmute.edu.materialsservice.Dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hcmute.edu.materialsservice.Enum.WordForm;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordRequest {

    @NotBlank(message = "Từ mới không được để trống")
    private String newWord;

    @NotBlank(message = "Nghĩa không được để trống")
    private String meaning;

    @NotNull(message = "Loại từ không được để trống")
    private WordForm wordForm;

    private String imageURL;

    private String phoneme;

    private String audioURL;
}
