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
    private Integer wordCount;  // Số lượng từ
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
