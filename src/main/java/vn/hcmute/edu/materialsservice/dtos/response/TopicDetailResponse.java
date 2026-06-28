package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicDetailResponse {
    private String id;
    private String name;
    private String description;
    private String level;
    private int sentenceCount;
    private List<String> sentences;
}
