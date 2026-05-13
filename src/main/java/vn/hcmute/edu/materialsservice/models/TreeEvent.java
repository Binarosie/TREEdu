package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.hcmute.edu.materialsservice.Enum.TreeStage;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tree_events")
public class TreeEvent {

    @Id
    private String id;

    @Field("userId")
    private String userId;

    // "STAGE_UP" | "HEALTH_DECAY" | "FRUIT_GAINED" | "FLOWER_BLOOMED" | "WATERED"
    @Field("event_type")
    private String eventType;

    @Field("from_stage")
    private TreeStage fromStage;

    @Field("to_stage")
    private TreeStage toStage;

    @Field("description")
    private String description;

    @Field("created_at")
    private LocalDateTime createdAt;
}
