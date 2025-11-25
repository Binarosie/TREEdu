package vn.hcmute.edu.materialsservice.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.Dto.request.QuizRequest;
import vn.hcmute.edu.materialsservice.Dto.response.QuizResponse;
import vn.hcmute.edu.materialsservice.Dto.response.QuizResponseWithQuestion;
import vn.hcmute.edu.materialsservice.Mapper.QuizMapper;
import vn.hcmute.edu.materialsservice.Model.Quiz;
import vn.hcmute.edu.materialsservice.Repository.QuizRepository;
import vn.hcmute.edu.materialsservice.Service.QuizService;
import vn.hcmute.edu.materialsservice.exception.DuplicateResourceException;
import vn.hcmute.edu.materialsservice.exception.InvalidDataException;
import vn.hcmute.edu.materialsservice.exception.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;

    @Override
    public QuizResponseWithQuestion createQuiz(QuizRequest requestDTO) {


        // Check if quiz with same title already exists
        if (quizRepository.existsByTitle(requestDTO.getTitle())) {
            throw new DuplicateResourceException("Quiz with title '" + requestDTO.getTitle() + "' already exists");
        }

        // Validate questions
        validateQuestions(requestDTO);

        // Map DTO to entity
        Quiz quiz = quizMapper.toEntity(requestDTO);

        // Save quiz
        Quiz savedQuiz = quizRepository.save(quiz);

        log.info("Quiz created successfully with ID: {}", savedQuiz.getId());
        return quizMapper.toResponse(savedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResponseWithQuestion getQuizById(String id) {
        log.info("Fetching quiz with ID: {}", id);

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));

        return quizMapper.toResponse(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponseWithQuestion> getQuizWithQuestion(Pageable pageable) {
        Page<Quiz> quizPage = quizRepository.findAll(pageable);
        return quizPage.map(quizMapper::toResponse);
    }

    @Override
    public Page<QuizResponse> getAllQuizzes(Pageable pageable) {
        return null;
    }


    @Override
    @Transactional(readOnly = true)
    public List<QuizResponseWithQuestion> getQuizzesByTopic(String topic) {
        log.info("Fetching quizzes by topic: {}", topic);

        List<Quiz> quizzes = quizRepository.findByTopic(topic);
        return quizzes.stream()
                .map(quizMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponseWithQuestion> getQuizzesByLevel(Integer level) {
        log.info("Fetching quizzes by level: {}", level);

        List<Quiz> quizzes = quizRepository.findByLevel(level);
        return quizzes.stream()
                .map(quizMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QuizResponseWithQuestion updateQuiz(String id, QuizRequest requestDTO) {
        log.info("Updating quiz with ID: {}", id);

        Quiz existingQuiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));

        // Check if new title conflicts with another quiz
        if (!existingQuiz.getTitle().equals(requestDTO.getTitle())
                && quizRepository.existsByTitle(requestDTO.getTitle())) {
            throw new DuplicateResourceException("Quiz with title '" + requestDTO.getTitle() + "' already exists");
        }

        // Validate questions
        validateQuestions(requestDTO);

        // Update entity
        quizMapper.updateEntityFromDTO(requestDTO, existingQuiz);

        // Save updated quiz
        Quiz updatedQuiz = quizRepository.save(existingQuiz);

        log.info("Quiz updated successfully with ID: {}", updatedQuiz.getId());
        return quizMapper.toResponse(updatedQuiz);
    }

    @Override
    public void deleteQuiz(String id) {
        log.info("Deleting quiz with ID: {}", id);

        if (!quizRepository.existsById(id)) {
            throw new ResourceNotFoundException("Quiz not found with ID: " + id);
        }

        quizRepository.deleteById(id);
        log.info("Quiz deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponseWithQuestion> searchQuizzesByTopic(String topic) {
        log.info("Searching quizzes by topic keyword: {}", topic);

        List<Quiz> quizzes = quizRepository.findByTopicIgnoreCase(topic);
        return quizzes.stream()
                .map(quizMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate all questions in the quiz
     */
    private void validateQuestions(QuizRequest requestDTO) {
        requestDTO.getQuestions().forEach(questionDTO -> {
            // Check if exactly one correct answer exists
            long correctAnswersCount = questionDTO.getOptions().stream()
                    .filter(answer -> answer.getIsCorrect() != null && answer.getIsCorrect())
                    .count();

            if (correctAnswersCount != 1) {
                throw new InvalidDataException(
                        "Each question must have exactly one correct answer. Question: '"
                                + questionDTO.getContent() + "' has " + correctAnswersCount + " correct answers"
                );
            }
        });
    }
}
