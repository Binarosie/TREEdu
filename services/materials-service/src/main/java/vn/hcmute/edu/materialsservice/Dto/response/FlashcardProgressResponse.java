package vn.hcmute.edu.materialsservice.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hcmute.edu.materialsservice.Enum.LearningStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardProgressResponse {

    private String id;
    private String userId;
    private String flashcardId;
    private String flashcardTitle;
    private Set<String> viewedWordIds;
    private int viewedWordCount;
    private int totalWords;
    private double progressPercentage;
    private LearningStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
}
