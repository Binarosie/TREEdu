package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.dtos.request.FlashcardRequest;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardResponse;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardWithWordsResponse;
import vn.hcmute.edu.materialsservice.dtos.response.WordResponse;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardType;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardVisibility;
import vn.hcmute.edu.materialsservice.Mapper.FlashcardMapper;
import vn.hcmute.edu.materialsservice.Mapper.WordMapper;
import vn.hcmute.edu.materialsservice.models.Flashcard;
import vn.hcmute.edu.materialsservice.models.NotificationEvent;
import vn.hcmute.edu.materialsservice.models.Word;
import vn.hcmute.edu.materialsservice.repository.FlashcardProgressRepository;
import vn.hcmute.edu.materialsservice.repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.repository.WordRepository;
import vn.hcmute.edu.materialsservice.services.iFlashcardService;
import vn.hcmute.edu.materialsservice.exceptions.FlashcardAlreadyExistsException;
import vn.hcmute.edu.materialsservice.exceptions.FlashcardNotFoundException;
import vn.hcmute.edu.materialsservice.exceptions.InvalidFlashcardDataException;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.observer.NotificationCenter;
import vn.hcmute.edu.materialsservice.utils.FuzzySearchUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardServiceImpl implements iFlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final WordRepository wordRepository;
    private final FlashcardMapper flashcardMapper;
    private final WordMapper wordMapper;
    private final FlashcardProgressRepository progressRepository;
    private final MongoTemplate mongoTemplate;

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

        boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_SUPPORTER"));

        if (isAdminOrSupporter) {
            flashcard.setType(EFlashcardType.SYSTEM);
            flashcard.setCreatedBy(null);
        } else {
            flashcard.setType(EFlashcardType.BY_MEMBER);
            flashcard.setCreatedBy(userDetails.getUser().getId().toString());
        }

        Flashcard saved = flashcardRepository.save(flashcard);

        FlashcardResponse response = flashcardMapper.toResponse(saved);

        response.setWordCount(0);

        boolean isOwner = saved.getCreatedBy() != null &&
                saved.getCreatedBy().equals(userDetails.getUser().getId().toString());

        response.setIsOwner(isOwner);

        return response;
    }

    @Override
    @Transactional
    public FlashcardResponse updateFlashcard(String id, FlashcardRequest request, Authentication authentication) {
        Flashcard existingFlashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new FlashcardNotFoundException(id));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getUser().getId().toString();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPORTER"));

        if (existingFlashcard.getType() == EFlashcardType.SYSTEM) {
            if (!isAdmin && !isSupporter) {
                throw new AccessDeniedException(
                        "Bạn không có quyền cập nhật flashcard hệ thống");
            }
        }

        if (existingFlashcard.getType() == EFlashcardType.BY_MEMBER) {
            if (!isAdmin && !userId.equals(existingFlashcard.getCreatedBy())) {
                throw new AccessDeniedException(
                        "Bạn chỉ có thể cập nhật flashcard do chính bạn tạo");
            }

            // Logic kiểm tra progress khi member sửa flashcard của họ
            var allProgress = progressRepository.findByFlashcardId(id);
            var otherUserProgress = allProgress.stream()
                    .filter(p -> !p.getUserId().equals(existingFlashcard.getCreatedBy()))
                    .toList();

            // Nếu flashcard PUBLIC
            if (existingFlashcard.getVisibility() == EFlashcardVisibility.PUBLIC) {
                if (!otherUserProgress.isEmpty()) {
                    throw new IllegalStateException(
                            "Flashcard này đang công khai và có " + otherUserProgress.size()
                                    + " người khác học. Không thể chỉnh sửa.");
                }
                // Chưa có ai khác học => tự động chuyển về PRIVATE để cho phép sửa
                existingFlashcard.setVisibility(EFlashcardVisibility.PRIVATE);
                log.info("Flashcard tự động chuyển về PRIVATE khi sửa (chưa có ai khác học)");
            }
            // Nếu PRIVATE => cho sửa bình thường, tiến trình cũ vẫn giữ nguyên
        }

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

        if (flashcard.getType() == EFlashcardType.SYSTEM) {
            if (!isAdmin && !isSupporter) {
                throw new AccessDeniedException(
                        "Bạn không có quyền xóa flashcard hệ thống");
            }
        }

        if (flashcard.getType() == EFlashcardType.BY_MEMBER) {
            if (!isAdmin && !userId.equals(flashcard.getCreatedBy())) {
                throw new AccessDeniedException(
                        "Bạn chỉ có thể xóa flashcard do chính bạn tạo");
            }
        }

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

        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_SUPPORTER"));

            // MEMBER chỉ xem SYSTEM hoặc flashcard của mình
            if (!isAdminOrSupporter) {
                String userId = userDetails.getUser().getId().toString();
                if (flashcard.getType() == EFlashcardType.BY_MEMBER &&
                        !userId.equals(flashcard.getCreatedBy())) {
                    throw new AccessDeniedException(
                            "Bạn không có quyền xem flashcard này");
                }
            }
        } else {
            // GUEST chỉ xem SYSTEM flashcard
            if (flashcard.getType() != EFlashcardType.SYSTEM) {
                throw new AccessDeniedException(
                        "Bạn cần đăng nhập để xem flashcard này");
            }
        }

        FlashcardResponse response = flashcardMapper.toResponse(flashcard);
        response.setWordCount((int) wordRepository.countByFlashcardId(id));

        return response;
    }

    @Override
    public FlashcardWithWordsResponse getFlashcardWithWords(
            String id,
            Authentication authentication) {

        log.info("Getting flashcard with words, ID: {}", id);

        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new FlashcardNotFoundException(id));

        boolean isOwner = false;
        boolean isAdminOrSupporter = false;

        // ===== CHECK AUTH =====
        if (authentication != null && authentication.isAuthenticated()) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String userId = userDetails.getUser().getId().toString();

            isAdminOrSupporter = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_SUPPORTER"));

            isOwner = flashcard.getCreatedBy() != null &&
                    flashcard.getCreatedBy().equals(userId);

            // Nếu không phải admin/supporter
            if (!isAdminOrSupporter) {

                // PRIVATE + không phải owner
                if (flashcard.getVisibility() == EFlashcardVisibility.PRIVATE
                        && !isOwner) {

                    throw new AccessDeniedException(
                            "Bạn không có quyền xem flashcard này");
                }
            }

        } else {

            // Guest chỉ xem PUBLIC
            if (flashcard.getVisibility() != EFlashcardVisibility.PUBLIC) {

                throw new AccessDeniedException(
                        "Bạn cần đăng nhập để xem flashcard này");
            }
        }

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

                // THÊM MẤY FIELD NÀY
                .type(flashcard.getType().name())
                .createdBy(flashcard.getCreatedBy())
                .visibility(flashcard.getVisibility())
                .isViolated(flashcard.getIsViolated())
                .isOwner(isOwner)

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

            if (!isAdminOrSupporter) {
                flashcards = flashcards.stream()
                        .filter(f -> {
                            return f.getType() == EFlashcardType.SYSTEM ||
                                    (f.getType() == EFlashcardType.BY_MEMBER &&
                                            userId.equals(f.getCreatedBy()));
                        })
                        .toList();
            }
        } else {
            flashcards = flashcards.stream()
                    .filter(f -> f.getType() == EFlashcardType.SYSTEM)
                    .toList();
        }

        List<FlashcardResponse> responses = flashcardMapper.toResponseList(flashcards);

        responses.forEach(response -> {
            long count = wordRepository.countByFlashcardId(response.getId());
            response.setWordCount((int) count);
        });

        return responses;
    }

    @Override
    public List<FlashcardResponse> getFlashcardsByTitle(String title, Authentication authentication) {
        log.info("Fuzzy searching flashcards by title: {}", title);

        if (title == null || title.trim().length() < 2) {
            log.warn("Title keyword too short for fuzzy search: {}", title);
            return List.of();
        }

        List<Flashcard> allFlashcards = flashcardRepository.findAll();

        List<Flashcard> flashcards = FuzzySearchUtil.fuzzyFilter(
                allFlashcards,
                title,
                Flashcard::getTitle,
                0.4);

        log.info("Found {} flashcards matching '{}' with fuzzy search", flashcards.size(), title);

        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_SUPPORTER"));

            if (!isAdminOrSupporter) {
                String userId = userDetails.getUser().getId().toString();
                flashcards = flashcards.stream()
                        .filter(f -> f.getType() == EFlashcardType.SYSTEM ||
                                f.getVisibility().PUBLIC.equals(f.getVisibility()) ||
                                (f.getType() == EFlashcardType.BY_MEMBER &&
                                        userId.equals(f.getCreatedBy())))
                        .toList();
            }
        }

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

        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_SUPPORTER"));

            if (!isAdminOrSupporter) {
                String userId = userDetails.getUser().getId().toString();
                flashcards = flashcards.stream()
                        .filter(f -> f.getType() == EFlashcardType.SYSTEM ||
                                (f.getType() == EFlashcardType.BY_MEMBER &&
                                        userId.equals(f.getCreatedBy())))
                        .toList();
            }
        }

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

