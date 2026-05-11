package vn.hcmute.edu.materialsservice.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileDTO {

    private String id;

    private String fullName;

    private String email;

    private Integer streakCount;

    private Integer longestStreak;

    private Integer xp;

    private Integer level;

    private Integer totalQuizCompleted;

    private Integer totalFlashcardLearned;

    private LocalDate lastStudyDate;
}
