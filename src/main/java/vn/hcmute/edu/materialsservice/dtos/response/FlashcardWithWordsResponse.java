package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.*;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardVisibility;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardWithWordsResponse {

    private String id;
    private String title;
    private String description;
    private Integer level;
    private String topic;
    private String type;
    private String createdBy;
    private EFlashcardVisibility visibility;
    private Boolean isViolated;
    private Boolean isOwner;
    private Integer wordCount;
    private List<WordResponse> words;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
