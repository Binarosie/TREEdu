package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.*;
import vn.hcmute.edu.materialsservice.Enum.TreeStage;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TreeResponse {
    private String userId;
    private TreeStage stage;
    private Integer health;
    private String healthStatus;   // "HEALTHY" | "WILTING" | "CRITICAL"
    private Integer fruits;
    private Integer flowers;
    private Boolean hasBird;
    private Boolean hasAura;
    private LocalDateTime lastHealthUpdate;
}

