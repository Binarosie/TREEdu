package vn.hcmute.edu.materialsservice.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fuzzy Search Utility - Tìm kiếm mờ sử dụng Levenshtein Distance
 * 
 * Threshold: 0.4 (40% similarity required)
 * Min characters: 2
 * 
 * Đặc biệt: Xử lý tiếng Việt có dấu
 */
public class FuzzySearchUtil {

    private static final double DEFAULT_THRESHOLD = 0.4;
    private static final int MIN_CHARACTERS = 2;

    /**
     * Normalize Vietnamese string - bỏ dấu tiếng Việt
     */
    public static String normalizeVietnamese(String text) {
        if (text == null) {
            return "";
        }

        // Normalize Unicode
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);

        // Remove diacritical marks
        normalized = normalized.replaceAll("\\p{M}", "");

        // Replace Đ/đ
        normalized = normalized.replace("Đ", "D").replace("đ", "d");

        return normalized.toLowerCase().trim();
    }

    /**
     * Tính Levenshtein Distance giữa 2 chuỗi
     */
    public static int levenshteinDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }

    /**
     * Tính similarity score (0.0 - 1.0) với Vietnamese normalization
     * 1.0 = hoàn toàn giống nhau
     * 0.0 = hoàn toàn khác nhau
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        s1 = s1.toLowerCase().trim();
        s2 = s2.toLowerCase().trim();

        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }

        // Normalize Vietnamese
        String norm1 = normalizeVietnamese(s1);
        String norm2 = normalizeVietnamese(s2);

        // Exact match (với hoặc không dấu)
        if (s1.equals(s2) || norm1.equals(norm2)) {
            return 1.0;
        }

        // StartsWith match (ưu tiên cao) - với normalized
        if (norm1.startsWith(norm2) || norm2.startsWith(norm1)) {
            return 0.9;
        }

        // Contains match (ưu tiên cao) - với normalized
        if (norm1.contains(norm2) || norm2.contains(norm1)) {
            return 0.85;
        }

        // Word boundary match - kiểm tra xem keyword có là đầu của một từ không
        String[] words1 = norm1.split("\\s+");
        String[] words2 = norm2.split("\\s+");

        for (String word : words1) {
            if (word.startsWith(norm2)) {
                return 0.8;
            }
        }

        for (String word : words2) {
            if (word.startsWith(norm1)) {
                return 0.8;
            }
        }

        // Levenshtein distance với normalized strings
        int maxLength = Math.max(norm1.length(), norm2.length());
        if (maxLength == 0) {
            return 1.0;
        }

        int distance = levenshteinDistance(norm1, norm2);
        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Kiểm tra xem chuỗi có match với keyword theo fuzzy search không
     * 
     * @param text      Chuỗi cần kiểm tra
     * @param keyword   Keyword tìm kiếm
     * @param threshold Ngưỡng similarity (0.0 - 1.0), mặc định 0.4
     * @return true nếu similarity >= threshold
     */
    public static boolean isFuzzyMatch(String text, String keyword, double threshold) {
        if (text == null || keyword == null) {
            return false;
        }

        // Validate min characters
        if (keyword.trim().length() < MIN_CHARACTERS) {
            return false;
        }

        return calculateSimilarity(text, keyword) >= threshold;
    }

    /**
     * Kiểm tra fuzzy match với default threshold (0.4)
     */
    public static boolean isFuzzyMatch(String text, String keyword) {
        return isFuzzyMatch(text, keyword, DEFAULT_THRESHOLD);
    }

    /**
     * Filter danh sách items theo fuzzy search
     * 
     * @param items     Danh sách items cần filter
     * @param keyword   Keyword tìm kiếm
     * @param extractor Function để lấy text từ item
     * @param threshold Ngưỡng similarity
     * @return Danh sách items đã filter và sort theo similarity (cao nhất trước)
     */
    public static <T> List<T> fuzzyFilter(List<T> items, String keyword,
            java.util.function.Function<T, String> extractor,
            double threshold) {
        if (items == null || keyword == null || keyword.trim().length() < MIN_CHARACTERS) {
            return new ArrayList<>();
        }

        // Tạo list các item với similarity score
        List<ScoredItem<T>> scoredItems = items.stream()
                .map(item -> {
                    String text = extractor.apply(item);
                    double score = calculateSimilarity(text, keyword);
                    return new ScoredItem<>(item, score);
                })
                .filter(scored -> scored.score >= threshold)
                .sorted(Comparator.comparingDouble((ScoredItem<T> s) -> s.score).reversed())
                .collect(Collectors.toList());

        return scoredItems.stream()
                .map(scored -> scored.item)
                .collect(Collectors.toList());
    }

    /**
     * Fuzzy filter với default threshold (0.4)
     */
    public static <T> List<T> fuzzyFilter(List<T> items, String keyword,
            java.util.function.Function<T, String> extractor) {
        return fuzzyFilter(items, keyword, extractor, DEFAULT_THRESHOLD);
    }

    /**
     * Tạo regex pattern cho MongoDB fuzzy search
     * Chuyển keyword thành pattern cho phép sai sót 1-2 ký tự
     */
    public static String createFuzzyRegexPattern(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ".*";
        }

        keyword = normalizeVietnamese(keyword);

        // Nếu keyword quá ngắn, chỉ dùng contains
        if (keyword.length() < MIN_CHARACTERS) {
            return ".*";
        }

        // Tạo pattern: cho phép các ký tự tùy chọn giữa các ký tự trong keyword
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            pattern.append(c);
            // Cho phép 0-1 ký tự bất kỳ giữa các ký tự (để match typos)
            if (i < keyword.length() - 1) {
                pattern.append(".{0,1}");
            }
        }

        return ".*" + pattern.toString() + ".*";
    }

    /**
     * Helper class để lưu item với similarity score
     */
    private static class ScoredItem<T> {
        final T item;
        final double score;

        ScoredItem(T item, double score) {
            this.item = item;
            this.score = score;
        }
    }
}
