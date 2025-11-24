package vn.hcmute.edu.materialsservice.Dto.response;

import lombok.*;
import vn.hcmute.edu.materialsservice.Enum.WordForm;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordResponse {

    private String id;
    private String flashcardId;
    private String newWord;
    private String meaning;
    private WordForm wordForm;
    private String phoneme;
    private String imageURL;
    private String audioURL;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
