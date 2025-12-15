package vn.hcmute.edu.materialsservice.Dto.response;

import lombok.*;

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
    // (optional) FE có thể dùng để check owner
    private Boolean isOwner;
    private Integer wordCount;  // Số lượng từ
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
