package vn.hcmute.edu.materialsservice.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
