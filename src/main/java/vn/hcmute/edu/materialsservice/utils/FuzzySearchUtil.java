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
     * Normalize Vietnamese string - bỏ dấu tiếng Việt * Chuyen: "Xin chao" -> "xin
     * chao"
     * Muc dich: De cac tim kiem khong bi anh huong boi dau
     */
    public static String normalizeVietnamese(String text) {
        if (text == null) {
            return "";
        }

        // Normalize Unicode - tach dau va ky tu
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);

        // Xoa cac dau (nhu dau mua, huyen, sac...)
        normalized = normalized.replaceAll("\\p{M}", "");

        // Thay the Đ/đ bang D/d
        normalized = normalized.replace("Đ", "D").replace("đ", "d");

        return normalized.toLowerCase().trim();
    }

    /**
     * Tính Levenshtein Distance giữa 2 chuỗi * Khoang cach: so luot sua doi
     * (them/xoa/thay) de dua 2 chuoi ve giong nhau
     * Vi du: "kitten" -> "sitting" = 3
     * 
     * @return so luot thay doi toi thieu
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
     * 0.0 = hoàn toàn khác nhau *
     * Uu tien kiem tra:
     * 1. Exact match (voi hoac khong dau) -> 1.0
     * 2. StartsWith match -> 0.9
     * 3. Contains match -> 0.85
     * 4. Word boundary match -> 0.8
     * 5. Levenshtein distance -> 0.0-0.8
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
     * @param text      Chuoi can kiem tra (vi du: "Xin chao")
     * @param keyword   Keyword tim kiem (vi du: "xin")
     * @param threshold Nguong similarity (0.0 - 1.0), mac dinh 0.4 = 40% giong nhau
     * @return true neu similarity >= threshold, nghia la match duoc
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
     * Kiem tra fuzzy match voi default threshold (0.4 = 40%)
     * Dung so huu thuc phong: tim kiem co cho phep loi tu
     */
    public static boolean isFuzzyMatch(String text, String keyword) {
        return isFuzzyMatch(text, keyword, DEFAULT_THRESHOLD);
    }

    /**
     * Filter danh sach items theo fuzzy search va sap xep theo thu tu tuong tu
     * 
     * @param items     Danh sach items can filter (vi du: list cac flashcard)
     * @param keyword   Keyword tim kiem (vi du: "xin")
     * @param extractor Function de lay text tu item (vi du: Flashcard::getTitle)
     * @param threshold Nguong similarity toi thieu
     * @return Danh sach items duoc filter va sort theo similarity (cao nhat dau,
     *         thap nhat cuoi)
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
                    String text = extractor.apply(item); // Lay text tu item
                    double score = calculateSimilarity(text, keyword); // Tinh similarity
                    return new ScoredItem<>(item, score);
                })
                // Loc chi nhung item co similarity >= threshold
                .filter(scored -> scored.score >= threshold)
                // Sap xep giam dan theo score (cao nhat truoc)
                .sorted(Comparator.comparingDouble((ScoredItem<T> s) -> s.score).reversed())
                .collect(Collectors.toList());

        // Tra ve chi items, bo score
        return scoredItems.stream()
                .map(scored -> scored.item)
                .collect(Collectors.toList());
    }

    /**
     * Fuzzy filter voi default threshold (0.4 = 40%)
     */
    public static <T> List<T> fuzzyFilter(List<T> items, String keyword,
            java.util.function.Function<T, String> extractor) {
        return fuzzyFilter(items, keyword, extractor, DEFAULT_THRESHOLD);
    }

    /**
     * Tạo regex pattern cho MongoDB fuzzy search
     * Da fuzzy: cho phep 0-1 ky tu bat ky giua cac ky tu de phat hien loi go
     * Vi du: "hello" -> "h.{0,1}e.{0,1}l.{0,1}l.{0,1}o"
     * Dieu nay match: "hello", "helo", "h3llo" (voi 1 loi)
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
     * Helper class luu item voi similarity score
     * Giup sap xep danh sach theo thu tu tuong tu
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
