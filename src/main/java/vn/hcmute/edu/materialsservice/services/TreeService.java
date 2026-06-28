package vn.hcmute.edu.materialsservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.Enum.TreeStage;
import vn.hcmute.edu.materialsservice.dtos.response.TreeResponse;
import vn.hcmute.edu.materialsservice.models.Member;
import vn.hcmute.edu.materialsservice.models.TreeEvent;
import vn.hcmute.edu.materialsservice.models.UserTree;
import vn.hcmute.edu.materialsservice.repository.TreeEventRepository;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.repository.UserTreeRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreeService {

    private final UserTreeRepository userTreeRepository;
    private final TreeEventRepository treeEventRepository;
    private final UserRepository userRepository;   // ← dùng UserRepository thật của bạn

    // ─── GET MY TREE ────────────────────────────────────────────────────────────

    public TreeResponse getMyTree(String userId) {
        UserTree tree = getOrCreateTree(userId);
        return toResponse(tree);
    }

    // ─── WATER TREE (gọi sau mỗi lần học) ──────────────────────────────────────

    public TreeResponse waterTree(String userId) {
        UserTree tree = getOrCreateTree(userId);

        // findById đã có sẵn trong UserRepository, cast về Member
        Member member = (Member) userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 1. Tăng health
        int oldHealth = tree.getHealth();
        tree.setHealth(Math.min(oldHealth + 20, 100));

        // 2. Tính lại stage
        TreeStage oldStage = tree.getStage();
        TreeStage newStage = calculateStage(member);
        tree.setStage(newStage);

        // 3. Kiểm tra decoration mới
        checkAndUnlockDecorations(tree, member);

        tree.setLastHealthUpdate(LocalDateTime.now());
        tree.setUpdatedAt(LocalDateTime.now());
        userTreeRepository.save(tree);

        // 4. Ghi event nếu stage thay đổi
        if (!oldStage.equals(newStage)) {
            saveEvent(userId, "STAGE_UP", oldStage, newStage,
                    "Tree evolved from " + oldStage + " to " + newStage);
        }
        saveEvent(userId, "WATERED", newStage, newStage,
                "Tree watered. Health: " + oldHealth + " → " + tree.getHealth());

        return toResponse(tree);
    }

    // ─── HISTORY ────────────────────────────────────────────────────────────────

    public List<TreeEvent> getHistory(String userId) {
        return treeEventRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ─── DAILY DECAY (gọi từ ScheduledJobService) ───────────────────────────────

    public void applyDailyDecay(String userId, int daysSkipped) {
        userTreeRepository.findByUserId(userId).ifPresent(tree -> {
            int decay = switch (daysSkipped) {
                case 1 -> 15;
                case 2 -> 25;
                default -> 35;
            };
            int oldHealth = tree.getHealth();
            int newHealth = Math.max(oldHealth - decay, 10);
            tree.setHealth(newHealth);
            tree.setLastHealthUpdate(LocalDateTime.now());
            tree.setUpdatedAt(LocalDateTime.now());
            userTreeRepository.save(tree);

            saveEvent(userId, "HEALTH_DECAY", tree.getStage(), tree.getStage(),
                    "Missed " + daysSkipped + " day(s). Health: " + oldHealth + " → " + newHealth);
        });
    }

    // ─── GỌI KHI PERFECT QUIZ (từ QuizService) ──────────────────────────────────

    public void onPerfectQuiz(String userId) {
        userTreeRepository.findByUserId(userId).ifPresent(tree -> {
            tree.setFruits(tree.getFruits() + 1);
            tree.setUpdatedAt(LocalDateTime.now());
            userTreeRepository.save(tree);
            saveEvent(userId, "FRUIT_GAINED", tree.getStage(), tree.getStage(),
                    "Perfect quiz! Total fruits: " + tree.getFruits());
        });
    }

    // ─── GỌI KHI VÀO TOP LEADERBOARD ───────────────────────────────────────────

    public void onTopLeaderboard(String userId) {
        userTreeRepository.findByUserId(userId).ifPresent(tree -> {
            tree.setHasAura(true);
            tree.setUpdatedAt(LocalDateTime.now());
            userTreeRepository.save(tree);
        });
    }

    // ─── HELPERS ────────────────────────────────────────────────────────────────

    private UserTree getOrCreateTree(String userId) {
        return userTreeRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserTree t = UserTree.builder()
                            .userId(userId)
                            .stage(TreeStage.SEED)
                            .health(100)
                            .fruits(0).flowers(0)
                            .hasBird(false).hasAura(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .lastHealthUpdate(LocalDateTime.now())
                            .build();
                    return userTreeRepository.save(t);
                });
    }

    /**
     * ANCIENT → streak >= 30 AND level >= 20
     * FRUIT   → totalQuizCompleted >= 20 AND level >= 15
     * FLOWER  → streak >= 21 OR level >= 15
     * GROWING → streak >= 14 OR level >= 10
     * YOUNG   → streak >= 7  OR level >= 5
     * SPROUT  → streak >= 3  OR level >= 2
     * SEED    → default
     */
    public TreeStage calculateStage(Member member) {
        int streak  = member.getStreakCount();
        int level   = member.getLevel();
        int quizzes = member.getTotalQuizCompleted();

        if (streak >= 30 && level >= 20)  return TreeStage.ANCIENT;
        if (quizzes >= 20 && level >= 15) return TreeStage.FRUIT;
        if (streak >= 21 || level >= 15)  return TreeStage.FLOWER;
        if (streak >= 14 || level >= 10)  return TreeStage.GROWING;
        if (streak >= 7  || level >= 5)   return TreeStage.YOUNG;
        if (streak >= 3  || level >= 2)   return TreeStage.SPROUT;
        return TreeStage.SEED;
    }

    private void checkAndUnlockDecorations(UserTree tree, Member member) {
        // Mỗi 14-day streak → thêm 1 flower
        if (member.getStreakCount() > 0 && member.getStreakCount() % 14 == 0) {
            tree.setFlowers(tree.getFlowers() + 1);
            saveEvent(tree.getUserId(), "FLOWER_BLOOMED", tree.getStage(), tree.getStage(),
                    "Flower unlocked at streak " + member.getStreakCount());
        }
        // 30-day streak → mở khóa bird
        if (member.getStreakCount() >= 30) {
            tree.setHasBird(true);
        }
    }

    private void saveEvent(String userId, String type, TreeStage from, TreeStage to, String desc) {
        treeEventRepository.save(TreeEvent.builder()
                .userId(userId)
                .eventType(type)
                .fromStage(from)
                .toStage(to)
                .description(desc)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private TreeResponse toResponse(UserTree tree) {
        String status;
        if (tree.getHealth() < 10)      status = "CRITICAL";
        else if (tree.getHealth() < 30) status = "WILTING";
        else                            status = "HEALTHY";

        return TreeResponse.builder()
                .userId(tree.getUserId())
                .stage(tree.getStage())
                .health(tree.getHealth())
                .healthStatus(status)
                .fruits(tree.getFruits())
                .flowers(tree.getFlowers())
                .hasBird(tree.getHasBird())
                .hasAura(tree.getHasAura())
                .lastHealthUpdate(tree.getLastHealthUpdate())
                .build();
    }
}
