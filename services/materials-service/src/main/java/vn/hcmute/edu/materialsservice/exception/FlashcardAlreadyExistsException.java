package vn.hcmute.edu.materialsservice.exception;

public class FlashcardAlreadyExistsException extends RuntimeException {

    public FlashcardAlreadyExistsException(String title) {
        super("Flashcard với tiêu đề '" + title + "' đã tồn tại");
    }
}
