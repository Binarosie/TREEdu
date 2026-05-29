package vn.hcmute.edu.materialsservice.services;

import org.springframework.web.multipart.MultipartFile;
import vn.hcmute.edu.materialsservice.Enum.ELessonStatus;
import vn.hcmute.edu.materialsservice.dtos.request.DictationCheckRequest;
import vn.hcmute.edu.materialsservice.dtos.request.DictationCreateRequest;
import vn.hcmute.edu.materialsservice.dtos.request.DictationUpdateRequest;
import vn.hcmute.edu.materialsservice.dtos.response.DictationCheckResponse;
import vn.hcmute.edu.materialsservice.models.DictationLesson;

import java.util.List;

public interface iDictationService {
    // Cho Supporter
    DictationLesson createLesson(DictationCreateRequest request);

    // Cho Member
    List<DictationLesson> getAllLessonsForMember(); // Chỉ lấy bài PUBLISHED
    List<DictationLesson> getAllLessonsForAdmin();  // Lấy hết (cả DRAFT lẫn PUBLISHED)
    DictationLesson getLessonById(String id);
    DictationCheckResponse checkAnswer(String dictationId, DictationCheckRequest request);
    void updateLessonStatus(String id, ELessonStatus status); // Hàm để Supporter bật/tắt ẩn hiện
    DictationLesson generateLessonWithAI(MultipartFile file, String title, String level, String audioUrl);
    DictationLesson updateLesson(String id, DictationUpdateRequest request);
}
