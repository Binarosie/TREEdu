package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "users")
public class Member extends User {

    @Builder.Default
    private Integer streakCount = 0;

    @Builder.Default
    private Integer longestStreak = 0;

    private LocalDate lastStudyDate;

    @Builder.Default
    private Integer xp = 0;

    @Builder.Default
    private Integer totalQuizCompleted = 0;

    @Builder.Default
    private Integer totalFlashcardLearned = 0;


    @Builder.Default
    private Integer level = 1;
}
