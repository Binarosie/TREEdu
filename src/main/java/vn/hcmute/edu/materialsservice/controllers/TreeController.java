package vn.hcmute.edu.materialsservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.dtos.response.TreeResponse;
import vn.hcmute.edu.materialsservice.models.TreeEvent;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.TreeService;

import java.util.List;

@RestController
@RequestMapping("/api/tree")
@RequiredArgsConstructor
public class TreeController {

    private final TreeService treeService;
    private final UserRepository userRepository;

    @GetMapping("/my-tree")
    public ResponseEntity<TreeResponse> getMyTree(Authentication authentication) {
        return ResponseEntity.ok(treeService.getMyTree(getUserId(authentication)));
    }

    @PostMapping("/water")
    public ResponseEntity<TreeResponse> waterTree(Authentication authentication) {
        return ResponseEntity.ok(treeService.waterTree(getUserId(authentication)));
    }

    @GetMapping("/history")
    public ResponseEntity<List<TreeEvent>> getHistory(Authentication authentication) {
        return ResponseEntity.ok(treeService.getHistory(getUserId(authentication)));
    }

    // Xóa cái hàm getUserId(OAuth2User principal) cũ đi để tránh nhầm lẫn
    // Chỉ giữ lại hàm này:
    private String getUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User chưa đăng nhập!");
        }

        Object principal = authentication.getPrincipal();
        String email;

        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else if (principal instanceof CustomUserDetails customUserDetails) {
            email = customUserDetails.getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email))
                .getId();
    }
}
