package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.*;
import vn.hcmute.edu.materialsservice.Enum.EWordForm;

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
    private EWordForm wordForm;
    private String phoneme;
    private String imageURL;
    private String audioURL;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
