package vn.hcmute.edu.materialsservice.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.Dto.request.FlashcardRequest;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardResponse;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardWithWordsResponse;
import vn.hcmute.edu.materialsservice.Dto.response.WordResponse;
import vn.hcmute.edu.materialsservice.Mapper.FlashcardMapper;
import vn.hcmute.edu.materialsservice.Mapper.WordMapper;
import vn.hcmute.edu.materialsservice.Model.Flashcard;
import vn.hcmute.edu.materialsservice.Model.Word;
import vn.hcmute.edu.materialsservice.Repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.Repository.WordRepository;
import vn.hcmute.edu.materialsservice.Service.FlashcardService;
import vn.hcmute.edu.materialsservice.exception.FlashcardAlreadyExistsException;
import vn.hcmute.edu.materialsservice.exception.FlashcardNotFoundException;
import vn.hcmute.edu.materialsservice.exception.InvalidFlashcardDataException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final WordRepository wordRepository;
    private final FlashcardMapper flashcardMapper;
    private final WordMapper wordMapper;

    @Override
    @Transactional
    public FlashcardResponse createFlashcard(FlashcardRequest request) {
        if (flashcardRepository.countByTitleIgnoreCase(request.getTitle()) > 0) {
            throw new FlashcardAlreadyExistsException(request.getTitle());
        }

        Flashcard flashcard = flashcardMapper.toEntity(request);
        flashcard.setCreatedAt(LocalDateTime.now());
        flashcard.setUpdatedAt(LocalDateTime.now());

        Flashcard savedFlashcard = flashcardRepository.save(flashcard);

        log.info("Flashcard created successfully with ID: {}", savedFlashcard.getId());

        FlashcardResponse response = flashcardMapper.toResponse(savedFlashcard);
        response.setWordCount(0);

        return response;
    }

    @Override
    @Transactional
    public FlashcardResponse updateFlashcard(String id, FlashcardRequest request) {
        Flashcard existingFlashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new FlashcardNotFoundException(id));

        List<Flashcard> flashcardsWithSameTitle = flashcardRepository
                .findByTitleContainingIgnoreCase(request.getTitle());

        boolean titleExists = flashcardsWithSameTitle.stream()
                .anyMatch(f -> !f.getId().equals(id) &&
                        f.getTitle().equalsIgnoreCase(request.getTitle()));

        if (titleExists) {
            throw new FlashcardAlreadyExistsException(request.getTitle());
        }

        existingFlashcard.setTitle(request.getTitle());
        existingFlashcard.setDescription(request.getDescription());
        existingFlashcard.setLevel(request.getLevel());
        existingFlashcard.setTopic(request.getTopic());
        existingFlashcard.setUpdatedAt(LocalDateTime.now());

        Flashcard updatedFlashcard = flashcardRepository.save(existingFlashcard);

        log.info("Flashcard updated successfully with ID: {}", id);

        FlashcardResponse response = flashcardMapper.toResponse(updatedFlashcard);
        response.setWordCount((int) wordRepository.countByFlashcardId(id));

        return response;
    }

    @Override
    @Transactional
    public void deleteFlashcard(String id) {
        log.info("Deleting flashcard with ID: {}", id);

        if (!flashcardRepository.existsById(id)) {
            throw new FlashcardNotFoundException(id);
        }

        // Xóa tất cả words của flashcard
        wordRepository.deleteByFlashcardId(id);

        // Xóa flashcard
        flashcardRepository.deleteById(id);

        log.info("Flashcard and its words deleted successfully with ID: {}", id);
    }

    @Override
    public FlashcardResponse getFlashcardById(String id) {
        log.info("Getting flashcard with ID: {}", id);

        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new FlashcardNotFoundException(id));

        FlashcardResponse response = flashcardMapper.toResponse(flashcard);
        response.setWordCount((int) wordRepository.countByFlashcardId(id));

        return response;
    }

    // ✅ METHOD MỚI: Lấy flashcard kèm tất cả words
    @Override
    public FlashcardWithWordsResponse getFlashcardWithWords(String id) {
        log.info("Getting flashcard with words, ID: {}", id);

        // Lấy flashcard
        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new FlashcardNotFoundException(id));

        // Lấy tất cả words của flashcard
        List<Word> words = wordRepository.findByFlashcardId(id);
        List<WordResponse> wordResponses = wordMapper.toResponseList(words);

        // Build response
        FlashcardWithWordsResponse response = FlashcardWithWordsResponse.builder()
                .id(flashcard.getId())
                .title(flashcard.getTitle())
                .description(flashcard.getDescription())
                .level(flashcard.getLevel())
                .topic(flashcard.getTopic())
                .wordCount(words.size())
                .words(wordResponses)
                .createdAt(flashcard.getCreatedAt())
                .updatedAt(flashcard.getUpdatedAt())
                .build();

        return response;
    }

    @Override
    public List<FlashcardResponse> getAllFlashcard() {
        List<Flashcard> flashcards = flashcardRepository.findAll();
        List<FlashcardResponse> responses = flashcardMapper.toResponseList(flashcards);

        // Set wordCount cho mỗi flashcard
        responses.forEach(response -> {
            long count = wordRepository.countByFlashcardId(response.getId());
            response.setWordCount((int) count);
        });

        return responses;
    }

    @Override
    public List<FlashcardResponse> getFlashcardsByTopic(String topic) {
        log.info("Getting flashcards by topic: {}", topic);

        if (topic == null || topic.trim().isEmpty()) {
            throw new InvalidFlashcardDataException("Topic không được để trống");
        }

        List<Flashcard> flashcards = flashcardRepository.findByTopicContainingIgnoreCase(topic);
        List<FlashcardResponse> responses = flashcardMapper.toResponseList(flashcards);

        responses.forEach(response -> {
            long count = wordRepository.countByFlashcardId(response.getId());
            response.setWordCount((int) count);
        });

        return responses;
    }

    @Override
    public List<FlashcardResponse> getFlashcardsByLevel(Integer level) {
        log.info("Getting flashcards by level: {}", level);

        validateLevel(level);

        List<Flashcard> flashcards = flashcardRepository.findByLevel(level);
        List<FlashcardResponse> responses = flashcardMapper.toResponseList(flashcards);

        responses.forEach(response -> {
            long count = wordRepository.countByFlashcardId(response.getId());
            response.setWordCount((int) count);
        });

        return responses;
    }

    private void validateLevel(Integer level) {
        if (level == null || level < 1 || level > 6) {
            throw new InvalidFlashcardDataException("Level phải từ 1 đến 6");
        }
    }
}
