package vn.hcmute.edu.materialsservice.Enum;

public enum WordForm {
    NOUN("Danh từ"),
    VERB("Động từ"),
    ADJECTIVE("Tính từ"),
    ADVERB("Trạng từ"),
    PRONOUN("Đại từ"),
    PREPOSITION("Giới từ"),
    CONJUNCTION("Liên từ"),
    INTERJECTION("Thán từ"),
    PHRASE("Cụm từ");

    private final String vietnameseName;

    WordForm(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}
