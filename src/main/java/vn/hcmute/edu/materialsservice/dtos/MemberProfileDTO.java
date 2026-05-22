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

    // === Field mới từ User ===
    private String phoneNumber;
    private String avatarUrl;
    private Integer birthYear;
    private String address;
    private String gender;

    // === Field từ Member ===
    private Integer streakCount;
    private Integer longestStreak;
    private Integer xp;
    private Integer level;
    private Integer totalQuizCompleted;
    private Integer totalFlashcardLearned;
    private LocalDate lastStudyDate;
}
