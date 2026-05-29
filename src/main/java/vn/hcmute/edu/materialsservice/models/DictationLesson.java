package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;
import vn.hcmute.edu.materialsservice.Enum.ELessonStatus;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "dictation_lessons")
public class DictationLesson {
    @Id
    private String id; // Để nguyên bản thế này thôi mày
    private String title;
    private String audioUrl;
    private String level;
    private List<AudioSegment> segments;

    @Builder.Default
    private ELessonStatus status = ELessonStatus.DRAFT;

}
