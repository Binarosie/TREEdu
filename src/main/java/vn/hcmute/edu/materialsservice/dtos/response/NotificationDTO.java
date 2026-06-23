package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDTO {
    private String id;
    private String receiverId;
    private String title;
    private String content;
    private String type;
    private Boolean isSeen;
    private LocalDateTime createdAt;
    private LocalDateTime seenAt;
}