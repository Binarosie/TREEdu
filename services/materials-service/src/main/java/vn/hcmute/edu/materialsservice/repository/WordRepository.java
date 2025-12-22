package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.Word;

import java.util.List;

@Repository
public interface WordRepository extends MongoRepository<Word, String> {

    // Tìm tất cả words của một flashcard
    List<Word> findByFlashcardId(String flashcardId);

    // Đếm số words của một flashcard
    long countByFlashcardId(String flashcardId);

    // Xóa tất cả words của một flashcard
    void deleteByFlashcardId(String flashcardId);

    // Kiểm tra word có thuộc flashcard không
    boolean existsByIdAndFlashcardId(String id, String flashcardId);
}
