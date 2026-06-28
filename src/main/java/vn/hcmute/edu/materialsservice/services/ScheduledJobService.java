package vn.hcmute.edu.materialsservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.models.Member;
import vn.hcmute.edu.materialsservice.models.User;
import vn.hcmute.edu.materialsservice.repository.UserRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobService {

    private final UserRepository userRepository;   // ← UserRepository thật của bạn
    private final TreeService treeService;
    private final LeaderboardService leaderboardService;

    /**
     * Chạy mỗi ngày lúc 00:01 — giảm health cây của những user không học hôm qua.
     */
    @Scheduled(cron = "0 1 0 * * *")
    public void dailyTreeDecay() {
        log.info("Running daily tree decay job...");

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            // Bỏ qua nếu không phải Member (ví dụ Admin)
            if (!(user instanceof Member member)) continue;
            if (member.getLastStudyDate() == null) continue;

            long daysSkipped = ChronoUnit.DAYS.between(member.getLastStudyDate(), LocalDate.now());
            if (daysSkipped >= 1) {
                treeService.applyDailyDecay(member.getId(), (int) daysSkipped);
            }
        }

        log.info("Daily tree decay job completed.");
    }

    /**
     * Rebuild leaderboard cache mỗi 30 phút.
     */
    @Scheduled(fixedRate = 1 * 60 * 1000)
    public void rebuildLeaderboard() {
        log.info("Rebuilding leaderboard snapshots...");
        leaderboardService.rebuildAllSnapshots();
    }
}
