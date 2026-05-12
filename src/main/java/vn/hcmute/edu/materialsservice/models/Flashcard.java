package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardType;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardVisibility;
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

    // VISIBILITY: PRIVATE (chỉ tác giả) hoặc PUBLIC (mọi người)
    private EFlashcardVisibility visibility = EFlashcardVisibility.PRIVATE;

    // VI PHẠM: Nếu true, flashcard không thể chuyển về PUBLIC
    private Boolean isViolated = false;

    private Boolean deleted = false;
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
