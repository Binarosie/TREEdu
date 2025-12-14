package vn.hcmute.edu.materialsservice.Dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicResponse {
    private String id;
    private String name;
    private String description;
    private String level;
    private int sentenceCount;
}
