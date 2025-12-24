package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.hcmute.edu.materialsservice.Enum.EWordForm;

import java.time.LocalDateTime;

@Document(collection = "words")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {

    @Id
    private String id;

    private String flashcardId;  // Liên kết với Flashcard

    private String newWord;

    private String meaning;

    private EWordForm wordForm;

    private String phoneme;

    private String imageURL;

    private String audioURL;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
