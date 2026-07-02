package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.hcmute.edu.materialsservice.Enum.TreeStage;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "leaderboard_snapshots")
@CompoundIndex(def = "{'type': 1, 'period': 1}", unique = true)
public class LeaderboardSnapshot {

    @Id
    private String id;

    @Field("type")
    private String type;

    @Field("period")
    private String period;

    @Field("entries")
    private List<LeaderboardEntry> entries;

    @Field("generated_at")
    private LocalDateTime generatedAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntry {
        private Integer rank;
        private String userId;
        private String displayName;
        private String avatarUrl;
        private Long value;
        private Integer level;
        private TreeStage treeStage;
        private Integer change;
    }
}
