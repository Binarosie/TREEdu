package vn.hcmute.edu.materialsservice.dtos.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicRequest {
    private String name;
    private String description;
    private String level;
    private List<String> sentences;
}
