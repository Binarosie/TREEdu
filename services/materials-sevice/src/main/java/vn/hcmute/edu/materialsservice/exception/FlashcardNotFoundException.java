package vn.hcmute.edu.materialsservice.exception;

public class FlashcardNotFoundException extends RuntimeException {

    public FlashcardNotFoundException(String id) {
        super("Không tìm thấy flashcard với ID: " + id);
    }

    public FlashcardNotFoundException(String field, String value) {
        super(String.format("Không tìm thấy flashcard với %s: %s", field, value));
    }
}
