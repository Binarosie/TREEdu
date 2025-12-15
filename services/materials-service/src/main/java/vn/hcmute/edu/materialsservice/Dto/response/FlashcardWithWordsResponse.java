package vn.hcmute.edu.materialsservice.Dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardWithWordsResponse {

    private String id;
    private String title;
    private String description;
    private Integer level;
    private String topic;
    // PHÂN LOẠI
    private String type;
    private Boolean isOwner;
    private Integer wordCount;
    private List<WordResponse> words;  // Danh sách words
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
