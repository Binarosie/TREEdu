package vn.hcmute.edu.materialsservice.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.hcmute.edu.materialsservice.dtos.response.BadRequestError;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.avatar-folder:avatars}")
    private String avatarFolder;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String uploadAudio(MultipartFile file) throws IOException {
        // Cloudinary nhận raw bytes, không cần lưu file tạm xuống disk
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                Map.of(
                        "resource_type", "video", // Cloudinary dùng "video" cho cả audio lẫn video
                        "folder", "treedu/audio",
                        "public_id", UUID.randomUUID().toString(),
                        "overwrite", false));

        String url = (String) result.get("secure_url");
        log.info("Upload Cloudinary thành công: {}", url);
        return url;
    }

    /**
     * Download audio từ temporary URL (FPT TTS) và upload lên Cloudinary
     * Để lưu trữ vĩnh viễn thay vì dùng temporary link
     */
    public String downloadAndStoreAudio(String tempAudioUrl, String wordId) {
        if (tempAudioUrl == null || tempAudioUrl.isBlank()) {
            log.warn("Temporary audio URL is empty, skipping storage");
            return null;
        }

        try {
            log.debug("📥 Downloading audio from temporary URL for word: {}", wordId);

            // 1. Download file từ URL tạm của FPT
            URL url = new URL(tempAudioUrl);
            InputStream inputStream = url.openStream();

            // 2. Upload lên Cloudinary
            Map<?, ?> result = cloudinary.uploader().upload(
                    inputStream,
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", "treedu/audio",
                            "public_id", "word_" + wordId, // Public ID để dễ xóa sau
                            "overwrite", true));

            String permanentUrl = (String) result.get("secure_url");
            log.info("✅ Audio stored permanently for word {}: {}", wordId, permanentUrl);

            return permanentUrl;

        } catch (IOException e) {
            log.error("❌ Failed to download/store audio for word {}: {}", wordId, e.getMessage(), e);
            return null; // Trả về null nếu lỗi, không throw để không block flow chính
        }
    }

    /**
     * Upload ảnh lên Cloudinary, trả về secure URL.
     * public_id = avatars/{userId} → tự động ghi đè ảnh cũ, không cần xóa thủ công.
     */
    public String uploadAvatar(MultipartFile file, String userId) {
        validateFile(file);

        try {
            log.debug("📤 Uploading avatar for user: {} (file size: {} bytes)", userId, file.getSize());

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", avatarFolder,
                            "public_id", userId, // cùng userId → overwrite ảnh cũ
                            "overwrite", true,
                            "resource_type", "image"
                    // "allowed_formats", new String[]{"png", "jpg", "jpeg"}
                    ));

            String secureUrl = (String) result.get("secure_url");
            log.info("✅ Uploaded avatar for user {}: {}", userId, secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("❌ Cloudinary upload failed for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Không thể upload ảnh lên Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Xóa ảnh theo public_id (avatars/{userId}).
     * Gọi khi user xóa avatar (set về null).
     */
    public void deleteAvatar(String userId) {
        try {
            String publicId = avatarFolder + "/" + userId;
            log.debug("🗑️ Deleting avatar: {}", publicId);

            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("✅ Avatar deleted for user {}: {}", userId, result.get("result"));

        } catch (IOException e) {
            log.warn("⚠️ Could not delete avatar for user {}: {}", userId, e.getMessage());
            // Không throw — tránh block flow chính
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("⚠️ Empty file received");
            throw new BadRequestError("File ảnh không được để trống");
        }

        String contentType = file.getContentType();
        log.debug("📋 File validation - name: {}, size: {}, type: {}",
                file.getOriginalFilename(), file.getSize(), contentType);

        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            log.warn("⚠️ Invalid content type: {}", contentType);
            throw new BadRequestError("Chỉ chấp nhận file .png hoặc .jpg/.jpeg");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("⚠️ File too large: {} bytes (max: {} bytes)", file.getSize(), MAX_FILE_SIZE);
            throw new BadRequestError("Kích thước file không được vượt quá 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.warn("⚠️ Invalid filename");
            throw new BadRequestError("Tên file không hợp lệ");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!Set.of("png", "jpg", "jpeg").contains(ext)) {
            log.warn("⚠️ Invalid file extension: {}", ext);
            throw new BadRequestError("Chỉ chấp nhận file .png hoặc .jpg/.jpeg");
        }

        log.debug("✅ File validation passed");
    }
}
