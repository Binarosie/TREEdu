package vn.hcmute.edu.materialsservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String receiverId;
    private String title;
    private String content;
    private String type;
    private Boolean isSeen;
    private LocalDateTime createdAt;
    private LocalDateTime seenAt;
    private Boolean deleted = false;
    private LocalDateTime deletedAt;
}