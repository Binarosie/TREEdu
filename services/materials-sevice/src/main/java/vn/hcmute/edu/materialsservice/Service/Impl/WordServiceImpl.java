package vn.hcmute.edu.materialsservice.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.Dto.request.WordRequest;
import vn.hcmute.edu.materialsservice.Dto.response.WordResponse;
import vn.hcmute.edu.materialsservice.Mapper.WordMapper;
import vn.hcmute.edu.materialsservice.Model.Word;
import vn.hcmute.edu.materialsservice.Repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.Repository.WordRepository;
import vn.hcmute.edu.materialsservice.Service.WordService;
import vn.hcmute.edu.materialsservice.exception.FlashcardNotFoundException;
import vn.hcmute.edu.materialsservice.exception.WordNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;
    private final FlashcardRepository flashcardRepository;
    private final WordMapper wordMapper;

    @Override
    @Transactional
    public WordResponse addWord(String flashcardId, WordRequest request) {
        // Kiểm tra flashcard có tồn tại không
        if (!flashcardRepository.existsById(flashcardId)) {
            throw new FlashcardNotFoundException(flashcardId);
        }

        Word word = wordMapper.toEntity(request);
        word.setFlashcardId(flashcardId);
        word.setCreatedAt(LocalDateTime.now());
        word.setUpdatedAt(LocalDateTime.now());

        Word savedWord = wordRepository.save(word);

        log.info("Word added to flashcard {}: {}", flashcardId, savedWord.getNewWord());

        return wordMapper.toResponse(savedWord);
    }

    @Override
    @Transactional
    public WordResponse updateWord(String id, WordRequest request) {
        Word existingWord = wordRepository.findById(id)
                .orElseThrow(() -> new WordNotFoundException(id));

        existingWord.setNewWord(request.getNewWord());
        existingWord.setMeaning(request.getMeaning());
        existingWord.setWordForm(request.getWordForm());
        existingWord.setPhoneme(request.getPhoneme());
        existingWord.setImageURL(request.getImageURL());
        existingWord.setAudioURL(request.getAudioURL());
        existingWord.setUpdatedAt(LocalDateTime.now());

        Word updatedWord = wordRepository.save(existingWord);

        log.info("Word updated: {}", id);

        return wordMapper.toResponse(updatedWord);
    }

    @Override
    public void deleteWord(String id) {
        if (!wordRepository.existsById(id)) {
            throw new WordNotFoundException(id);
        }

        wordRepository.deleteById(id);

        log.info("Word deleted: {}", id);
    }

    @Override
    public WordResponse getWordById(String id) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new WordNotFoundException(id));

        return wordMapper.toResponse(word);
    }

    @Override
    public List<WordResponse> getWordsByFlashcardId(String flashcardId) {
        List<Word> words = wordRepository.findByFlashcardId(flashcardId);
        return wordMapper.toResponseList(words);
    }
}
