package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardType;
import java.time.LocalDateTime;

@Document(collection = "flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {
    @Id
    private String id;
    private String title;
    private String description;
    private Integer level;
    private String topic;

    // PHÂN LOẠI
    private EFlashcardType type;

    // OWNER (MEMBER tạo)
    private String createdBy; // userId (UUID string)

    private Boolean deleted = false;
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
