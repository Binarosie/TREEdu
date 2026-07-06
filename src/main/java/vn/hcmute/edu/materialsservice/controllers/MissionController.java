package vn.hcmute.edu.materialsservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.models.UserDailyMission;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.impl.MissionServiceImpl;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionServiceImpl missionService;

    // Helper lấy userId từ Auth
    private String getUserId(Authentication auth) {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUser().getId().toString();
    }

    // Lấy danh sách nhiệm vụ hôm nay (1/3, 2/3...)
    @GetMapping("/daily")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public ResponseEntity<UserDailyMission> getDailyMissions(Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(missionService.getOrCreateDailyMissions(userId));
    }

    // Điểm danh ngày mới
    @PostMapping("/check-in")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public ResponseEntity<UserDailyMission> checkIn(Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(missionService.checkIn(userId));
    }

    // Nhận thưởng XP (Cần ID của nhiệm vụ)
    @PostMapping("/{missionId}/claim-reward")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public ResponseEntity<UserDailyMission> claimReward(
            @PathVariable String missionId,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(missionService.claimReward(userId, missionId));
    }
}
