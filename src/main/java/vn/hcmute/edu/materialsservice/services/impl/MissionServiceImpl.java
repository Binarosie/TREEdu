package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.Enum.EMissionType;
import vn.hcmute.edu.materialsservice.exceptions.ResourceNotFoundException;
import vn.hcmute.edu.materialsservice.models.UserDailyMission;
import vn.hcmute.edu.materialsservice.repository.UserDailyMissionRepository;
import vn.hcmute.edu.materialsservice.services.iUserService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MissionServiceImpl {

    private final UserDailyMissionRepository missionRepository;
    private final iUserService userService; // Để gọi hàm addXpToMember

    // Hàm tiện ích lấy múi giờ Việt Nam
    private LocalDate getToday() {
        return LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    // 1️⃣ LUỒNG 1: Lấy danh sách nhiệm vụ hôm nay (Tự động sinh nếu chưa có)
    public UserDailyMission getOrCreateDailyMissions(String userId) {
        LocalDate today = getToday();

        return missionRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> {
                    // Nếu là ngày mới, tạo mảng nhiệm vụ mới
                    List<UserDailyMission.MissionProgress> tasks = new ArrayList<>();

                    // Điểm danh (Mặc định luôn có) - 1/1
                    tasks.add(UserDailyMission.MissionProgress.builder()
                            .missionId(UUID.randomUUID().toString())
                            .type(EMissionType.CHECK_IN)
                            .title("Điểm danh ngày mới")
                            .targetCount(1).currentCount(0).rewardXp(10)
                            .isCompleted(false).isRewardClaimed(false).build());

                    // Làm 3 Quiz - 0/3
                    tasks.add(UserDailyMission.MissionProgress.builder()
                            .missionId(UUID.randomUUID().toString())
                            .type(EMissionType.DO_QUIZ_ANY)
                            .title("Hoàn thành 3 bài Quiz")
                            .targetCount(3).currentCount(0).rewardXp(50)
                            .isCompleted(false).isRewardClaimed(false).build());

                    // Học 2 bộ Flashcard - 0/2
                    tasks.add(UserDailyMission.MissionProgress.builder()
                            .missionId(UUID.randomUUID().toString())
                            .type(EMissionType.LEARN_FLASHCARD)
                            .title("Học 2 bộ Flashcard")
                            .targetCount(2).currentCount(0).rewardXp(40)
                            .isCompleted(false).isRewardClaimed(false).build());

                    UserDailyMission newDaily = UserDailyMission.builder()
                            .userId(userId).date(today).missions(tasks).build();

                    return missionRepository.save(newDaily);
                });
    }

    // 2️⃣ LUỒNG 2: Xử lý nút Điểm Danh (Check-in)
    public UserDailyMission checkIn(String userId) {
        // Đảm bảo đã có mission hôm nay
        UserDailyMission daily = getOrCreateDailyMissions(userId);

        boolean updated = false;
        for (UserDailyMission.MissionProgress m : daily.getMissions()) {
            if (m.getType() == EMissionType.CHECK_IN && !m.isCompleted()) {
                m.setCurrentCount(1);
                m.setCompleted(true);

                // 🚀 THÊM 2 DÒNG NÀY ĐỂ HOÀN THIỆN LOGIC 1-CLICK:
                m.setRewardClaimed(true); // Đánh dấu đã nhận thưởng luôn
                userService.addXpToMember(userId, m.getRewardXp()); // Cộng thẳng XP vào tài khoản

                updated = true;
                break;
            }
        }

        if (updated) {
            return missionRepository.save(daily);
        }
        throw new IllegalStateException("Hôm nay bạn đã điểm danh rồi!");
    }

    public UserDailyMission claimReward(String userId, String missionId) {
        UserDailyMission daily = missionRepository.findByUserIdAndDate(userId, getToday())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhiệm vụ hôm nay!"));

        UserDailyMission.MissionProgress mission = daily.getMissions().stream()
                .filter(m -> m.getMissionId().equals(missionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Nhiệm vụ không tồn tại!"));

        if (!mission.isCompleted()) {
            throw new IllegalStateException("Nhiệm vụ chưa hoàn thành (" + mission.getCurrentCount() + "/" + mission.getTargetCount() + "). Chưa thể nhận thưởng!");
        }
        if (mission.isRewardClaimed()) {
            throw new IllegalStateException("Bạn đã nhận thưởng của nhiệm vụ này rồi!");
        }

        // Đánh dấu đã nhận và gọi UserService cộng điểm
        mission.setRewardClaimed(true);
        userService.addXpToMember(userId, mission.getRewardXp());

        return missionRepository.save(daily);
    }

    // 💡 HÀM BỔ TRỢ: Dùng để chèn vào QuizService, FlashcardService (cộng currentCount)
    public void fireMissionEvent(String userId, EMissionType type, int amount) {
        missionRepository.findByUserIdAndDate(userId, getToday()).ifPresent(daily -> {
            boolean isUpdated = false;
            for (UserDailyMission.MissionProgress m : daily.getMissions()) {
                if (m.getType() == type && !m.isCompleted()) {
                    int newCount = m.getCurrentCount() + amount;
                    m.setCurrentCount(Math.min(newCount, m.getTargetCount()));

                    if (m.getCurrentCount() >= m.getTargetCount()) {
                        m.setCompleted(true);
                    }
                    isUpdated = true;
                }
            }
            if (isUpdated) {
                missionRepository.save(daily);
            }
        });
    }
}
