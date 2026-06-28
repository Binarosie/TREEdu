package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.models.Member;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.services.IStreakService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreakServiceImpl implements IStreakService {

    private final UserRepository userRepository;

    @Override
    public void updateStreak(Member member) {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        LocalDate lastStudyDate = member.getLastStudyDate();

        int currentStreak = Optional
                .ofNullable(member.getStreakCount())
                .orElse(0);

        int longestStreak = Optional
                .ofNullable(member.getLongestStreak())
                .orElse(0);

        // User học lần đầu
        if (lastStudyDate == null) {
            member.setStreakCount(1);
        }

        // Hôm nay đã học rồi -> không cộng nữa
        else if (lastStudyDate.equals(today)) {
            return;
        }

        // Học liên tiếp
        else if (lastStudyDate.equals(today.minusDays(1))) {
            member.setStreakCount(currentStreak + 1);
        }

        // Bị mất streak
        else {
            member.setStreakCount(1);
        }

        // Update ngày học gần nhất
        member.setLastStudyDate(today);

        // Update longest streak
        if (member.getStreakCount() > longestStreak) {
            member.setLongestStreak(member.getStreakCount());
        }

        userRepository.save(member);
    }
}
