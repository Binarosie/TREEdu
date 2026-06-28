package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.*;
import vn.hcmute.edu.materialsservice.Enum.TreeStage;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LeaderboardResponse {
    private String type;
    private String period;
    private List<EntryDTO> entries;
    private Integer myRank;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EntryDTO {
        private Integer rank;
        private String userId;
        private String displayName;
        private String avatarUrl;  // 🌟 THÊM TRƯỜNG NÀY
        private Long value;
        private Integer level;
        private TreeStage treeStage;
        private Integer change;
    }
}
