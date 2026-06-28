package vn.hcmute.edu.materialsservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.Enum.TreeStage;
import vn.hcmute.edu.materialsservice.dtos.response.LeaderboardResponse;
import vn.hcmute.edu.materialsservice.models.LeaderboardSnapshot;
import vn.hcmute.edu.materialsservice.repository.LeaderboardSnapshotRepository;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final MongoTemplate mongoTemplate;
    private final LeaderboardSnapshotRepository snapshotRepository;

    // ─── PUBLIC APIs ────────────────────────────────────────────────────────────

    public LeaderboardResponse getWeeklyXp(String currentUserId) {
        String period = getCurrentWeekPeriod();
        LeaderboardSnapshot snapshot = snapshotRepository
                .findByTypeAndPeriod("WEEKLY_XP", period)
                .orElseGet(() -> buildAndSaveSnapshot("WEEKLY_XP", period));
        return toResponse(snapshot, currentUserId);
    }

    public LeaderboardResponse getStreak(String currentUserId) {
        LeaderboardSnapshot snapshot = snapshotRepository
                .findByTypeAndPeriod("STREAK", "ALL_TIME")
                .orElseGet(() -> buildAndSaveSnapshot("STREAK", "ALL_TIME"));
        return toResponse(snapshot, currentUserId);
    }

    public LeaderboardResponse getTotalXp(String currentUserId) {
        LeaderboardSnapshot snapshot = snapshotRepository
                .findByTypeAndPeriod("TOTAL_XP", "ALL_TIME")
                .orElseGet(() -> buildAndSaveSnapshot("TOTAL_XP", "ALL_TIME"));
        return toResponse(snapshot, currentUserId);
    }

    public Map<String, Integer> getMyRanks(String currentUserId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String weekPeriod = getCurrentWeekPeriod();

        List.of(
                Map.entry("WEEKLY_XP", weekPeriod),
                Map.entry("STREAK",    "ALL_TIME"),
                Map.entry("TOTAL_XP",  "ALL_TIME")
        ).forEach(e ->
                snapshotRepository.findByTypeAndPeriod(e.getKey(), e.getValue())
                        .ifPresent(snapshot -> snapshot.getEntries().stream()
                                .filter(entry -> currentUserId.equals(entry.getUserId()))
                                .findFirst()
                                .ifPresent(entry -> result.put(e.getKey(), entry.getRank())))
        );
        return result;
    }

    public void rebuildAllSnapshots() {
        String weekPeriod = getCurrentWeekPeriod();
        buildAndSaveSnapshot("WEEKLY_XP", weekPeriod);
        buildAndSaveSnapshot("STREAK",    "ALL_TIME");
        buildAndSaveSnapshot("TOTAL_XP",  "ALL_TIME");
        log.info("Leaderboard snapshots rebuilt at {}", LocalDateTime.now());
    }

    // ─── BUILD SNAPSHOT ─────────────────────────────────────────────────────────

    private LeaderboardSnapshot buildAndSaveSnapshot(String type, String period) {
        Map<String, Integer> previousRanks = snapshotRepository
                .findByTypeAndPeriod(type, period)
                .map(old -> old.getEntries().stream()
                        .collect(Collectors.toMap(
                                LeaderboardSnapshot.LeaderboardEntry::getUserId,
                                LeaderboardSnapshot.LeaderboardEntry::getRank)))
                .orElse(Collections.emptyMap());

        String sortField = switch (type) {
            case "STREAK"    -> "streakCount";
            default          -> "xp";
        };

        MatchOperation matchMember = Aggregation.match(Criteria.where("_class").regex(".*Member$"));
        MatchOperation matchScore = Aggregation.match(Criteria.where(sortField).gt(0));
        SortOperation sort = Aggregation.sort(Sort.by(Sort.Direction.DESC, sortField));
        LimitOperation limit = Aggregation.limit(100);

        LookupOperation lookup = LookupOperation.newLookup()
                .from("user_trees")
                .localField("_id")
                .foreignField("userId")
                .as("tree_data_list");

        // 🌟 THÊM "avatar_url" VÀO PROJECTION
        ProjectionOperation project = Aggregation.project("_id", "full_name", "email", "xp", "level", "streakCount", "avatar_url")
                .and("tree_data_list").arrayElementAt(0).as("tree_info");

        Aggregation agg = Aggregation.newAggregation(matchMember, matchScore, sort, limit, lookup, project);

        List<Map> raw = mongoTemplate.aggregate(agg, "users", Map.class).getMappedResults();

        List<LeaderboardSnapshot.LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            Map row = raw.get(i);
            String userId = row.get("_id").toString();

            String displayName = "Người dùng ẩn danh";
            if (row.get("full_name") != null) {
                displayName = row.get("full_name").toString();
            } else if (row.get("email") != null) {
                displayName = row.get("email").toString().split("@")[0];
            }

            // 🌟 LẤY AVATAR URL TỪ KẾT QUẢ AGGREGATION
            String avatarUrl = row.get("avatar_url") != null ? row.get("avatar_url").toString() : null;

            long value = ((Number) Objects.requireNonNullElse(row.get(sortField), 0)).longValue();
            int level = ((Number) Objects.requireNonNullElse(row.get("level"), 1)).intValue();

            TreeStage treeStage = null;
            if (row.get("tree_info") instanceof Map treeMap) {
                Object stageObj = treeMap.get("stage");
                if (stageObj != null) {
                    try {
                        treeStage = TreeStage.valueOf(stageObj.toString().toUpperCase().trim());
                    } catch (IllegalArgumentException e) {
                        log.warn("Không thể parse Stage: {}", stageObj);
                    }
                }
            }
            int currentRank = i + 1;
            int change = previousRanks.containsKey(userId) ? previousRanks.get(userId) - currentRank : 0;

            entries.add(LeaderboardSnapshot.LeaderboardEntry.builder()
                    .rank(currentRank)
                    .userId(userId)
                    .displayName(displayName)
                    .avatarUrl(avatarUrl) // 🌟 GÁN VÀO SNAPSHOT
                    .value(value)
                    .level(level)
                    .treeStage(treeStage)
                    .change(change)
                    .build());
        }

        LeaderboardSnapshot snapshot = LeaderboardSnapshot.builder()
                .type(type)
                .period(period)
                .entries(entries)
                .generatedAt(LocalDateTime.now())
                .build();

        snapshotRepository.findByTypeAndPeriod(type, period)
                .ifPresent(old -> snapshot.setId(old.getId()));

        return snapshotRepository.save(snapshot);
    }

    // ─── HELPERS ───────────────────────────────────────────────────

    private LeaderboardResponse toResponse(LeaderboardSnapshot snapshot, String currentUserId) {
        Integer myRank = snapshot.getEntries().stream()
                .filter(e -> currentUserId.equals(e.getUserId()))
                .findFirst()
                .map(LeaderboardSnapshot.LeaderboardEntry::getRank)
                .orElse(null);

        List<LeaderboardResponse.EntryDTO> dtos = snapshot.getEntries().stream()
                .map(e -> LeaderboardResponse.EntryDTO.builder()
                        .rank(e.getRank())
                        .userId(e.getUserId())
                        .displayName(e.getDisplayName())
                        .avatarUrl(e.getAvatarUrl()) // 🌟 GÁN VÀO DTO TRẢ VỀ CHO FRONTEND
                        .value(e.getValue())
                        .level(e.getLevel())
                        .treeStage(e.getTreeStage())
                        .change(e.getChange())
                        .build())
                .collect(Collectors.toList());

        return LeaderboardResponse.builder()
                .type(snapshot.getType())
                .period(snapshot.getPeriod())
                .entries(dtos)
                .myRank(myRank)
                .build();
    }

    private String getCurrentWeekPeriod() {
        LocalDateTime now = LocalDateTime.now();
        int week = now.get(WeekFields.ISO.weekOfWeekBasedYear());
        return now.getYear() + "-W" + String.format("%02d", week);
    }
}
