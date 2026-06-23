package vn.hcmute.edu.materialsservice.models;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class NotificationEvent {
    private String receiverId;
    private List<String> receiverIds;
    private String title;
    private String content;
    private String type;
}