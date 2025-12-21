package vn.hcmute.edu.materialsservice.exceptions;

public class WordNotFoundException extends RuntimeException {
    public WordNotFoundException(String id) {
        super("Không tìm thấy từ với ID: " + id);
    }
}
