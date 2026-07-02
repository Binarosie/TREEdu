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

    private String phoneNumber;
    private String avatarUrl;
    private Integer birthYear;
    private String address;
    private String gender;

    private Integer streakCount;
    private Integer longestStreak;
    private Integer xp;
    private Integer level;
    private Integer totalQuizCompleted;
    private Integer totalFlashcardLearned;
    private LocalDate lastStudyDate;

    private Integer xpNeededForNextLevel; // Số XP cần thêm (ví dụ: 400 XP để lên cấp)
    private Integer currentLevelProgressXp; // Số XP đã tích lũy tính riêng trong level này
    private Double progressPercentage; // Phần trăm thanh tiến trình (ví dụ: 50.0%)
}
