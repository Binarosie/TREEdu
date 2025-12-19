package vn.hcmute.edu.materialsservice.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.Dto.request.FlashcardRequest;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardResponse;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardWithWordsResponse;
import vn.hcmute.edu.materialsservice.Dto.response.WordResponse;
import vn.hcmute.edu.materialsservice.Enum.FlashcardType;
import vn.hcmute.edu.materialsservice.Mapper.FlashcardMapper;
import vn.hcmute.edu.materialsservice.Mapper.WordMapper;
import vn.hcmute.edu.materialsservice.Model.Flashcard;
import vn.hcmute.edu.materialsservice.Model.Word;
import vn.hcmute.edu.materialsservice.Repository.FlashcardProgressRepository;
import vn.hcmute.edu.materialsservice.Repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.Repository.WordRepository;
import vn.hcmute.edu.materialsservice.Service.FlashcardLearningService;
import vn.hcmute.edu.materialsservice.Service.FlashcardService;
import vn.hcmute.edu.materialsservice.exception.FlashcardAlreadyExistsException;
import vn.hcmute.edu.materialsservice.exception.FlashcardNotFoundException;
import vn.hcmute.edu.materialsservice.exception.InvalidFlashcardDataException;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;

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
    private final FlashcardProgressRepository progressRepository;

    @Override
    @Transactional
    public FlashcardResponse createFlashcard(
            FlashcardRequest request,
            Authentication authentication) {

        if (flashcardRepository.countByTitleIgnoreCase(request.getTitle()) > 0) {
            throw new FlashcardAlreadyExistsException(request.getTitle());
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Flashcard flashcard = flashcardMapper.toEntity(request);
        flashcard.setCreatedAt(LocalDateTime.now());
        flashcard.setUpdatedAt(LocalDateTime.now());
        flashcard.setDeleted(false);

        // ================= LOGIC PHÂN LOẠI =================
        boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_SUPPORTER"));

        if (isAdminOrSupporter) {
            flashcard.setType(FlashcardType.SYSTEM);
            flashcard.setCreatedBy(null);
        } else {
            flashcard.setType(FlashcardType.BY_MEMBER);
            flashcard.setCreatedBy(userDetails.getUser().getId().toString());
        }
        // ===================================================

        Flashcard saved = flashcardRepository.save(flashcard);

        FlashcardResponse response = flashcardMapper.toResponse(saved);
        response.setWordCount(0);

        return response;
    }

    @Override
    @Transactional
    public FlashcardResponse updateFlashcard(String id, FlashcardRequest request, Authentication authentication) {
        Flashcard existingFlashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new FlashcardNotFoundException(id));

        long progressCount = progressRepository.findByFlashcardId(id).size();
        if (progressCount > 0) {
            throw new IllegalStateException(
                    "Không thể cập nhật flashcard này vì đã có " + progressCount + " người học. "
                            + "Chỉ được cập nhật flashcard chưa có ai học.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getUser().getId().toString();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPORTER"));

        // ================= RULE =================
        if (existingFlashcard.getType() == FlashcardType.SYSTEM) {
            if (!isAdmin && !isSupporter) {
                throw new AccessDeniedException(
                        "Bạn không có quyền cập nhật flashcard hệ thống");
            }
        }

        if (existingFlashcard.getType() == FlashcardType.BY_MEMBER) {
            if (!isAdmin && !userId.equals(existingFlashcard.getCreatedBy())) {
                throw new AccessDeniedException(
                        "Bạn chỉ có thể cập nhật flashcard do chính bạn tạo");
            }
        }
        // ========================================

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
    public void deleteFlashcard(String id, Authentication authentication) {

        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new FlashcardNotFoundException(id));

        long progressCount = progressRepository.findByFlashcardId(id).size();
        if (progressCount > 0) {
            throw new IllegalStateException(
                    "Không thể xóa flashcard này vì đã có " + progressCount + " người học. "
                            + "Chỉ được xóa flashcard chưa có ai học.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getUser().getId().toString();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPORTER"));

        // ================= RULE =================
        if (flashcard.getType() == FlashcardType.SYSTEM) {
            if (!isAdmin && !isSupporter) {
                throw new AccessDeniedException(
                        "Bạn không có quyền xóa flashcard hệ thống");
            }
        }

        if (flashcard.getType() == FlashcardType.BY_MEMBER) {
            if (!isAdmin && !userId.equals(flashcard.getCreatedBy())) {
                throw new AccessDeniedException(
                        "Bạn chỉ có thể xóa flashcard do chính bạn tạo");
            }
        }
        // ========================================

        flashcard.setDeleted(true);
        flashcard.setDeletedAt(LocalDateTime.now());
        flashcardRepository.save(flashcard);

        log.info("Flashcard soft deleted successfully with ID: {}", id);
    }

    @Override
    public FlashcardResponse getFlashcardById(String id, Authentication authentication) {
        log.info("Getting flashcard with ID: {}", id);

        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new FlashcardNotFoundException(id));

        // ================= FILTER THEO ROLE =================
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_SUPPORTER"));

            // MEMBER chỉ xem SYSTEM hoặc flashcard của mình
            if (!isAdminOrSupporter) {
                String userId = userDetails.getUser().getId().toString();
                if (flashcard.getType() == FlashcardType.BY_MEMBER &&
                        !userId.equals(flashcard.getCreatedBy())) {
                    throw new AccessDeniedException(
                            "Bạn không có quyền xem flashcard này");
                }
            }
        } else {
            // GUEST chỉ xem SYSTEM flashcard
            if (flashcard.getType() != FlashcardType.SYSTEM) {
                throw new AccessDeniedException(
                        "Bạn cần đăng nhập để xem flashcard này");
            }
        }
        // ===================================================

        FlashcardResponse response = flashcardMapper.toResponse(flashcard);
        response.setWordCount((int) wordRepository.countByFlashcardId(id));

        return response;
    }

    // METHOD MỚI: Lấy flashcard kèm tất cả words
    @Override
    public FlashcardWithWordsResponse getFlashcardWithWords(String id, Authentication authentication) {
        log.info("Getting flashcard with words, ID: {}", id);

        // Lấy flashcard
        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new FlashcardNotFoundException(id));

        // ================= FILTER THEO ROLE =================
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_SUPPORTER"));

            if (!isAdminOrSupporter) {
                String userId = userDetails.getUser().getId().toString();
                if (flashcard.getType() == FlashcardType.BY_MEMBER &&
                        !userId.equals(flashcard.getCreatedBy())) {
                    throw new AccessDeniedException(
                            "Bạn không có quyền xem flashcard này");
                }
            }
        } else {
            // GUEST chỉ xem SYSTEM flashcard
            if (flashcard.getType() != FlashcardType.SYSTEM) {
                throw new AccessDeniedException(
                        "Bạn cần đăng nhập để xem flashcard này");
            }
        }
        // ===================================================

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
    public List<FlashcardResponse> getAllFlashcard(Authentication authentication) {

        System.out.println("===== DEBUG GET ALL FLASHCARD =====");

        List<Flashcard> flashcards = flashcardRepository.findAll();
        System.out.println("Total flashcards in DB: " + flashcards.size());

        if (authentication != null) {
            System.out.println("Authentication class: " + authentication.getClass().getName());
            System.out.println("isAuthenticated: " + authentication.isAuthenticated());
            System.out.println("Principal: " + authentication.getPrincipal());
        } else {
            System.out.println("Authentication is NULL (GUEST)");
        }

        // ================= FILTER THEO ROLE =================
        if (authentication != null && authentication.isAuthenticated()) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String userId = userDetails.getUser().getId().toString();
            System.out.println(">>> LOGIN USER ID: " + userId);
            System.out.println(">>> AUTHORITIES: " + userDetails.getAuthorities());

            boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_SUPPORTER"));

            System.out.println("isAdminOrSupporter: " + isAdminOrSupporter);

            if (!isAdminOrSupporter) {
                flashcards = flashcards.stream()
                        .filter(f -> {
                            System.out.println("Flashcard ID: " + f.getId()
                                    + " | type=" + f.getType()
                                    + " | createdBy=" + f.getCreatedBy());

                            return f.getType() == FlashcardType.SYSTEM ||
                                    (f.getType() == FlashcardType.BY_MEMBER &&
                                            userId.equals(f.getCreatedBy()));
                        })
                        .toList();
            }
        } else {
            System.out.println(">>> GUEST MODE");
            flashcards = flashcards.stream()
                    .filter(f -> f.getType() == FlashcardType.SYSTEM)
                    .toList();
        }
        // ===================================================

        System.out.println("Flashcards after filter: " + flashcards.size());
        System.out.println("===================================");

        List<FlashcardResponse> responses = flashcardMapper.toResponseList(flashcards);

        responses.forEach(response -> {
            long count = wordRepository.countByFlashcardId(response.getId());
            response.setWordCount((int) count);
        });

        return responses;
    }

    @Override
    public List<FlashcardResponse> getFlashcardsByTopic(String topic, Authentication authentication) {
        log.info("Fuzzy searching flashcards by topic: {}", topic);

        // Validate min characters
        if (topic == null || topic.trim().length() < 2) {
            log.warn("Topic keyword too short for fuzzy search: {}", topic);
            return List.of();
        }

        // Lấy tất cả flashcards trước
        List<Flashcard> allFlashcards = flashcardRepository.findAll();

        // Apply fuzzy filter với threshold 0.4
        List<Flashcard> flashcards = vn.hcmute.edu.materialsservice.utils.FuzzySearchUtil.fuzzyFilter(
                allFlashcards,
                topic,
                Flashcard::getTopic,
                0.4);

        log.info("Found {} flashcards matching '{}' with fuzzy search", flashcards.size(), topic);

        // ================= FILTER THEO ROLE =================
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_SUPPORTER"));

            if (!isAdminOrSupporter) {
                String userId = userDetails.getUser().getId().toString();
                flashcards = flashcards.stream()
                        .filter(f -> f.getType() == FlashcardType.SYSTEM ||
                                (f.getType() == FlashcardType.BY_MEMBER &&
                                        userId.equals(f.getCreatedBy())))
                        .toList();
            }
        }
        // ===================================================

        List<FlashcardResponse> responses = flashcardMapper.toResponseList(flashcards);

        responses.forEach(response -> {
            long count = wordRepository.countByFlashcardId(response.getId());
            response.setWordCount((int) count);
        });

        return responses;
    }

    @Override
    public List<FlashcardResponse> getFlashcardsByLevel(Integer level, Authentication authentication) {
        log.info("Getting flashcards by level: {}", level);

        validateLevel(level);

        List<Flashcard> flashcards = flashcardRepository.findByLevel(level);

        // ================= FILTER THEO ROLE =================
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_SUPPORTER"));

            if (!isAdminOrSupporter) {
                String userId = userDetails.getUser().getId().toString();
                flashcards = flashcards.stream()
                        .filter(f -> f.getType() == FlashcardType.SYSTEM ||
                                (f.getType() == FlashcardType.BY_MEMBER &&
                                        userId.equals(f.getCreatedBy())))
                        .toList();
            }
        }
        // ===================================================

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
