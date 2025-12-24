package vn.hcmute.edu.materialsservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.hcmute.edu.materialsservice.Enum.ELearningStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "flashcard_progress")
public class FlashcardProgress {

    @Id
    private String id;

    private String userId;

    private String flashcardId;

    // Danh sách ID các word đã xem
    @Builder.Default
    private Set<String> viewedWordIds = new HashSet<>();

    // Tổng số word trong flashcard (cache để tính % tiến độ)
    private int totalWords;

    private ELearningStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime lastAccessedAt;

    // Helper methods
    public int getViewedWordCount() {
        return viewedWordIds != null ? viewedWordIds.size() : 0;
    }

    public double getProgress() {
        if (totalWords == 0)
            return 0.0;
        return (double) getViewedWordCount() / totalWords * 100;
    }

    public boolean isCompleted() {
        return totalWords > 0 && getViewedWordCount() >= totalWords;
    }
}
