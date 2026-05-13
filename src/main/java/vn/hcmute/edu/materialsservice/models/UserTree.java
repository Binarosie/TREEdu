package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.hcmute.edu.materialsservice.Enum.TreeStage;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_trees")
public class UserTree {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("userId")
    private String userId;   // = User.id (MongoDB _id của users collection)

    @Builder.Default
    @Field("stage")
    private TreeStage stage = TreeStage.SEED;

    @Builder.Default
    @Field("health")
    private Integer health = 100;

    @Builder.Default
    @Field("fruits")
    private Integer fruits = 0;

    @Builder.Default
    @Field("flowers")
    private Integer flowers = 0;

    @Builder.Default
    @Field("has_bird")
    private Boolean hasBird = false;

    @Builder.Default
    @Field("has_aura")
    private Boolean hasAura = false;

    @Field("last_health_update")
    private LocalDateTime lastHealthUpdate;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}
