package vn.hcmute.edu.materialsservice.dtos.request;

import lombok.Data;

@Data
public class SendNotificationRequest {
    private String receiverId;
    private String title;
    private String content;
}