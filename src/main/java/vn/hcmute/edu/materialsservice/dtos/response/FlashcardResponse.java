package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.*;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardVisibility;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardResponse {

    private String id;
    private String title;
    private String description;
    private Integer level;
    private String topic;
    // PHÂN LOẠI
    private String type; // SYSTEM | BY_MEMBER
    private String createdBy; // ID của người tạo (null nếu SYSTEM)
    // VISIBILITY
    private EFlashcardVisibility visibility;
    // VIOLATION
    private Boolean isViolated;
    // (optional) FE có thể dùng để check owner
    private Boolean isOwner;
    private Integer wordCount; // Số lượng từ
    private Integer reportCount; // Số lượt báo cáo
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
