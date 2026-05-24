package vn.hcmute.edu.materialsservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.hcmute.edu.materialsservice.Enum.EMissionType;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_daily_missions")
public class UserDailyMission {
    @Id
    private String id;

    @Indexed
    private String userId;

    private LocalDate date; // Mấu chốt để sang ngày mới hệ thống tự reset

    private List<MissionProgress> missions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissionProgress {
        private String missionId;
        private EMissionType type;
        private String title;
        private int targetCount;        // Mục tiêu (Ví dụ: 3)
        private int currentCount;       // Đã làm (Ví dụ: 1)
        private int rewardXp;           // XP thưởng (Ví dụ: 50)
        private boolean isCompleted;    // true khi current >= target
        private boolean isRewardClaimed;// true khi đã bấm nút Claim
    }
}
