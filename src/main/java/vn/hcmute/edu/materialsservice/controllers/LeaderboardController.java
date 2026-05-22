package vn.hcmute.edu.materialsservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.dtos.response.LeaderboardResponse;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.LeaderboardService;

import java.util.Map;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final UserRepository userRepository;

    /**
     * GET /api/leaderboard/weekly-xp
     */
    @GetMapping("/weekly-xp")
    public ResponseEntity<LeaderboardResponse> getWeeklyXp(Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(leaderboardService.getWeeklyXp(userId));
    }

    /**
     * GET /api/leaderboard/streak
     */
    @GetMapping("/streak")
    public ResponseEntity<LeaderboardResponse> getStreak(Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(leaderboardService.getStreak(userId));
    }

    /**
     * GET /api/leaderboard/total-xp
     */
    @GetMapping("/total-xp")
    public ResponseEntity<LeaderboardResponse> getTotalXp(Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(leaderboardService.getTotalXp(userId));
    }

    /**
     * GET /api/leaderboard/my-rank
     * Response: { "WEEKLY_XP": 5, "STREAK": 12, "TOTAL_XP": 8 }
     */
    @GetMapping("/my-rank")
    public ResponseEntity<Map<String, Integer>> getMyRank(Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(leaderboardService.getMyRanks(userId));
    }

    /**
     * POST /api/leaderboard/rebuild
     * API này giúp bạn ép hệ thống làm mới bảng xếp hạng ngay lập tức để test trên Postman
     */
    @PostMapping("/rebuild")
    public ResponseEntity<String> rebuildLeaderboard() {
        leaderboardService.rebuildAllSnapshots();
        return ResponseEntity.ok("Leaderboard snapshots rebuilt successfully!");
    }

    // ─── Helper: Lấy User.id từ Authentication (Hybrid: OAuth2 & Email/Pass) ──────
    private String getUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User chưa đăng nhập hoặc Token không hợp lệ!");
        }

        Object principal = authentication.getPrincipal();
        String email;

        if (principal instanceof OAuth2User oauth2User) {
            // Trường hợp login bằng Google/Facebook
            email = oauth2User.getAttribute("email");
        } else if (principal instanceof CustomUserDetails customUserDetails) {
            // Trường hợp login bằng Email/Password (khớp với AuthController của bạn)
            email = customUserDetails.getUsername();
        } else {
            // Trường hợp dự phòng nếu principal là String (Username)
            email = principal.toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User với email: " + email))
                .getId();
    }
}
