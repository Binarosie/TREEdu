package vn.hcmute.edu.materialsservice.Enum;

public enum EWordForm {
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

    EWordForm(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}