//    @Override
//    @Transactional
//    public FlashcardResponse changeVisibility(String id, EFlashcardVisibility visibility,
//            Authentication authentication) {
//        Flashcard flashcard = flashcardRepository.findById(id)
//                .orElseThrow(() -> new FlashcardNotFoundException(id));
//
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        String userId = userDetails.getUser().getId().toString();
//
//        // Chỉ tác giả (BY_MEMBER) hoặc Admin có thể thay đổi visibility
//        if (flashcard.getType() == EFlashcardType.BY_MEMBER) {
//            if (!userId.equals(flashcard.getCreatedBy())) {
//                throw new AccessDeniedException("Bạn chỉ có thể thay đổi visibility của flashcard do chính bạn tạo");
//            }
//        } else if (flashcard.getType() == EFlashcardType.SYSTEM) {
//            boolean isAdmin = userDetails.getAuthorities().stream()
//                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//            if (!isAdmin) {
//                throw new AccessDeniedException("Chỉ admin mới có thể thay đổi visibility flashcard hệ thống");
//            }
//        }
//
//        // Nếu flashcard bị vi phạm, không được chuyển sang PUBLIC
//        if (flashcard.getIsViolated() != null && flashcard.getIsViolated()
//                && visibility == EFlashcardVisibility.PUBLIC) {
//            throw new IllegalStateException("Flashcard này đã bị đánh dấu vi phạm, không thể chuyển sang PUBLIC");
//        }
//
//        flashcard.setVisibility(visibility);
//        flashcard.setUpdatedAt(LocalDateTime.now());
//
//        Flashcard updated = flashcardRepository.save(flashcard);
//
//        FlashcardResponse response = flashcardMapper.toResponse(updated);
//        response.setWordCount((int) wordRepository.countByFlashcardId(id));
//
//        return response;
//    }

    @Override
    @Transactional
    public FlashcardResponse changeVisibility(String id, EFlashcardVisibility visibility,
                                              Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUser().getId().toString();

        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new FlashcardNotFoundException(id));

        // Check owner / admin (giữ nguyên)
        if (flashcard.getType() == EFlashcardType.BY_MEMBER) {
            if (!userId.equals(flashcard.getCreatedBy())) {
                throw new AccessDeniedException("Bạn chỉ có thể thay đổi visibility của flashcard do chính bạn tạo");
            }
        } else if (flashcard.getType() == EFlashcardType.SYSTEM) {
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new AccessDeniedException("Chỉ admin mới có thể thay đổi visibility flashcard hệ thống");
            }
        }

        // Check violated (giữ nguyên)
        if (flashcard.getIsViolated() != null && flashcard.getIsViolated()
                && visibility == EFlashcardVisibility.PUBLIC) {
            throw new IllegalStateException("Flashcard này đã bị đánh dấu vi phạm, không thể chuyển sang PUBLIC");
        }

        Flashcard updated;

        //Thêm: PUBLIC → PRIVATE cần atomic check learnerCount
        if (flashcard.getVisibility() == EFlashcardVisibility.PUBLIC
                && visibility == EFlashcardVisibility.PRIVATE) {

            updated = setPrivateAtomically(id);

            if (updated == null) {
                long learnerCount = progressRepository.countByFlashcardId(id);
                if (learnerCount > 0) {
                    throw new IllegalStateException(
                            "Không thể chuyển về PRIVATE vì đã có " + learnerCount + " người đang học");
                }
                throw new IllegalStateException("Không thể thay đổi trạng thái flashcard lúc này");
            }

        } else {
            // Các trường hợp còn lại: PRIVATE → PUBLIC, PUBLIC → PUBLIC, v.v.
            flashcard.setVisibility(visibility);
            flashcard.setUpdatedAt(LocalDateTime.now());
            updated = flashcardRepository.save(flashcard);
        }

        // Thêm: Notify owner
        NotificationCenter.notifyObservers(NotificationEvent.builder()
                .receiverId(userId)
                .type("SYSTEM")
                .title("Cập nhật trạng thái flashcard")
                .content("Flashcard \"" + updated.getTitle() + "\" đã chuyển sang "
                        + updated.getVisibility().name())
                .build());

        log.info("Flashcard {} visibility changed to {} by {}", id, updated.getVisibility(), userId);

        FlashcardResponse response = flashcardMapper.toResponse(updated);
        response.setWordCount((int) wordRepository.countByFlashcardId(id));
        return response;
    }

    private Flashcard setPrivateAtomically(String flashcardId) {
        long learnerCount = progressRepository.countByFlashcardId(flashcardId);

        if (learnerCount > 0) {
            return null;
        }

        Query query = new Query(Criteria.where("_id").is(flashcardId)
                .and("visibility").is(EFlashcardVisibility.PUBLIC.name()));

        Update update = new Update()
                .set("visibility", EFlashcardVisibility.PRIVATE.name())
                .set("updatedAt", LocalDateTime.now());

        FindAndModifyOptions options = FindAndModifyOptions.options()
                .returnNew(true)
                .upsert(false);

        return mongoTemplate.findAndModify(query, update, options, Flashcard.class);
    }

    @Override
    public List<FlashcardResponse> getPublicFlashcards(Authentication authentication) {
        log.info("Getting public flashcards");

        List<Flashcard> flashcards = flashcardRepository.findByVisibility(EFlashcardVisibility.PUBLIC);

        // Lọc ra các flashcard không bị vi phạm (optional, tuỳ logic)
        // flashcards = flashcards.stream()
        // .filter(f -> f.getIsViolated() == null || !f.getIsViolated())
        // .toList();

        List<FlashcardResponse> responses = flashcardMapper.toResponseList(flashcards);

        responses.forEach(response -> {
            long count = wordRepository.countByFlashcardId(response.getId());
            response.setWordCount((int) count);
        });

        return responses;
    }

    @Override
    public List<FlashcardResponse> getMyFlashcards(Authentication authentication) {
        log.info("Getting user's flashcards");

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn phải đăng nhập để xem flashcard của mình");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUser().getId().toString();

        List<Flashcard> flashcards = flashcardRepository.findByCreatedBy(userId);

        List<FlashcardResponse> responses = flashcardMapper.toResponseList(flashcards);

        responses.forEach(response -> {
            long count = wordRepository.countByFlashcardId(response.getId());
            response.setWordCount((int) count);
            response.setIsOwner(true);
        });

        return responses;
    }

    @Override
    public List<FlashcardResponse> getAllFlashcardWithPublic(Authentication authentication) {
        log.info("Getting all flashcards: user's flashcards + PUBLIC flashcards from system/other members");

        List<Flashcard> allFlashcards = new java.util.ArrayList<>();
        java.util.Set<String> addedIds = new java.util.HashSet<>();

        // 1. Nếu user đã đăng nhập, lấy tất cả flashcard của user
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String userId = userDetails.getUser().getId().toString();

            List<Flashcard> userFlashcards = flashcardRepository.findByCreatedBy(userId);
            allFlashcards.addAll(userFlashcards);
            userFlashcards.forEach(f -> addedIds.add(f.getId()));
        }

        // 2. Lấy tất cả PUBLIC flashcards (từ hệ thống và từ members khác)
        List<Flashcard> publicFlashcards = flashcardRepository.findByVisibility(EFlashcardVisibility.PUBLIC);
        for (Flashcard flashcard : publicFlashcards) {
            // Tránh trùng lặp: nếu đã thêm flashcard này từ user's flashcards, thì bỏ qua
            if (!addedIds.contains(flashcard.getId())) {
                allFlashcards.add(flashcard);
                addedIds.add(flashcard.getId());
            }
        }

        List<FlashcardResponse> responses = flashcardMapper.toResponseList(allFlashcards);

        // Set isOwner flag nếu authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String userId = userDetails.getUser().getId().toString();

            responses.forEach(response -> {
                long count = wordRepository.countByFlashcardId(response.getId());
                response.setWordCount((int) count);

                // Kiểm tra xem user có phải là owner không
                // Nếu createdBy là null hoặc trùng với userId, thì là owner
                Boolean isOwner = response.getCreatedBy() != null && response.getCreatedBy().equals(userId);
                response.setIsOwner(isOwner);
            });
        } else {
            responses.forEach(response -> {
                long count = wordRepository.countByFlashcardId(response.getId());
                response.setWordCount((int) count);
                response.setIsOwner(false);
            });
        }

        return responses;
    }
}
